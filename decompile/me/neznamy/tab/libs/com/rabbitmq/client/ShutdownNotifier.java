package me.neznamy.tab.libs.com.rabbitmq.client;

public interface ShutdownNotifier {
   void addShutdownListener(ShutdownListener var1);

   void removeShutdownListener(ShutdownListener var1);

   ShutdownSignalException getCloseReason();

   void notifyListeners();

   boolean isOpen();
}
