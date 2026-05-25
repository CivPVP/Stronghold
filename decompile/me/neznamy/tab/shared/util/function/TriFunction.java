package me.neznamy.tab.shared.util.function;

@FunctionalInterface
public interface TriFunction<A, B, C, D> {
   D apply(A var1, B var2, C var3);
}
