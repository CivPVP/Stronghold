package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.time.Duration;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

class LinkedBlockingDeque<E> extends AbstractQueue<E> implements Deque<E>, Serializable {
   private static final long serialVersionUID = -387911632671998426L;
   private transient LinkedBlockingDeque.Node<E> first;
   private transient LinkedBlockingDeque.Node<E> last;
   private transient int count;
   private final int capacity;
   private final InterruptibleReentrantLock lock;
   private final Condition notEmpty;
   private final Condition notFull;

   public LinkedBlockingDeque() {
      this(Integer.MAX_VALUE);
   }

   public LinkedBlockingDeque(boolean fairness) {
      this(Integer.MAX_VALUE, fairness);
   }

   public LinkedBlockingDeque(Collection<? extends E> c) {
      this(Integer.MAX_VALUE);
      this.lock.lock();

      try {
         for (E e : c) {
            Objects.requireNonNull(e);
            if (!this.linkLast(e)) {
               throw new IllegalStateException("Deque full");
            }
         }
      } finally {
         this.lock.unlock();
      }
   }

   public LinkedBlockingDeque(int capacity) {
      this(capacity, false);
   }

   public LinkedBlockingDeque(int capacity, boolean fairness) {
      if (capacity <= 0) {
         throw new IllegalArgumentException();
      }

      this.capacity = capacity;
      this.lock = new InterruptibleReentrantLock(fairness);
      this.notEmpty = this.lock.newCondition();
      this.notFull = this.lock.newCondition();
   }

   @Override
   public boolean add(E e) {
      this.addLast(e);
      return true;
   }

   @Override
   public void addFirst(E e) {
      if (!this.offerFirst(e)) {
         throw new IllegalStateException("Deque full");
      }
   }

   @Override
   public void addLast(E e) {
      if (!this.offerLast(e)) {
         throw new IllegalStateException("Deque full");
      }
   }

