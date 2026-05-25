package me.neznamy.tab.libs.com.rabbitmq.client.impl;

public class Version {
   private final int _major;
   private final int _minor;

   public Version(int major, int minor) {
      this._major = major;
      this._minor = minor;
   }

   public int getMajor() {
      return this._major;
   }

   public int getMinor() {
      return this._minor;
   }

   @Override
   public String toString() {
      return "" + this.getMajor() + "-" + this.getMinor();
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof Version)) {
         return false;
      }

      Version other = (Version)o;
      return this.getMajor() == other.getMajor() && this.getMinor() == other.getMinor();
   }

   @Override
   public int hashCode() {
      return 31 * this.getMajor() + this.getMinor();
   }

   public Version adjust() {
      return this.getMajor() == 8 && this.getMinor() == 0 ? new Version(0, 8) : this;
   }

   public static boolean checkVersion(Version clientVersion, Version serverVersion) {
      return clientVersion.adjust().equals(serverVersion.adjust());
   }
}
