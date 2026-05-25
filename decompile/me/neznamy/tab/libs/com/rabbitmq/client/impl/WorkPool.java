package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class WorkPool<K, W> {
   private static final int MAX_QUEUE_LENGTH = 1000;
   private final SetQueue<K> ready = new SetQueue<>();
   private final Set<K> inProgress = new HashSet<>();
   private final Map<K, VariableLinkedBlockingQueue<W>> pool = new HashMap<>();
   private final Set<K> unlimited = new HashSet<>();
   private final BiConsumer<VariableLinkedBlockingQueue<W>, W> enqueueingCallback;

   public WorkPool(int queueingTimeout) {
      if (queueingTimeout > 0) {
         this.enqueueingCallback = (queue, item) -> {
            try {
               boolean offered = queue.offer(item, queueingTimeout, TimeUnit.MILLISECONDS);
               if (!offered) {
                  throw new WorkPoolFullException("Could not enqueue in work pool after " + queueingTimeout + " ms.");
               }
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
         };
      } else {
         this.enqueueingCallback = (queue, item) -> {
            try {
               queue.put(item);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
         };
      }
   }

   public void registerKey(K key) {
      synchronized (this) {
         if (!this.pool.containsKey(key)) {
            int initialCapacity = this.unlimited.isEmpty() ? 1000 : Integer.MAX_VALUE;
            this.pool.put(key, new VariableLinkedBlockingQueue<>(initialCapacity));
         }
      }
   }

   public synchronized void limit(K key) {
      this.unlimited.remove(key);
      if (this.unlimited.isEmpty()) {
         this.setCapacities(1000);
      }
   }

   public synchronized void unlimit(K key) {
      this.unlimited.add(key);
      if (!this.unlimited.isEmpty()) {
         this.setCapacities(Integer.MAX_VALUE);
      }
   }

   private void setCapacities(int capacity) {
      Iterator<VariableLinkedBlockingQueue<W>> it = this.pool.values().iterator();

      while (it.hasNext()) {
         it.next().setCapacity(capacity);
      }
   }

   public void unregisterKey(K key) {
      synchronized (this) {
         this.pool.remove(key);
         this.ready.remove(key);
         this.inProgress.remove(key);
         this.unlimited.remove(key);
      }
   }

   public void unregisterAllKeys() {
      synchronized (this) {
         this.pool.clear();
         this.ready.clear();
         this.inProgress.clear();
         this.unlimited.clear();
      }
   }

   public K nextWorkBlock(Collection<W> to, int size) {
      synchronized (this) {
         K nextKey = this.readyToInProgress();
         if (nextKey != null) {
            VariableLinkedBlockingQueue<W> queue = this.pool.get(nextKey);
            this.drainTo(queue, to, size);
         }

         return nextKey;
      }
   }

   private int drainTo(VariableLinkedBlockingQueue<W> deList, Collection<W> c, int maxElements) {
      int n;
      for (n = 0; n < maxElements; n++) {
         W first = deList.poll();
         if (first == null) {
            break;
         }

         c.add(first);
      }

      return n;
   }

   public boolean addWorkItem(K key, W item) {
      VariableLinkedBlockingQueue<W> queue;
      synchronized (this) {
         queue = this.pool.get(key);
      }

      if (queue != null) {
         this.enqueueingCallback.accept(queue, item);
         synchronized (this) {
            if (this.isDormant(key)) {
               this.dormantToReady(key);
               return true;
            }
         }
      }

      return false;
   }

   public boolean finishWorkBlock(K key) {
      synchronized (this) {
         if (!this.isRegistered(key)) {
            return false;
         } else if (!this.inProgress.contains(key)) {
            throw new IllegalStateException("Client " + key + " not in progress");
         } else if (this.moreWorkItems(key)) {
            this.inProgressToReady(key);
            return true;
         } else {
            this.inProgressToDormant(key);
            return false;
         }
      }
   }

   private boolean moreWorkItems(K key) {
      VariableLinkedBlockingQueue<W> leList = this.pool.get(key);
      return leList != null && !leList.isEmpty();
   }

   private boolean isInProgress(K key) {
      return this.inProgress.contains(key);
   }

   private boolean isReady(K key) {
      return this.ready.contains(key);
   }

   private boolean isRegistered(K key) {
      return this.pool.containsKey(key);
   }

   private boolean isDormant(K key) {
      return !this.isInProgress(key) && !this.isReady(key) && this.isRegistered(key);
   }

   private void inProgressToReady(K key) {
      this.inProgress.remove(key);
      this.ready.addIfNotPresent(key);
   }

   private void inProgressToDormant(K key) {
      this.inProgress.remove(key);
   }

   private void dormantToReady(K key) {
      this.ready.addIfNotPresent(key);
   }

   private K readyToInProgress() {
      K key = this.ready.poll();
      if (key != null) {
         this.inProgress.add(key);
      }

      return key;
   }
}
