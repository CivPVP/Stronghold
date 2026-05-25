package me.neznamy.tab.libs.redis.clients.jedis.executors;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import me.neznamy.tab.libs.redis.clients.jedis.CommandObject;
import me.neznamy.tab.libs.redis.clients.jedis.Connection;
import me.neznamy.tab.libs.redis.clients.jedis.ConnectionPool;
import me.neznamy.tab.libs.redis.clients.jedis.HostAndPort;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.annots.VisibleForTesting;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisAskDataException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisBroadcastException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisClusterOperationException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisConnectionException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisMovedDataException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisRedirectionException;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ClusterConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterCommandExecutor implements CommandExecutor {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   public final ClusterConnectionProvider provider;
   protected final int maxAttempts;
   protected final Duration maxTotalRetriesDuration;

   public ClusterCommandExecutor(ClusterConnectionProvider provider, int maxAttempts, Duration maxTotalRetriesDuration) {
      this.provider = provider;
      this.maxAttempts = maxAttempts;
      this.maxTotalRetriesDuration = maxTotalRetriesDuration;
   }

   @Override
   public void close() {
      this.provider.close();
   }

   @Override
   public final <T> T broadcastCommand(CommandObject<T> commandObject) {
      Map<String, ConnectionPool> connectionMap = this.provider.getConnectionMap();
      boolean isErrored = false;
      T reply = null;
      JedisBroadcastException bcastError = new JedisBroadcastException();

      for (Entry<String, ConnectionPool> entry : connectionMap.entrySet()) {
         HostAndPort node = HostAndPort.from(entry.getKey());
         ConnectionPool pool = entry.getValue();

         try (Connection connection = pool.getResource()) {
            T aReply = this.execute(connection, commandObject);
            bcastError.addReply(node, aReply);
            if (!isErrored) {
               if (reply == null) {
                  reply = aReply;
               } else if (!reply.equals(aReply)) {
                  isErrored = true;
                  reply = null;
               }
            }
         } catch (Exception anError) {
            bcastError.addReply(node, anError);
            isErrored = true;
         }
      }

      if (isErrored) {
         throw bcastError;
      } else {
         return reply;
      }
   }

   @Override
   public final <T> T executeCommand(CommandObject<T> commandObject) {
      return this.doExecuteCommand(commandObject, false);
   }

   public final <T> T executeCommandToReplica(CommandObject<T> commandObject) {
      return this.doExecuteCommand(commandObject, true);
   }

   private <T> T doExecuteCommand(CommandObject<T> commandObject, boolean toReplica) {
      Instant deadline = Instant.now().plus(this.maxTotalRetriesDuration);
      JedisRedirectionException redirect = null;
      int consecutiveConnectionFailures = 0;
      Exception lastException = null;

      for (int attemptsLeft = this.maxAttempts; attemptsLeft > 0; attemptsLeft--) {
         Connection connection = null;

         try {
            if (redirect != null) {
               connection = this.provider.getConnection(redirect.getTargetNode());
               if (redirect instanceof JedisAskDataException) {
                  connection.executeCommand(Protocol.Command.ASKING);
               }
            } else {
               connection = toReplica
                  ? this.provider.getReplicaConnection(commandObject.getArguments())
                  : this.provider.getConnection(commandObject.getArguments());
            }

            return this.execute(connection, commandObject);
         } catch (JedisClusterOperationException jnrcne) {
            throw jnrcne;
         } catch (JedisConnectionException jce) {
            lastException = jce;
            consecutiveConnectionFailures++;
            this.log.debug("Failed connecting to Redis: {}", connection, jce);
            boolean reset = this.handleConnectionProblem(attemptsLeft - 1, consecutiveConnectionFailures, deadline);
            if (reset) {
               consecutiveConnectionFailures = 0;
               redirect = null;
            }
         } catch (JedisRedirectionException jre) {
            if (lastException == null || lastException instanceof JedisRedirectionException) {
               lastException = jre;
            }

            this.log.debug("Redirected by server to {}", jre.getTargetNode());
            consecutiveConnectionFailures = 0;
            redirect = jre;
            if (jre instanceof JedisMovedDataException) {
               this.provider.renewSlotCache(connection);
            }
         } finally {
            IOUtils.closeQuietly(connection);
         }

         if (Instant.now().isAfter(deadline)) {
            throw new JedisClusterOperationException("Cluster retry deadline exceeded.");
         }
      }

      JedisClusterOperationException maxAttemptsException = new JedisClusterOperationException("No more cluster attempts left.");
      maxAttemptsException.addSuppressed(lastException);
      throw maxAttemptsException;
   }

   @VisibleForTesting
   protected <T> T execute(Connection connection, CommandObject<T> commandObject) {
      return connection.executeCommand(commandObject);
   }

   private boolean handleConnectionProblem(int attemptsLeft, int consecutiveConnectionFailures, Instant doneDeadline) {
      if (this.maxAttempts < 3) {
         if (attemptsLeft == 0) {
            this.provider.renewSlotCache();
            return true;
         } else {
            return false;
         }
      } else {
         if (consecutiveConnectionFailures < 2) {
            return false;
         }

         this.sleep(getBackoffSleepMillis(attemptsLeft, doneDeadline));
         this.provider.renewSlotCache();
         return true;
      }
   }

   private static long getBackoffSleepMillis(int attemptsLeft, Instant deadline) {
      if (attemptsLeft <= 0) {
         return 0L;
      }

      long millisLeft = Duration.between(Instant.now(), deadline).toMillis();
      if (millisLeft < 0L) {
         throw new JedisClusterOperationException("Cluster retry deadline exceeded.");
      }

      long maxBackOff = millisLeft / (attemptsLeft * attemptsLeft);
      return ThreadLocalRandom.current().nextLong(maxBackOff + 1L);
   }

   @VisibleForTesting
   protected void sleep(long sleepMillis) {
      try {
         TimeUnit.MILLISECONDS.sleep(sleepMillis);
      } catch (InterruptedException e) {
         throw new JedisClusterOperationException(e);
      }
   }
}
