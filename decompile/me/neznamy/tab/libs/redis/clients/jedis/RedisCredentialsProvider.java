package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.function.Supplier;

public interface RedisCredentialsProvider extends Supplier<RedisCredentials> {
   default void prepare() {
   }

   default void cleanUp() {
   }
}
