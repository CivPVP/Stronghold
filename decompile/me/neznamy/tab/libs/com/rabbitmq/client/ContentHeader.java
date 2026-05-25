package me.neznamy.tab.libs.com.rabbitmq.client;

public interface ContentHeader extends Cloneable {
   int getClassId();

   String getClassName();

   void appendPropertyDebugStringTo(StringBuilder var1);
}
