package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public interface Channel extends ShutdownNotifier, AutoCloseable {
   int getChannelNumber();

   Connection getConnection();

   @Override
   void close() throws IOException, TimeoutException;

   void close(int var1, String var2) throws IOException, TimeoutException;

   void abort() throws IOException;

   void abort(int var1, String var2) throws IOException;

   void addReturnListener(ReturnListener var1);

   ReturnListener addReturnListener(ReturnCallback var1);

   boolean removeReturnListener(ReturnListener var1);

   void clearReturnListeners();

   void addConfirmListener(ConfirmListener var1);

   ConfirmListener addConfirmListener(ConfirmCallback var1, ConfirmCallback var2);

   boolean removeConfirmListener(ConfirmListener var1);

   void clearConfirmListeners();

   Consumer getDefaultConsumer();

   void setDefaultConsumer(Consumer var1);

   void basicQos(int var1, int var2, boolean var3) throws IOException;

   void basicQos(int var1, boolean var2) throws IOException;

   void basicQos(int var1) throws IOException;

   void basicPublish(String var1, String var2, AMQP.BasicProperties var3, byte[] var4) throws IOException;

   void basicPublish(String var1, String var2, boolean var3, AMQP.BasicProperties var4, byte[] var5) throws IOException;

   void basicPublish(String var1, String var2, boolean var3, boolean var4, AMQP.BasicProperties var5, byte[] var6) throws IOException;

   AMQP.Exchange.DeclareOk exchangeDeclare(String var1, String var2) throws IOException;

   AMQP.Exchange.DeclareOk exchangeDeclare(String var1, BuiltinExchangeType var2) throws IOException;

   AMQP.Exchange.DeclareOk exchangeDeclare(String var1, String var2, boolean var3) throws IOException;

   AMQP.Exchange.DeclareOk exchangeDeclare(String var1, BuiltinExchangeType var2, boolean var3) throws IOException;

   AMQP.Exchange.DeclareOk exchangeDeclare(String var1, String var2, boolean var3, boolean var4, Map<String, Object> var5) throws IOException;

   AMQP.Exchange.DeclareOk exchangeDeclare(String var1, BuiltinExchangeType var2, boolean var3, boolean var4, Map<String, Object> var5) throws IOException;

   AMQP.Exchange.DeclareOk exchangeDeclare(String var1, String var2, boolean var3, boolean var4, boolean var5, Map<String, Object> var6) throws IOException;

   AMQP.Exchange.DeclareOk exchangeDeclare(String var1, BuiltinExchangeType var2, boolean var3, boolean var4, boolean var5, Map<String, Object> var6) throws IOException;

   void exchangeDeclareNoWait(String var1, String var2, boolean var3, boolean var4, boolean var5, Map<String, Object> var6) throws IOException;

   void exchangeDeclareNoWait(String var1, BuiltinExchangeType var2, boolean var3, boolean var4, boolean var5, Map<String, Object> var6) throws IOException;

   AMQP.Exchange.DeclareOk exchangeDeclarePassive(String var1) throws IOException;

   AMQP.Exchange.DeleteOk exchangeDelete(String var1, boolean var2) throws IOException;

   void exchangeDeleteNoWait(String var1, boolean var2) throws IOException;

   AMQP.Exchange.DeleteOk exchangeDelete(String var1) throws IOException;

   AMQP.Exchange.BindOk exchangeBind(String var1, String var2, String var3) throws IOException;

   AMQP.Exchange.BindOk exchangeBind(String var1, String var2, String var3, Map<String, Object> var4) throws IOException;

   void exchangeBindNoWait(String var1, String var2, String var3, Map<String, Object> var4) throws IOException;

   AMQP.Exchange.UnbindOk exchangeUnbind(String var1, String var2, String var3) throws IOException;

   AMQP.Exchange.UnbindOk exchangeUnbind(String var1, String var2, String var3, Map<String, Object> var4) throws IOException;

   void exchangeUnbindNoWait(String var1, String var2, String var3, Map<String, Object> var4) throws IOException;

   AMQP.Queue.DeclareOk queueDeclare() throws IOException;

   AMQP.Queue.DeclareOk queueDeclare(String var1, boolean var2, boolean var3, boolean var4, Map<String, Object> var5) throws IOException;

   void queueDeclareNoWait(String var1, boolean var2, boolean var3, boolean var4, Map<String, Object> var5) throws IOException;

   AMQP.Queue.DeclareOk queueDeclarePassive(String var1) throws IOException;

   AMQP.Queue.DeleteOk queueDelete(String var1) throws IOException;

   AMQP.Queue.DeleteOk queueDelete(String var1, boolean var2, boolean var3) throws IOException;

   void queueDeleteNoWait(String var1, boolean var2, boolean var3) throws IOException;

   AMQP.Queue.BindOk queueBind(String var1, String var2, String var3) throws IOException;

   AMQP.Queue.BindOk queueBind(String var1, String var2, String var3, Map<String, Object> var4) throws IOException;

   void queueBindNoWait(String var1, String var2, String var3, Map<String, Object> var4) throws IOException;

   AMQP.Queue.UnbindOk queueUnbind(String var1, String var2, String var3) throws IOException;

   AMQP.Queue.UnbindOk queueUnbind(String var1, String var2, String var3, Map<String, Object> var4) throws IOException;

   AMQP.Queue.PurgeOk queuePurge(String var1) throws IOException;

   GetResponse basicGet(String var1, boolean var2) throws IOException;

   void basicAck(long var1, boolean var3) throws IOException;

   void basicNack(long var1, boolean var3, boolean var4) throws IOException;

   void basicReject(long var1, boolean var3) throws IOException;

   String basicConsume(String var1, Consumer var2) throws IOException;

   String basicConsume(String var1, DeliverCallback var2, CancelCallback var3) throws IOException;

   String basicConsume(String var1, DeliverCallback var2, ConsumerShutdownSignalCallback var3) throws IOException;

   String basicConsume(String var1, DeliverCallback var2, CancelCallback var3, ConsumerShutdownSignalCallback var4) throws IOException;

   String basicConsume(String var1, boolean var2, Consumer var3) throws IOException;

   String basicConsume(String var1, boolean var2, DeliverCallback var3, CancelCallback var4) throws IOException;

   String basicConsume(String var1, boolean var2, DeliverCallback var3, ConsumerShutdownSignalCallback var4) throws IOException;

   String basicConsume(String var1, boolean var2, DeliverCallback var3, CancelCallback var4, ConsumerShutdownSignalCallback var5) throws IOException;

   String basicConsume(String var1, boolean var2, Map<String, Object> var3, Consumer var4) throws IOException;

   String basicConsume(String var1, boolean var2, Map<String, Object> var3, DeliverCallback var4, CancelCallback var5) throws IOException;

   String basicConsume(String var1, boolean var2, Map<String, Object> var3, DeliverCallback var4, ConsumerShutdownSignalCallback var5) throws IOException;

   String basicConsume(String var1, boolean var2, Map<String, Object> var3, DeliverCallback var4, CancelCallback var5, ConsumerShutdownSignalCallback var6) throws IOException;

   String basicConsume(String var1, boolean var2, String var3, Consumer var4) throws IOException;

   String basicConsume(String var1, boolean var2, String var3, DeliverCallback var4, CancelCallback var5) throws IOException;

   String basicConsume(String var1, boolean var2, String var3, DeliverCallback var4, ConsumerShutdownSignalCallback var5) throws IOException;

   String basicConsume(String var1, boolean var2, String var3, DeliverCallback var4, CancelCallback var5, ConsumerShutdownSignalCallback var6) throws IOException;

   String basicConsume(String var1, boolean var2, String var3, boolean var4, boolean var5, Map<String, Object> var6, Consumer var7) throws IOException;

   String basicConsume(String var1, boolean var2, String var3, boolean var4, boolean var5, Map<String, Object> var6, DeliverCallback var7, CancelCallback var8) throws IOException;

   String basicConsume(
      String var1, boolean var2, String var3, boolean var4, boolean var5, Map<String, Object> var6, DeliverCallback var7, ConsumerShutdownSignalCallback var8
   ) throws IOException;

   String basicConsume(
      String var1,
      boolean var2,
      String var3,
      boolean var4,
      boolean var5,
      Map<String, Object> var6,
      DeliverCallback var7,
      CancelCallback var8,
      ConsumerShutdownSignalCallback var9
   ) throws IOException;

   void basicCancel(String var1) throws IOException;

   AMQP.Basic.RecoverOk basicRecover() throws IOException;

   AMQP.Basic.RecoverOk basicRecover(boolean var1) throws IOException;

   AMQP.Tx.SelectOk txSelect() throws IOException;

   AMQP.Tx.CommitOk txCommit() throws IOException;

   AMQP.Tx.RollbackOk txRollback() throws IOException;

   AMQP.Confirm.SelectOk confirmSelect() throws IOException;

   long getNextPublishSeqNo();

   boolean waitForConfirms() throws InterruptedException;

   boolean waitForConfirms(long var1) throws InterruptedException, TimeoutException;

   void waitForConfirmsOrDie() throws IOException, InterruptedException;

   void waitForConfirmsOrDie(long var1) throws IOException, InterruptedException, TimeoutException;

   void asyncRpc(Method var1) throws IOException;

   Command rpc(Method var1) throws IOException;

   long messageCount(String var1) throws IOException;

   long consumerCount(String var1) throws IOException;

   CompletableFuture<Command> asyncCompletableRpc(Method var1) throws IOException;
}
