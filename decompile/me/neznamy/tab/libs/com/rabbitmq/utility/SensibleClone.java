package me.neznamy.tab.libs.com.rabbitmq.utility;

public interface SensibleClone<T extends SensibleClone<T>> extends Cloneable {
   T sensibleClone();
}
