package me.neznamy.tab.libs.com.rabbitmq.client.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class VariableLinkedBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable {
   private static final long serialVersionUID = -6903933977591709194L;
   private int capacity;
   private final AtomicInteger count = new AtomicInteger(0);
   private transient VariableLinkedBlockingQueue.Node<E> head;
   private transient VariableLinkedBlockingQueue.Node<E> last;
   private final ReentrantLock takeLock = new ReentrantLock();
   private final Condition notEmpty = this.takeLock.newCondition();
   private final ReentrantLock putLock = new ReentrantLock();
   private final Condition notFull = this.putLock.newCondition();

   private void signalNotEmpty() {
      ReentrantLock takeLock = this.takeLock;
      takeLock.lock();

      try {
         this.notEmpty.signal();
      } finally {
         takeLock.unlock();
      }
   }

   private void signalNotFull() {
      ReentrantLock putLock = this.putLock;
      putLock.lock();

      try {
         this.notFull.signal();
      } finally {
         putLock.unlock();
      }
   }

   private void insert(E x) {
      this.last = this.last.next = new VariableLinkedBlockingQueue.Node<>(x);
   }

   private E extract() {
      VariableLinkedBlockingQueue.Node<E> first = this.head.next;
      this.head = first;
      E x = first.item;
      first.item = null;
      return x;
   }

   private void fullyLock() {
      this.putLock.lock();
      this.takeLock.lock();
   }

   private void fullyUnlock() {
      this.takeLock.unlock();
      this.putLock.unlock();
   }

   public VariableLinkedBlockingQueue() {
      this(Integer.MAX_VALUE);
   }

   public VariableLinkedBlockingQueue(int capacity) {
      if (capacity <= 0) {
         throw new IllegalArgumentException();
      }

      this.capacity = capacity;
      this.last = this.head = new VariableLinkedBlockingQueue.Node<>(null);
   }

   public VariableLinkedBlockingQueue(Collection<? extends E> c) {
      this(Integer.MAX_VALUE);
      Iterator<? extends E> it = c.iterator();

      while (it.hasNext()) {
         this.add((E)it.next());
      }
   }

   @Override
   public int size() {
      return this.count.get();
   }

   public void setCapacity(int capacity) {
      int oldCapacity = this.capacity;
      this.capacity = capacity;
      int size = this.count.get();
      if (capacity > size && size >= oldCapacity) {
         this.signalNotFull();
      }
   }

   @Override
   public int remainingCapacity() {
      return this.capacity - this.count.get();
   }

   @Override
   public void put(E o) throws InterruptedException {
      if (o == null) {
         throw new NullPointerException();
      }

      int c = -1;
      ReentrantLock putLock = this.putLock;
      AtomicInteger count = this.count;
      putLock.lockInterruptibly();

      try {
         try {
            while (count.get() >= this.capacity) {
               this.notFull.await();
            }
         } catch (InterruptedException ie) {
            this.notFull.signal();
            throw ie;
         }

         this.insert(o);
         c = count.getAndIncrement();
         if (c + 1 < this.capacity) {
            this.notFull.signal();
         }
      } finally {
         putLock.unlock();
      }

      if (c == 0) {
         this.signalNotEmpty();
      }
   }

   @Override
   public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {
      if (o == null) {
         throw new NullPointerException();
      }

      long nanos = unit.toNanos(timeout);
      int c = -1;
      ReentrantLock putLock = this.putLock;
      AtomicInteger count = this.count;
      putLock.lockInterruptibly();

      try {
         while (count.get() >= this.capacity) {
            if (nanos <= 0L) {
               return false;
            }

            try {
               nanos = this.notFull.awaitNanos(nanos);
            } catch (InterruptedException ie) {
               this.notFull.signal();
               throw ie;
            }
         }

         this.insert(o);
         c = count.getAndIncrement();
         if (c + 1 < this.capacity) {
            this.notFull.signal();
         }
      } finally {
         putLock.unlock();
      }

      if (c == 0) {
         this.signalNotEmpty();
      }

      return true;
   }

   @Override
   public boolean offer(E o) {
      if (o == null) {
         throw new NullPointerException();
      }

      AtomicInteger count = this.count;
      if (count.get() >= this.capacity) {
         return false;
      }

      int c = -1;
      ReentrantLock putLock = this.putLock;
      putLock.lock();

      try {
         if (count.get() < this.capacity) {
            this.insert(o);
            c = count.getAndIncrement();
            if (c + 1 < this.capacity) {
               this.notFull.signal();
            }
         }
      } finally {
         putLock.unlock();
      }

      if (c == 0) {
         this.signalNotEmpty();
      }

      return c >= 0;
   }

   @Override
   public E take() throws InterruptedException {
      int c = -1;
      AtomicInteger count = this.count;
      ReentrantLock takeLock = this.takeLock;
      takeLock.lockInterruptibly();

      E x;
      try {
         try {
            while (count.get() == 0) {
               this.notEmpty.await();
            }
         } catch (InterruptedException ie) {
            this.notEmpty.signal();
            throw ie;
         }

         x = this.extract();
         c = count.getAndDecrement();
         if (c > 1) {
            this.notEmpty.signal();
         }
      } finally {
         takeLock.unlock();
      }

      if (c >= this.capacity) {
         this.signalNotFull();
      }

      return x;
   }

   @Override
   public E poll(long timeout, TimeUnit unit) throws InterruptedException {
      E x = null;
      int c = -1;
      long nanos = unit.toNanos(timeout);
      AtomicInteger count = this.count;
      ReentrantLock takeLock = this.takeLock;
      takeLock.lockInterruptibly();

      try {
         while (count.get() <= 0) {
            if (nanos <= 0L) {
               return null;
            }

            try {
               nanos = this.notEmpty.awaitNanos(nanos);
            } catch (InterruptedException ie) {
               this.notEmpty.signal();
               throw ie;
            }
         }

         x = this.extract();
         c = count.getAndDecrement();
         if (c > 1) {
            this.notEmpty.signal();
         }
      } finally {
         takeLock.unlock();
      }

      if (c >= this.capacity) {
         this.signalNotFull();
      }

      return x;
   }

   @Override
   public E poll() {
      AtomicInteger count = this.count;
      if (count.get() == 0) {
         return null;
      }

      E x = null;
      int c = -1;
      ReentrantLock takeLock = this.takeLock;
      takeLock.lock();

      try {
         if (count.get() > 0) {
            x = this.extract();
            c = count.getAndDecrement();
            if (c > 1) {
               this.notEmpty.signal();
            }
         }
      } finally {
         takeLock.unlock();
      }

      if (c >= this.capacity) {
         this.signalNotFull();
      }

      return x;
   }

   @Override
   public E peek() {
      if (this.count.get() == 0) {
         return null;
      }

      ReentrantLock takeLock = this.takeLock;
      takeLock.lock();

      try {
         VariableLinkedBlockingQueue.Node<E> first = this.head.next;
         return first == null ? null : first.item;
      } finally {
         takeLock.unlock();
      }
   }

   @Override
   public boolean remove(Object o) {
      if (o == null) {
         return false;
      }

      boolean removed = false;
      this.fullyLock();

      try {
         VariableLinkedBlockingQueue.Node<E> trail = this.head;

         VariableLinkedBlockingQueue.Node<E> p;
         for (p = this.head.next; p != null; p = p.next) {
            if (o.equals(p.item)) {
               removed = true;
               break;
            }

            trail = p;
         }

         if (removed) {
            p.item = null;
            trail.next = p.next;
            if (this.count.getAndDecrement() >= this.capacity) {
               this.notFull.signalAll();
            }
         }
      } finally {
         this.fullyUnlock();
      }

      return removed;
   }

   @Override
   public Object[] toArray() {
      this.fullyLock();

      try {
         int size = this.count.get();
         Object[] a = new Object[size];
         int k = 0;

         for (VariableLinkedBlockingQueue.Node<E> p = this.head.next; p != null; p = p.next) {
            a[k++] = p.item;
         }

         return a;
      } finally {
         this.fullyUnlock();
      }
   }

   @Override
   public <T> T[] toArray(T[] a) {
      this.fullyLock();

      try {
         int size = this.count.get();
         if (a.length < size) {
            a = (T[])Array.newInstance(a.getClass().getComponentType(), size);
         }

         int k = 0;

         for (VariableLinkedBlockingQueue.Node<?> p = this.head.next; p != null; p = p.next) {
            a[k++] = (T)p.item;
         }

         return a;
      } finally {
         this.fullyUnlock();
      }
   }

   @Override
   public String toString() {
      this.fullyLock();

      try {
         return super.toString();
      } finally {
         this.fullyUnlock();
      }
   }

   @Override
   public void clear() {
      this.fullyLock();

      try {
         this.head.next = null;
         if (this.count.getAndSet(0) >= this.capacity) {
            this.notFull.signalAll();
         }
      } finally {
         this.fullyUnlock();
      }
   }

   @Override
   public int drainTo(Collection<? super E> c) {
      if (c == null) {
         throw new NullPointerException();
      }

      if (c == this) {
         throw new IllegalArgumentException();
      }

      this.fullyLock();

      VariableLinkedBlockingQueue.Node<E> first;
      try {
         first = this.head.next;
         this.head.next = null;
         if (this.count.getAndSet(0) >= this.capacity) {
            this.notFull.signalAll();
         }
      } finally {
         this.fullyUnlock();
      }

      int n = 0;

      for (VariableLinkedBlockingQueue.Node p = first; p != null; p = p.next) {
         c.add(p.item);
         p.item = null;
         n++;
      }

      return n;
   }

   @Override
   public int drainTo(Collection<? super E> c, int maxElements) {
      if (c == null) {
         throw new NullPointerException();
      }

      if (c == this) {
         throw new IllegalArgumentException();
      }

      if (maxElements <= 0) {
         return 0;
      }

      this.fullyLock();

      try {
         int n = 0;

         VariableLinkedBlockingQueue.Node<E> p;
         for (p = this.head.next; p != null && n < maxElements; n++) {
            c.add(p.item);
            p.item = null;
            p = p.next;
         }

         if (n != 0) {
            this.head.next = p;
            if (this.count.getAndAdd(-n) >= this.capacity) {
               this.notFull.signalAll();
            }
         }

         return n;
      } finally {
         this.fullyUnlock();
      }
   }

   @Override
   public Iterator<E> iterator() {
      return new VariableLinkedBlockingQueue.Itr();
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      this.fullyLock();

      try {
         s.defaultWriteObject();

         for (VariableLinkedBlockingQueue.Node<E> p = this.head.next; p != null; p = p.next) {
            s.writeObject(p.item);
         }

         s.writeObject(null);
      } finally {
         this.fullyUnlock();
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      this.count.set(0);
      this.last = this.head = new VariableLinkedBlockingQueue.Node<>(null);

      while (true) {
         E item = (E)s.readObject();
         if (item == null) {
            return;
         }

         this.add(item);
      }
   }

   private class Itr implements Iterator<E> {
      private VariableLinkedBlockingQueue.Node<E> current;
      private VariableLinkedBlockingQueue.Node<E> lastRet;
      private Object currentElement;

      Itr() {
         ReentrantLock putLock = VariableLinkedBlockingQueue.this.putLock;
         ReentrantLock takeLock = VariableLinkedBlockingQueue.this.takeLock;
         putLock.lock();
         takeLock.lock();

         try {
            this.current = VariableLinkedBlockingQueue.this.head.next;
            if (this.current != null) {
               this.currentElement = this.current.item;
            }
         } finally {
            takeLock.unlock();
            putLock.unlock();
         }
      }

      @Override
      public boolean hasNext() {
         return this.current != null;
      }

      @Override
      public E next() {
         ReentrantLock putLock = VariableLinkedBlockingQueue.this.putLock;
         ReentrantLock takeLock = VariableLinkedBlockingQueue.this.takeLock;
         putLock.lock();
         takeLock.lock();

         try {
            if (this.current == null) {
               throw new NoSuchElementException();
            }

            E x = (E)this.currentElement;
            this.lastRet = this.current;
            this.current = this.current.next;
            if (this.current != null) {
               this.currentElement = this.current.item;
            }

            return x;
         } finally {
            takeLock.unlock();
            putLock.unlock();
         }
      }

      @Override
      public void remove() {
         if (this.lastRet == null) {
            throw new IllegalStateException();
         }

         ReentrantLock putLock = VariableLinkedBlockingQueue.this.putLock;
         ReentrantLock takeLock = VariableLinkedBlockingQueue.this.takeLock;
         putLock.lock();
         takeLock.lock();

         try {
            VariableLinkedBlockingQueue.Node<E> node = this.lastRet;
            this.lastRet = null;
            VariableLinkedBlockingQueue.Node<E> trail = VariableLinkedBlockingQueue.this.head;

            VariableLinkedBlockingQueue.Node<E> p;
            for (p = VariableLinkedBlockingQueue.this.head.next; p != null && p != node; p = p.next) {
               trail = p;
            }

            if (p == node) {
               p.item = null;
               trail.next = p.next;
               int c = VariableLinkedBlockingQueue.this.count.getAndDecrement();
               if (c >= VariableLinkedBlockingQueue.this.capacity) {
                  VariableLinkedBlockingQueue.this.notFull.signalAll();
               }
            }
         } finally {
            takeLock.unlock();
            putLock.unlock();
         }
      }
   }

   static class Node<E> {
      volatile E item;
      VariableLinkedBlockingQueue.Node<E> next;

      Node(E x) {
         this.item = x;
      }
   }
}
