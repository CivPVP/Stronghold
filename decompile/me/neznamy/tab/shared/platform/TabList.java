package me.neznamy.tab.shared.platform;

import java.util.UUID;
import lombok.Generated;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import org.jetbrains.annotations.Nullable;

public interface TabList {
   String TEXTURES_PROPERTY = "textures";

   void removeEntry(@NonNull UUID var1);

   void updateDisplayName(@NonNull UUID var1, @Nullable TabComponent var2);

   void updateDisplayName(@NonNull TabPlayer var1, @Nullable TabComponent var2);

   void updateLatency(@NonNull UUID var1, int var2);

   void updateLatency(@NonNull TabPlayer var1, int var2);

   void updateGameMode(@NonNull UUID var1, int var2);

   void updateGameMode(@NonNull TabPlayer var1, int var2);

   void updateListed(@NonNull UUID var1, boolean var2);

   void updateListOrder(@NonNull UUID var1, int var2);

   void updateHat(@NonNull UUID var1, boolean var2);

   void addEntry(@NonNull TabList.Entry var1);

   boolean containsEntry(@NonNull UUID var1);

   void setPlayerListHeaderFooter(@Nullable TabComponent var1, @Nullable TabComponent var2);

   @Nullable
   TabList.Skin getSkin();

   void blockSpectator(@NonNull TabPlayer var1);

   void unblockSpectator(@NonNull TabPlayer var1);

   class Entry {
      @NonNull
      private UUID uniqueId;
      @NonNull
      private String name;
      @Nullable
      private TabList.Skin skin;
      private boolean listed;
      private int latency;
      private int gameMode;
      @Nullable
      private TabComponent displayName;
      private int listOrder;
      private boolean showHat;

      @NonNull
      @Generated
      public UUID getUniqueId() {
         return this.uniqueId;
      }

      @NonNull
      @Generated
      public String getName() {
         return this.name;
      }

      @Nullable
      @Generated
      public TabList.Skin getSkin() {
         return this.skin;
      }

      @Generated
      public boolean isListed() {
         return this.listed;
      }

      @Generated
      public int getLatency() {
         return this.latency;
      }

      @Generated
      public int getGameMode() {
         return this.gameMode;
      }

      @Nullable
      @Generated
      public TabComponent getDisplayName() {
         return this.displayName;
      }

      @Generated
      public int getListOrder() {
         return this.listOrder;
      }

      @Generated
      public boolean isShowHat() {
         return this.showHat;
      }

      @Generated
      public void setUniqueId(@NonNull UUID uniqueId) {
         if (uniqueId == null) {
            throw new NullPointerException("uniqueId is marked non-null but is null");
         }

         this.uniqueId = uniqueId;
      }

      @Generated
      public void setName(@NonNull String name) {
         if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
         }

         this.name = name;
      }

      @Generated
      public void setSkin(@Nullable TabList.Skin skin) {
         this.skin = skin;
      }

      @Generated
      public void setListed(boolean listed) {
         this.listed = listed;
      }

      @Generated
      public void setLatency(int latency) {
         this.latency = latency;
      }

      @Generated
      public void setGameMode(int gameMode) {
         this.gameMode = gameMode;
      }

      @Generated
      public void setDisplayName(@Nullable TabComponent displayName) {
         this.displayName = displayName;
      }

      @Generated
      public void setListOrder(int listOrder) {
         this.listOrder = listOrder;
      }

      @Generated
      public void setShowHat(boolean showHat) {
         this.showHat = showHat;
      }

      @Generated
      public Entry(
         @NonNull UUID uniqueId,
         @NonNull String name,
         @Nullable TabList.Skin skin,
         boolean listed,
         int latency,
         int gameMode,
         @Nullable TabComponent displayName,
         int listOrder,
         boolean showHat
      ) {
         if (uniqueId == null) {
            throw new NullPointerException("uniqueId is marked non-null but is null");
         }

         if (name == null) {
            throw new NullPointerException("name is marked non-null but is null");
         }

         this.uniqueId = uniqueId;
         this.name = name;
         this.skin = skin;
         this.listed = listed;
         this.latency = latency;
         this.gameMode = gameMode;
         this.displayName = displayName;
         this.listOrder = listOrder;
         this.showHat = showHat;
      }
   }

   class Skin {
      @NonNull
      private final String value;
      @Nullable
      private final String signature;

      @NonNull
      @Generated
      public String getValue() {
         return this.value;
      }

      @Nullable
      @Generated
      public String getSignature() {
         return this.signature;
      }

      @Generated
      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof TabList.Skin)) {
            return false;
         } else {
            TabList.Skin other = (TabList.Skin)o;
            if (!other.canEqual(this)) {
               return false;
            } else {
               Object this$value = this.getValue();
               Object other$value = other.getValue();
               if (this$value == null ? other$value == null : this$value.equals(other$value)) {
                  Object this$signature = this.getSignature();
                  Object other$signature = other.getSignature();
                  return this$signature == null ? other$signature == null : this$signature.equals(other$signature);
               } else {
                  return false;
               }
            }
         }
      }

      @Generated
      protected boolean canEqual(Object other) {
         return other instanceof TabList.Skin;
      }

      @Generated
      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         Object $value = this.getValue();
         result = result * 59 + ($value == null ? 43 : $value.hashCode());
         Object $signature = this.getSignature();
         return result * 59 + ($signature == null ? 43 : $signature.hashCode());
      }

      @Generated
      @Override
      public String toString() {
         return "TabList.Skin(value=" + this.getValue() + ", signature=" + this.getSignature() + ")";
      }

      @Generated
      public Skin(@NonNull String value, @Nullable String signature) {
         if (value == null) {
            throw new NullPointerException("value is marked non-null but is null");
         }

         this.value = value;
         this.signature = signature;
      }
   }
}
