package me.neznamy.tab.libs.redis.clients.jedis;

import java.time.Duration;
import java.util.Set;
import me.neznamy.tab.libs.org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import me.neznamy.tab.libs.redis.clients.jedis.providers.ClusterConnectionProvider;
import me.neznamy.tab.libs.redis.clients.jedis.util.IOUtils;

public class ClusterPipeline extends MultiNodePipelineBase {
   private final ClusterConnectionProvider provider;
   private AutoCloseable closeable = null;

   public ClusterPipeline(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig) {
      this(new ClusterConnectionProvider(clusterNodes, clientConfig), createClusterCommandObjects(clientConfig.getRedisProtocol()));
      this.closeable = this.provider;
   }

   public ClusterPipeline(Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig) {
      this(new ClusterConnectionProvider(clusterNodes, clientConfig, poolConfig), createClusterCommandObjects(clientConfig.getRedisProtocol()));
      this.closeable = this.provider;
   }

   public ClusterPipeline(
      Set<HostAndPort> clusterNodes, JedisClientConfig clientConfig, GenericObjectPoolConfig<Connection> poolConfig, Duration topologyRefreshPeriod
   ) {
      this(
         new ClusterConnectionProvider(clusterNodes, clientConfig, poolConfig, topologyRefreshPeriod),
         createClusterCommandObjects(clientConfig.getRedisProtocol())
      );
      this.closeable = this.provider;
   }

   public ClusterPipeline(ClusterConnectionProvider provider) {
      this(provider, new ClusterCommandObjects());
   }

   public ClusterPipeline(ClusterConnectionProvider provider, ClusterCommandObjects commandObjects) {
      super(commandObjects);
      this.provider = provider;
   }

   private static ClusterCommandObjects createClusterCommandObjects(RedisProtocol protocol) {
      ClusterCommandObjects cco = new ClusterCommandObjects();
      if (protocol == RedisProtocol.RESP3) {
         cco.setProtocol(protocol);
      }

      return cco;
   }

   public void prepareGraphCommands() {
      super.prepareGraphCommands(this.provider);
   }

   @Override
   public void close() {
      try {
         super.close();
      } finally {
         IOUtils.closeQuietly(this.closeable);
      }
   }

   @Override
   protected HostAndPort getNodeKey(CommandArguments args) {
      return this.provider.getNode(((ClusterCommandArguments)args).getCommandHashSlot());
   }

   @Override
   protected Connection getConnection(HostAndPort nodeKey) {
      return this.provider.getConnection(nodeKey);
   }

   public Response<Long> spublish(String channel, String message) {
      return this.appendCommand(this.commandObjects.spublish(channel, message));
   }

   public Response<Long> spublish(byte[] channel, byte[] message) {
      return this.appendCommand(this.commandObjects.spublish(channel, message));
   }
}
