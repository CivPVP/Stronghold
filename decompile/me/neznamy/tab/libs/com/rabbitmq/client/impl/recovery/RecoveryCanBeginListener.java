package me.neznamy.tab.libs.com.rabbitmq.client.impl.recovery;

import me.neznamy.tab.libs.com.rabbitmq.client.ShutdownSignalException;

public interface RecoveryCanBeginListener {
   void recoveryCanBegin(ShutdownSignalException var1);
}
