package me.neznamy.tab.libs.redis.clients.jedis;

@Deprecated
public abstract class TransactionBase extends AbstractTransaction {
   @Deprecated
   protected TransactionBase() {
   }

   protected TransactionBase(CommandObjects commandObjects) {
      super(commandObjects);
   }
}
