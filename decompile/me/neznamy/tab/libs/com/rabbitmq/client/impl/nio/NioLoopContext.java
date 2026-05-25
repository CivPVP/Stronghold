package me.neznamy.tab.libs.com.rabbitmq.client.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import me.neznamy.tab.libs.com.rabbitmq.client.impl.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioLoopContext {
   private static final Logger LOGGER = LoggerFactory.getLogger(NioLoopContext.class);
   private final SocketChannelFrameHandlerFactory socketChannelFrameHandlerFactory;
   private final ExecutorService executorService;
   private final ThreadFactory threadFactory;
   final ByteBuffer readBuffer;
   final ByteBuffer writeBuffer;
   SelectorHolder readSelectorState;
   SelectorHolder writeSelectorState;

   public NioLoopContext(SocketChannelFrameHandlerFactory socketChannelFrameHandlerFactory, NioParams nioParams) {
      this.socketChannelFrameHandlerFactory = socketChannelFrameHandlerFactory;
      this.executorService = nioParams.getNioExecutor();
      this.threadFactory = nioParams.getThreadFactory();
      NioContext nioContext = new NioContext(nioParams, null);
      this.readBuffer = nioParams.getByteBufferFactory().createReadBuffer(nioContext);
      this.writeBuffer = nioParams.getByteBufferFactory().createWriteBuffer(nioContext);
   }

   void initStateIfNecessary() throws IOException {
      this.socketChannelFrameHandlerFactory.lock();

      try {
         if (this.readSelectorState == null) {
            this.readSelectorState = new SelectorHolder(Selector.open());
            this.writeSelectorState = new SelectorHolder(Selector.open());
            this.startIoLoops();
         }
      } finally {
         this.socketChannelFrameHandlerFactory.unlock();
      }
   }

   private void startIoLoops() {
      if (this.executorService == null) {
         Thread nioThread = Environment.newThread(this.threadFactory, new NioLoop(this.socketChannelFrameHandlerFactory.nioParams, this), "rabbitmq-nio");
         nioThread.start();
      } else {
         this.executorService.submit(new NioLoop(this.socketChannelFrameHandlerFactory.nioParams, this));
      }
   }

   protected boolean cleanUp() {
      int readRegistrationsCount = this.readSelectorState.registrations.size();
      if (readRegistrationsCount != 0) {
         return false;
      }

      this.socketChannelFrameHandlerFactory.lock();

      try {
         if (readRegistrationsCount != this.readSelectorState.registrations.size()) {
            return false;
         }

         try {
            this.readSelectorState.selector.close();
         } catch (IOException e) {
            LOGGER.warn("Could not close read selector: {}", e.getMessage());
         }

         try {
            this.writeSelectorState.selector.close();
         } catch (IOException e) {
            LOGGER.warn("Could not close write selector: {}", e.getMessage());
         }

         this.readSelectorState = null;
         this.writeSelectorState = null;
      } finally {
         this.socketChannelFrameHandlerFactory.unlock();
      }

      return true;
   }
}
