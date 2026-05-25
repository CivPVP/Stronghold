package me.neznamy.tab.libs.redis.clients.jedis;

public abstract class JedisMonitor {
   protected Connection client;

   public void proceed(Connection client) {
      this.client = client;
      this.client.setTimeoutInfinite();

      do {
         String command = client.getBulkReply();
         this.onCommand(command);
      } while (client.isConnected());
   }

   public abstract void onCommand(String var1);
}
