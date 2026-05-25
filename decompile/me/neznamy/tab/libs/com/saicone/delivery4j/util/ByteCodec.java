package me.neznamy.tab.libs.com.saicone.delivery4j.util;

import java.util.Base64;
import org.jetbrains.annotations.NotNull;

public interface ByteCodec<T> {
   ByteCodec<String> BASE64 = new ByteCodec<String>() {
      @NotNull
      public String encode(byte[] src) {
         return Base64.getEncoder().encodeToString(src);
      }

      public byte[] decode(@NotNull String src) {
         return Base64.getDecoder().decode(src);
      }
   };

   @NotNull
   T encode(byte[] var1);

   byte[] decode(@NotNull T var1);
}
