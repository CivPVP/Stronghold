package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.cpu.ThreadExecutor;
import org.jetbrains.annotations.NotNull;

public interface CustomThreaded {
   @NotNull
   ThreadExecutor getCustomThread();
}
