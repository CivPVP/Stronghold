package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.Environment;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioLoop implements Runnable {
   private static final Logger LOGGER = LoggerFactory.getLogger(NioLoop.class);
   private final NioLoopContext context;
   private final NioParams nioParams;
   private final ExecutorService connectionShutdownExecutor;

   public NioLoop(NioParams nioParams, NioLoopContext loopContext) {
      this.nioParams = nioParams;
      this.context = loopContext;
      this.connectionShutdownExecutor = nioParams.getConnectionShutdownExecutor();
   }

   @Override
   public void run() {
      SelectorHolder selectorState = this.context.readSelectorState;
      Selector selector = selectorState.selector;
      Set<SocketChannelRegistration> registrations = selectorState.registrations;
      ByteBuffer buffer = this.context.readBuffer;
      SelectorHolder writeSelectorState = this.context.writeSelectorState;
      Selector writeSelector = writeSelectorState.selector;
      Set<SocketChannelRegistration> writeRegistrations = writeSelectorState.registrations;
      boolean writeRegistered = false;

      try {
         while (!Thread.currentThread().isInterrupted()) {
            for (SelectionKey selectionKey : selector.keys()) {
               SocketChannelFrameHandlerState state = (SocketChannelFrameHandlerState)selectionKey.attachment();
               if (state.getConnection() != null && state.getHeartbeatNanoSeconds() > 0L) {
                  long now = System.nanoTime();
                  if (now - state.getLastActivity() > state.getHeartbeatNanoSeconds() * 2L) {
                     try {
                        this.handleHeartbeatFailure(state);
                     } catch (Exception e) {
                        LOGGER.warn("Error after heartbeat failure of connection {}", state.getConnection());
                     } finally {
                        selectionKey.cancel();
                     }
                  }
               }
            }

            int select;
            if (!writeRegistered && registrations.isEmpty() && writeRegistrations.isEmpty()) {
               select = selector.select(1000L);
               if (selector.keys().isEmpty()) {
                  boolean clean = this.context.cleanUp();
                  if (clean) {
                     return;
                  }
               }
            } else {
               select = selector.selectNow();
            }

            writeRegistered = false;
            Iterator<SocketChannelRegistration> registrationIterator = registrations.iterator();

            while (registrationIterator.hasNext()) {
               SocketChannelRegistration registration = registrationIterator.next();
               registrationIterator.remove();
               int operations = registration.operations;

               try {
                  if (registration.state.getChannel().isOpen()) {
                     registration.state.getChannel().register(selector, operations, registration.state);
                  }
               } catch (Exception e) {
                  LOGGER.info("Error while registering socket channel for read: {}", e.getMessage());
               }
            }

            if (select > 0) {
               Set<SelectionKey> readyKeys = selector.selectedKeys();
               Iterator<SelectionKey> iterator = readyKeys.iterator();

               while (iterator.hasNext()) {
                  SelectionKey key = iterator.next();
                  iterator.remove();
                  if (key.isValid()) {
                     SocketChannelFrameHandlerState state = (SocketChannelFrameHandlerState)key.attachment();

                     try {
                        if (key.isReadable()) {
                           if (!state.getChannel().isOpen()) {
                              key.cancel();
                           } else if (state.getConnection() != null) {
                              state.prepareForReadSequence();

                              while (true) {
                                 if (state.continueReading()) {
                                    Frame frame = state.frameBuilder.readFrame();
                                    if (frame == null) {
                                       continue;
                                    }

                                    try {
                                       state.getConnection().ioLoopThread(Thread.currentThread());
                                       boolean noProblem = state.getConnection().handleReadFrame(frame);
                                       if (!noProblem || state.getConnection().isRunning() && !state.getConnection().hasBrokerInitiatedShutdown()) {
                                          continue;
                                       }

                                       this.dispatchShutdownToConnection(state);
                                       key.cancel();
                                    } catch (Throwable ex) {
                                       this.handleIoError(state, ex);
                                       key.cancel();
                                    }
                                 }

                                 state.setLastActivity(System.nanoTime());
                                 break;
                              }
                           }
                        }
                     } catch (Exception e) {
                        LOGGER.warn("Error during reading frames", e);
                        this.handleIoError(state, e);
                        key.cancel();
                     } finally {
                        ((Buffer)buffer).clear();
                     }
                  }
               }
            }

            select = writeSelector.selectNow();
            Iterator<SocketChannelRegistration> writeRegistrationIterator = writeRegistrations.iterator();

            while (writeRegistrationIterator.hasNext()) {
               SocketChannelRegistration writeRegistration = writeRegistrationIterator.next();
               writeRegistrationIterator.remove();
               int operations = writeRegistration.operations;

               try {
                  if (writeRegistration.state.getChannel().isOpen()) {
                     writeRegistration.state.getChannel().register(writeSelector, operations, writeRegistration.state);
                     writeRegistered = true;
                  }
               } catch (Exception e) {
                  LOGGER.info("Error while registering socket channel for write: {}", e.getMessage());
               }
            }

            if (select > 0) {
               Set<SelectionKey> readyKeys = writeSelector.selectedKeys();
               Iterator<SelectionKey> iterator = readyKeys.iterator();

               while (iterator.hasNext()) {
                  SelectionKey key = iterator.next();
                  iterator.remove();
                  SocketChannelFrameHandlerState state = (SocketChannelFrameHandlerState)key.attachment();
                  if (key.isValid()) {
                     try {
                        if (key.isWritable()) {
                           if (!state.getChannel().isOpen()) {
                              key.cancel();
                           } else {
                              state.prepareForWriteSequence();
                              int toBeWritten = state.getWriteQueue().size();
                              int written = 0;
                              DataOutputStream outputStream = state.outputStream;

                              WriteRequest request;
                              while (written <= toBeWritten && (request = state.getWriteQueue().poll()) != null) {
                                 request.handle(outputStream);
                                 written++;
                              }

                              outputStream.flush();
                           }
                        }
                     } catch (Exception e) {
                        this.handleIoError(state, e);
                     } finally {
                        state.endWriteSequence();
                        key.cancel();
                     }
                  }
               }
            }
         }
      } catch (Exception e) {
         LOGGER.error("Error in NIO loop", e);
      }
   }

   protected void handleIoError(SocketChannelFrameHandlerState state, Throwable ex) {
      if (this.needToDispatchIoError(state)) {
         this.dispatchIoErrorToConnection(state, ex);
      } else {
         try {
            state.close();
         } catch (IOException var4) {
         }
      }
   }

   protected void handleHeartbeatFailure(SocketChannelFrameHandlerState state) {
      if (this.needToDispatchIoError(state)) {
         this.dispatchShutdownToConnection(() -> state.getConnection().handleHeartbeatFailure(), state.getConnection().toString());
      } else {
         try {
            state.close();
         } catch (IOException var3) {
         }
      }
   }

   protected boolean needToDispatchIoError(SocketChannelFrameHandlerState state) {
      return state.getConnection().isOpen();
   }

   protected void dispatchIoErrorToConnection(SocketChannelFrameHandlerState state, Throwable ex) {
      this.dispatchShutdownToConnection(() -> state.getConnection().handleIoError(ex), state.getConnection().toString());
   }

   protected void dispatchShutdownToConnection(SocketChannelFrameHandlerState state) {
      this.dispatchShutdownToConnection(() -> state.getConnection().doFinalShutdown(), state.getConnection().toString());
   }

   protected void dispatchShutdownToConnection(Runnable connectionShutdownRunnable, String connectionName) {
      if (this.connectionShutdownExecutor != null) {
         this.connectionShutdownExecutor.execute(connectionShutdownRunnable);
      } else if (this.executorService() != null) {
         this.executorService().execute(connectionShutdownRunnable);
      } else {
         String name = "rabbitmq-connection-shutdown-" + connectionName;
         Thread shutdownThread = Environment.newThread(this.threadFactory(), connectionShutdownRunnable, name);
         shutdownThread.start();
      }
   }

   private ExecutorService executorService() {
      return this.nioParams.getNioExecutor();
   }

   private ThreadFactory threadFactory() {
      return this.nioParams.getThreadFactory();
   }
}