   @Override
   public void clear() {
      this.lock.lock();

      try {
         LinkedBlockingDeque.Node<E> f = this.first;

         while (f != null) {
            f.item = null;
            LinkedBlockingDeque.Node<E> n = f.next;
            f.prev = null;
            f.next = null;
            f = n;
         }

         this.first = this.last = null;
         this.count = 0;
         this.notFull.signalAll();
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public boolean contains(Object o) {
      if (o == null) {
         return false;
      }

      this.lock.lock();

      try {
         for (LinkedBlockingDeque.Node<E> p = this.first; p != null; p = p.next) {
            if (o.equals(p.item)) {
               return true;
            }
         }

         return false;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public Iterator<E> descendingIterator() {
      return new LinkedBlockingDeque.DescendingItr();
   }

   public int drainTo(Collection<? super E> c) {
      return this.drainTo(c, Integer.MAX_VALUE);
   }

   public int drainTo(Collection<? super E> c, int maxElements) {
      Objects.requireNonNull(c, "c");
      if (c == this) {
         throw new IllegalArgumentException();
      }

      this.lock.lock();

      try {
         int n = Math.min(maxElements, this.count);

         for (int i = 0; i < n; i++) {
            c.add(this.first.item);
            this.unlinkFirst();
         }

         return n;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public E element() {
      return this.getFirst();
   }

   @Override
   public E getFirst() {
      E x = this.peekFirst();
      if (x == null) {
         throw new NoSuchElementException();
      } else {
         return x;
      }
   }

   @Override
   public E getLast() {
      E x = this.peekLast();
      if (x == null) {
         throw new NoSuchElementException();
      } else {
         return x;
      }
   }

   public int getTakeQueueLength() {
      this.lock.lock();

      try {
         return this.lock.getWaitQueueLength(this.notEmpty);
      } finally {
         this.lock.unlock();
      }
   }

   public boolean hasTakeWaiters() {
      this.lock.lock();

      try {
         return this.lock.hasWaiters(this.notEmpty);
      } finally {
         this.lock.unlock();
      }
   }

   public void interuptTakeWaiters() {
      this.lock.lock();

      try {
         this.lock.interruptWaiters(this.notEmpty);
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public Iterator<E> iterator() {
      return new LinkedBlockingDeque.Itr();
   }

   private boolean linkFirst(E e) {
      if (this.count >= this.capacity) {
         return false;
      }

      LinkedBlockingDeque.Node<E> f = this.first;
      LinkedBlockingDeque.Node<E> x = new LinkedBlockingDeque.Node<>(e, null, f);
      this.first = x;
      if (this.last == null) {
         this.last = x;
      } else {
         f.prev = x;
      }

      this.count++;
      this.notEmpty.signal();
      return true;
   }

   private boolean linkLast(E e) {
      if (this.count >= this.capacity) {
         return false;
      }

      LinkedBlockingDeque.Node<E> l = this.last;
      LinkedBlockingDeque.Node<E> x = new LinkedBlockingDeque.Node<>(e, l, null);
      this.last = x;
      if (this.first == null) {
         this.first = x;
      } else {
         l.next = x;
      }

      this.count++;
      this.notEmpty.signal();
      return true;
   }

   @Override
   public boolean offer(E e) {
      return this.offerLast(e);
   }

   boolean offer(E e, Duration timeout) throws InterruptedException {
      return this.offerLast(e, timeout);
   }

   public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
      return this.offerLast(e, timeout, unit);
   }

   @Override
   public boolean offerFirst(E e) {
      Objects.requireNonNull(e, "e");
      this.lock.lock();

      try {
         return this.linkFirst(e);
      } finally {
         this.lock.unlock();
      }
   }

   public boolean offerFirst(E e, Duration timeout) throws InterruptedException {
      Objects.requireNonNull(e, "e");
      long nanos = timeout.toNanos();
      this.lock.lockInterruptibly();

      try {
         while (!this.linkFirst(e)) {
            if (nanos <= 0L) {
               return false;
            }

            nanos = this.notFull.awaitNanos(nanos);
         }

         return true;
      } finally {
         this.lock.unlock();
      }
   }

   public boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException {
      return this.offerFirst(e, PoolImplUtils.toDuration(timeout, unit));
   }

   @Override
   public boolean offerLast(E e) {
      Objects.requireNonNull(e, "e");
      this.lock.lock();

      try {
         return this.linkLast(e);
      } finally {
         this.lock.unlock();
      }
   }

   boolean offerLast(E e, Duration timeout) throws InterruptedException {
      Objects.requireNonNull(e, "e");
      long nanos = timeout.toNanos();
      this.lock.lockInterruptibly();

      try {
         while (!this.linkLast(e)) {
            if (nanos <= 0L) {
               return false;
            }

            nanos = this.notFull.awaitNanos(nanos);
         }

         return true;
      } finally {
         this.lock.unlock();
      }
   }

   public boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
      return this.offerLast(e, PoolImplUtils.toDuration(timeout, unit));
   }

   @Override
   public E peek() {
      return this.peekFirst();
   }

   @Override
   public E peekFirst() {
      this.lock.lock();

      try {
         return this.first == null ? null : this.first.item;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public E peekLast() {
      this.lock.lock();

      try {
         return this.last == null ? null : this.last.item;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public E poll() {
      return this.pollFirst();
   }

   E poll(Duration timeout) throws InterruptedException {
      return this.pollFirst(timeout);
   }

   public E poll(long timeout, TimeUnit unit) throws InterruptedException {
      return this.pollFirst(timeout, unit);
   }

   @Override
   public E pollFirst() {
      this.lock.lock();

      try {
         return this.unlinkFirst();
      } finally {
         this.lock.unlock();
      }
   }

   E pollFirst(Duration timeout) throws InterruptedException {
      long nanos = timeout.toNanos();
      this.lock.lockInterruptibly();

      try {
         E x;
         while ((x = this.unlinkFirst()) == null) {
            if (nanos <= 0L) {
               return null;
            }

            nanos = this.notEmpty.awaitNanos(nanos);
         }

         return x;
      } finally {
         this.lock.unlock();
      }
   }

   public E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
      return this.pollFirst(PoolImplUtils.toDuration(timeout, unit));
   }

   @Override
   public E pollLast() {
      this.lock.lock();

      try {
         return this.unlinkLast();
      } finally {
         this.lock.unlock();
      }
   }

   public E pollLast(Duration timeout) throws InterruptedException {
      long nanos = timeout.toNanos();
      this.lock.lockInterruptibly();

      try {
         E x;
         while ((x = this.unlinkLast()) == null) {
            if (nanos <= 0L) {
               return null;
            }

            nanos = this.notEmpty.awaitNanos(nanos);
         }

         return x;
      } finally {
         this.lock.unlock();
      }
   }

   public E pollLast(long timeout, TimeUnit unit) throws InterruptedException {
      return this.pollLast(PoolImplUtils.toDuration(timeout, unit));
   }

   @Override
   public E pop() {
      return this.removeFirst();
   }

   @Override
   public void push(E e) {
      this.addFirst(e);
   }

   public void put(E e) throws InterruptedException {
      this.putLast(e);
   }

   public void putFirst(E e) throws InterruptedException {
      Objects.requireNonNull(e, "e");
      this.lock.lock();

      try {
         while (!this.linkFirst(e)) {
            this.notFull.await();
         }
      } finally {
         this.lock.unlock();
      }
   }

   public void putLast(E e) throws InterruptedException {
      Objects.requireNonNull(e, "e");
      this.lock.lock();

      try {
         while (!this.linkLast(e)) {
            this.notFull.await();
         }
      } finally {
         this.lock.unlock();
      }
   }

   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
      s.defaultReadObject();
      this.count = 0;
      this.first = null;
      this.last = null;

      while (true) {
         E item = (E)s.readObject();
         if (item == null) {
            return;
         }

         this.add(item);
      }
   }

   public int remainingCapacity() {
      this.lock.lock();

      try {
         return this.capacity - this.count;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public E remove() {
      return this.removeFirst();
   }

   @Override
   public boolean remove(Object o) {
      return this.removeFirstOccurrence(o);
   }

   @Override
   public E removeFirst() {
      E x = this.pollFirst();
      if (x == null) {
         throw new NoSuchElementException();
      } else {
         return x;
      }
   }

   @Override
   public boolean removeFirstOccurrence(Object o) {
      if (o == null) {
         return false;
      }

      this.lock.lock();

      try {
         for (LinkedBlockingDeque.Node<E> p = this.first; p != null; p = p.next) {
            if (o.equals(p.item)) {
               this.unlink(p);
               return true;
            }
         }

         return false;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public E removeLast() {
      E x = this.pollLast();
      if (x == null) {
         throw new NoSuchElementException();
      } else {
         return x;
      }
   }

   @Override
   public boolean removeLastOccurrence(Object o) {
      if (o == null) {
         return false;
      }

      this.lock.lock();

      try {
         for (LinkedBlockingDeque.Node<E> p = this.last; p != null; p = p.prev) {
            if (o.equals(p.item)) {
               this.unlink(p);
               return true;
            }
         }

         return false;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public int size() {
      this.lock.lock();

      try {
         return this.count;
      } finally {
         this.lock.unlock();
      }
   }

   public E take() throws InterruptedException {
      return this.takeFirst();
   }

   public E takeFirst() throws InterruptedException {
      this.lock.lock();

      try {
         E x;
         while ((x = this.unlinkFirst()) == null) {
            this.notEmpty.await();
         }

         return x;
      } finally {
         this.lock.unlock();
      }
   }

   public E takeLast() throws InterruptedException {
      this.lock.lock();

      try {
         E x;
         while ((x = this.unlinkLast()) == null) {
            this.notEmpty.await();
         }

         return x;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public Object[] toArray() {
      this.lock.lock();

      try {
         Object[] a = new Object[this.count];
         int k = 0;

         for (LinkedBlockingDeque.Node<E> p = this.first; p != null; p = p.next) {
            a[k++] = p.item;
         }

         return a;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public <T> T[] toArray(T[] a) {
      this.lock.lock();

      try {
         if (a.length < this.count) {
            a = (T[])Array.newInstance(a.getClass().getComponentType(), this.count);
         }

         int k = 0;

         for (LinkedBlockingDeque.Node<E> p = this.first; p != null; p = p.next) {
            a[k++] = (T)p.item;
         }

         if (a.length > k) {
            a[k] = null;
         }

         return a;
      } finally {
         this.lock.unlock();
      }
   }

   @Override
   public String toString() {
      this.lock.lock();

      try {
         return super.toString();
      } finally {
         this.lock.unlock();
      }
   }

   private void unlink(LinkedBlockingDeque.Node<E> x) {
      LinkedBlockingDeque.Node<E> p = x.prev;
      LinkedBlockingDeque.Node<E> n = x.next;
      if (p == null) {
         this.unlinkFirst();
      } else if (n == null) {
         this.unlinkLast();
      } else {
         p.next = n;
         n.prev = p;
         x.item = null;
         this.count--;
         this.notFull.signal();
      }
   }

   private E unlinkFirst() {
      LinkedBlockingDeque.Node<E> f = this.first;
      if (f == null) {
         return null;
      }

      LinkedBlockingDeque.Node<E> n = f.next;
      E item = f.item;
      f.item = null;
      f.next = f;
      this.first = n;
      if (n == null) {
         this.last = null;
      } else {
         n.prev = null;
      }

      this.count--;
      this.notFull.signal();
      return item;
   }

   private E unlinkLast() {
      LinkedBlockingDeque.Node<E> l = this.last;
      if (l == null) {
         return null;
      }

      LinkedBlockingDeque.Node<E> p = l.prev;
      E item = l.item;
      l.item = null;
      l.prev = l;
      this.last = p;
      if (p == null) {
         this.first = null;
      } else {
         p.next = null;
      }

      this.count--;
      this.notFull.signal();
      return item;
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      this.lock.lock();

      try {
         s.defaultWriteObject();

         for (LinkedBlockingDeque.Node<E> p = this.first; p != null; p = p.next) {
            s.writeObject(p.item);
         }

         s.writeObject(null);
      } finally {
         this.lock.unlock();
      }
   }

   private abstract class AbstractItr implements Iterator<E> {
      LinkedBlockingDeque.Node<E> next;
      Object nextItem;
      private LinkedBlockingDeque.Node<E> lastRet;

      AbstractItr() {
         LinkedBlockingDeque.this.lock.lock();

         try {
            this.next = this.firstNode();
            this.nextItem = this.next == null ? null : this.next.item;
         } finally {
            LinkedBlockingDeque.this.lock.unlock();
         }
      }

      void advance() {
         LinkedBlockingDeque.this.lock.lock();

         try {
            this.next = this.succ(this.next);
            this.nextItem = this.next == null ? null : this.next.item;
         } finally {
            LinkedBlockingDeque.this.lock.unlock();
         }
      }

      abstract LinkedBlockingDeque.Node<E> firstNode();

      @Override
      public boolean hasNext() {
         return this.next != null;
      }

      @Override
      public E next() {
         if (this.next == null) {
            throw new NoSuchElementException();
         }

         this.lastRet = this.next;
         E x = (E)this.nextItem;
         this.advance();
         return x;
      }

      abstract LinkedBlockingDeque.Node<E> nextNode(LinkedBlockingDeque.Node<E> var1);

      @Override
      public void remove() {
         LinkedBlockingDeque.Node<E> n = this.lastRet;
         if (n == null) {
            throw new IllegalStateException();
         }

         this.lastRet = null;
         LinkedBlockingDeque.this.lock.lock();

         try {
            if (n.item != null) {
               LinkedBlockingDeque.this.unlink(n);
            }
         } finally {
            LinkedBlockingDeque.this.lock.unlock();
         }
      }

      private LinkedBlockingDeque.Node<E> succ(LinkedBlockingDeque.Node<E> n) {
         while (true) {
            LinkedBlockingDeque.Node<E> s = this.nextNode(n);
            if (s == null) {
               return null;
            }

            if (s.item != null) {
               return s;
            }

            if (s == n) {
               return this.firstNode();
            }

            n = s;
         }
      }
   }

   private class DescendingItr extends LinkedBlockingDeque<E>.AbstractItr {
      private DescendingItr() {
      }

      @Override
      LinkedBlockingDeque.Node<E> firstNode() {
         return LinkedBlockingDeque.this.last;
      }

      @Override
      LinkedBlockingDeque.Node<E> nextNode(LinkedBlockingDeque.Node<E> n) {
         return n.prev;
      }
   }

   private class Itr extends LinkedBlockingDeque<E>.AbstractItr {
      private Itr() {
      }

      @Override
      LinkedBlockingDeque.Node<E> firstNode() {
         return LinkedBlockingDeque.this.first;
      }

      @Override
      LinkedBlockingDeque.Node<E> nextNode(LinkedBlockingDeque.Node<E> n) {
         return n.next;
      }
   }

   private static final class Node<E> {
      E item;
      LinkedBlockingDeque.Node<E> prev;
      LinkedBlockingDeque.Node<E> next;

      Node(E x, LinkedBlockingDeque.Node<E> p, LinkedBlockingDeque.Node<E> n) {
         this.item = x;
         this.prev = p;
         this.next = n;
      }
   }
}
