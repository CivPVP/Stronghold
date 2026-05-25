package me.neznamy.tab.shared.util;

import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public class PerformanceUtil {
   private static final String[] intToString = IntStream.range(0, 1000).mapToObj(Integer::toString).toArray(String[]::new);

   @NotNull
   public static String toString(int i) {
      return i >= 0 && i < intToString.length ? intToString[i] : Integer.toString(i);
   }
}
