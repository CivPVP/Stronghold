package me.neznamy.tab.libs.com.rabbitmq.utility;

import java.util.BitSet;

public class IntAllocator {
   private final int loRange;
   private final int hiRange;
   private final int numberOfBits;
   private int lastIndex = 0;
   private final BitSet freeSet;

   public IntAllocator(int bottom, int top) {
      this.loRange = bottom;
      this.hiRange = top + 1;
      this.numberOfBits = this.hiRange - this.loRange;
      this.freeSet = new BitSet(this.numberOfBits);
      this.freeSet.set(0, this.numberOfBits);
   }

   public int allocate() {
      int setIndex = this.freeSet.nextSetBit(this.lastIndex);
      if (setIndex < 0) {
         setIndex = this.freeSet.nextSetBit(0);
      }

      if (setIndex < 0) {
         return -1;
      }

      this.lastIndex = setIndex;
      this.freeSet.clear(setIndex);
      return setIndex + this.loRange;
   }

   public void free(int reservation) {
      this.freeSet.set(reservation - this.loRange);
   }

   public boolean reserve(int reservation) {
      int index = reservation - this.loRange;
      if (this.freeSet.get(index)) {
         this.freeSet.clear(index);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("IntAllocator{allocated = [");
      int firstClearBit = this.freeSet.nextClearBit(0);
      if (firstClearBit < this.numberOfBits) {
         int firstSetAfterThat = this.freeSet.nextSetBit(firstClearBit + 1);
         if (firstSetAfterThat < 0) {
            firstSetAfterThat = this.numberOfBits;
         }

         this.stringInterval(sb, firstClearBit, firstSetAfterThat);
         int i = this.freeSet.nextClearBit(firstSetAfterThat + 1);

         while (i < this.numberOfBits) {
            int nextSet = this.freeSet.nextSetBit(i);
            if (nextSet < 0) {
               nextSet = this.numberOfBits;
            }

            this.stringInterval(sb.append(", "), i, nextSet);
            i = nextSet;
            i = this.freeSet.nextClearBit(i + 1);
         }
      }

      sb.append("]}");
      return sb.toString();
   }

   private void stringInterval(StringBuilder sb, int i1, int i2) {
      sb.append(i1 + this.loRange);
      if (i1 + 1 != i2) {
         sb.append("..").append(i2 - 1 + this.loRange);
      }
   }
}
