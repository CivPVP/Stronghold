package me.neznamy.tab.libs.com.rabbitmq.utility;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utility {
   public static <T extends Throwable & SensibleClone<T>> T fixStackTrace(T throwable) {
      throwable = (T)throwable.sensibleClone();
      if (throwable.getCause() == null) {
         try {
            throwable.initCause(new Utility.ThrowableCreatedElsewhere(throwable));
         } catch (IllegalStateException var3) {
         }
      }

      throwable.fillInStackTrace();
      StackTraceElement[] existing = throwable.getStackTrace();
      StackTraceElement[] newTrace = new StackTraceElement[existing.length - 1];
      System.arraycopy(existing, 1, newTrace, 0, newTrace.length);
      throwable.setStackTrace(newTrace);
      return throwable;
   }

   /** @deprecated */
   public static String makeStackTrace(Throwable throwable) {
      ByteArrayOutputStream baOutStream = new ByteArrayOutputStream();
      PrintStream printStream = new PrintStream(baOutStream, false);
      throwable.printStackTrace(printStream);
      printStream.flush();
      String text = baOutStream.toString();
      printStream.close();
      return text;
   }

   public static <E> Set<E> copy(Set<E> set) {
      synchronized (set) {
         return new LinkedHashSet<>(set);
      }
   }

   public static <E> List<E> copy(List<E> list) {
      synchronized (list) {
         return new ArrayList<>(list);
      }
   }

   public static <K, V> Map<K, V> copy(Map<K, V> map) {
      synchronized (map) {
         return new LinkedHashMap<>(map);
      }
   }

   static class ThrowableCreatedElsewhere extends Throwable {
      private static final long serialVersionUID = 1L;

      public ThrowableCreatedElsewhere(Throwable throwable) {
         super(throwable.getClass() + " created elsewhere");
         this.setStackTrace(throwable.getStackTrace());
      }

      @Override
      public synchronized Throwable fillInStackTrace() {
         return this;
      }
   }
}
