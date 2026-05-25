package me.neznamy.tab.libs.redis.clients.jedis;

import java.util.Arrays;
import java.util.HashSet;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisValidationException;

public final class ClientSetInfoConfig {
   private final boolean disabled;
   private final String libNameSuffix;
   private static final HashSet<Character> BRACES = new HashSet<>(Arrays.asList('(', ')', '[', ']', '{', '}'));
   public static final ClientSetInfoConfig DEFAULT = new ClientSetInfoConfig();
   public static final ClientSetInfoConfig DISABLED = new ClientSetInfoConfig(true);

   public ClientSetInfoConfig() {
      this(false, null);
   }

   public ClientSetInfoConfig(boolean disabled) {
      this(disabled, null);
   }

   public ClientSetInfoConfig(String libNameSuffix) {
      this(false, libNameSuffix);
   }

   private ClientSetInfoConfig(boolean disabled, String libNameSuffix) {
      this.disabled = disabled;
      this.libNameSuffix = validateLibNameSuffix(libNameSuffix);
   }

   private static String validateLibNameSuffix(String suffix) {
      if (suffix != null && !suffix.trim().isEmpty()) {
         for (int i = 0; i < suffix.length(); i++) {
            char c = suffix.charAt(i);
            if (c < ' ' || c > '~' || BRACES.contains(c)) {
               throw new JedisValidationException("lib-name suffix cannot contain braces, newlines or special characters.");
            }
         }

         return suffix.replaceAll("\\s", "-");
      } else {
         return null;
      }
   }

   public final boolean isDisabled() {
      return this.disabled;
   }

   public final String getLibNameSuffix() {
      return this.libNameSuffix;
   }

   public static ClientSetInfoConfig withLibNameSuffix(String suffix) {
      return new ClientSetInfoConfig(suffix);
   }
}
