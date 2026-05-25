package me.neznamy.tab.shared.util.function;

@FunctionalInterface
public interface FunctionWithException<A, B> {
   B apply(A var1) throws Exception;
}
