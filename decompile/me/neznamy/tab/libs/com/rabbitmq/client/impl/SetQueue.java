package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SetQueue<T> {
   private final Set<T> members = new HashSet<>();
   private final Queue<T> queue = new LinkedList<>();

   public boolean addIfNotPresent(T item) {
      if (this.members.contains(item)) {
         return false;
      }

      this.members.add(item);
      this.queue.offer(item);
      return true;
   }

   public T poll() {
      T item = this.queue.poll();
      if (item != null) {
         this.members.remove(item);
      }

      return item;
   }

   public boolean contains(T item) {
      return this.members.contains(item);
   }

   public boolean isEmpty() {
      return this.members.isEmpty();
   }

   public boolean remove(T item) {
      this.queue.remove(item);
      return this.members.remove(item);
   }

   public void clear() {
      this.queue.clear();
      this.members.clear();
   }
}
