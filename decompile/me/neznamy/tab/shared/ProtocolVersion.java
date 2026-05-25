package me.neznamy.tab.shared;

import lombok.Generated;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

public enum ProtocolVersion {
   UNKNOWN,
   V1_21_11(774),
   V1_21_10(773),
   V1_21_9(773),
   V1_21_8(772),
   V1_21_7(772),
   V1_21_6(771),
   V1_21_5(770),
   V1_21_4(769),
   V1_21_3(768),
   V1_21_2(768),
   V1_21_1(767),
   V1_21(767),
   V1_20_6(766),
   V1_20_5(766),
   V1_20_4(765),
   V1_20_3(765),
   V1_20_2(764),
   V1_20_1(763),
   V1_20(763),
   V1_19_4(762),
   V1_19_3(761),
   V1_19_2(760),
   V1_19_1(760),
   V1_19(759),
   V1_18_2(758),
   V1_18_1(757),
   V1_18(757),
   V1_17_1(756),
   V1_17(755),
   V1_16_5(754),
   V1_16_4(754),
   V1_16_3(753),
   V1_16_2(751),
   V1_16_1(736),
   V1_16(735),
   V1_15_2(578),
   V1_15_1(575),
   V1_15(573),
   V1_14_4(498),
   V1_14_3(490),
   V1_14_2(485),
   V1_14_1(480),
   V1_14(477),
   V1_13_2(404),
   V1_13_1(401),
   V1_13(393),
   V1_12_2(340),
   V1_12_1(338),
   V1_12(335),
   V1_11_2(316),
   V1_11_1(316),
   V1_11(315),
   V1_10_2(210),
   V1_10_1(210),
   V1_10(210),
   V1_9_4(110),
   V1_9_3(110),
   V1_9_2(109),
   V1_9_1(108),
   V1_9(107),
   V1_8(47),
   V1_7_10(5),
   V1_7_9(5),
   V1_7_8(5),
   V1_7_7(5),
   V1_7_6(5),
   V1_7_5(4),
   V1_7_4(4),
   V1_7_2(4),
   V1_6_4(78),
   V1_6_2(74),
   V1_6_1(73),
   V1_5_2(61),
   V1_5_1(60),
   V1_5(60),
   V1_4_7(51),
   V1_4_6(51);

   private static final ProtocolVersion[] VALUES = values();
   private final int networkId;
   private final int minorVersion;
   @NotNull
   private final String friendlyName;

   ProtocolVersion(int networkId) {
      this.networkId = networkId;
      this.minorVersion = Integer.parseInt(this.toString().split("_")[1]);
      this.friendlyName = this.toString().substring(1).replace("_", ".");
   }

   ProtocolVersion() {
      this.networkId = 999;
      this.minorVersion = 99;
      this.friendlyName = "Unknown";
   }

   public boolean supportsRGB() {
      return this.minorVersion >= 16;
   }

   @NotNull
   public static ProtocolVersion fromFriendlyName(@NonNull String friendlyName) {
      if (friendlyName == null) {
         throw new NullPointerException("friendlyName is marked non-null but is null");
      }

      if (friendlyName.startsWith("1.8")) {
         return V1_8;
      }

      try {
         return valueOf("V" + friendlyName.replace(".", "_"));
      } catch (IllegalArgumentException e) {
         return UNKNOWN;
      }
   }

   @NotNull
   public static ProtocolVersion fromNetworkId(int networkId) {
      for (ProtocolVersion v : VALUES) {
         if (networkId == v.networkId) {
            return v;
         }
      }

      return UNKNOWN;
   }

   @Generated
   public int getNetworkId() {
      return this.networkId;
   }

   @Generated
   public int getMinorVersion() {
      return this.minorVersion;
   }

   @NotNull
   @Generated
   public String getFriendlyName() {
      return this.friendlyName;
   }
}
