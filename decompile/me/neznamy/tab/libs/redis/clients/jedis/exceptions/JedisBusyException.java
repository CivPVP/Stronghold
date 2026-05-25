package me.neznamy.tab.libs.redis.clients.jedis.exceptions;

public class JedisBusyException extends JedisDataException {
   private static final long serialVersionUID = 3992655220229243478L;

   public JedisBusyException(String message) {
      super(message);
   }

   public JedisBusyException(Throwable cause) {
      super(cause);
   }

   public JedisBusyException(String message, Throwable cause) {
      super(message, cause);
   }
}
