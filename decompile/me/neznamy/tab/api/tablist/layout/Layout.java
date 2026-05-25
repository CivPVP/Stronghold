package me.neznamy.tab.api.tablist.layout;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Layout {
   @NotNull
   String getName();

   void addFixedSlot(int var1, @NonNull String var2);

   void addFixedSlot(int var1, @NonNull String var2, @NonNull String var3);

   void addFixedSlot(int var1, @NonNull String var2, int var3);

   void addFixedSlot(int var1, @NonNull String var2, @NonNull String var3, int var4);

   void addGroup(@Nullable String var1, int[] var2);
}
