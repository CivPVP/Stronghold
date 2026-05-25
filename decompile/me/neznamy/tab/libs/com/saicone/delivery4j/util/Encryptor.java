package me.neznamy.tab.libs.com.saicone.delivery4j.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.jetbrains.annotations.NotNull;

public interface Encryptor {
   @NotNull
   static Encryptor of(@NotNull SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
      return of("AES", key, StandardCharsets.UTF_8);
   }

   @NotNull
   static Encryptor of(@NotNull SecretKey key, @NotNull Charset charset) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
      return of("AES", key, charset);
   }

   @NotNull
   static Encryptor of(@NotNull String transformation, @NotNull SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
      return of(transformation, key, StandardCharsets.UTF_8);
   }

   @NotNull
   static Encryptor of(@NotNull final String transformation, @NotNull final SecretKey key, @NotNull final Charset charset) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
      final Cipher encryptMode = Cipher.getInstance(transformation);
      encryptMode.init(1, key);
      final Cipher decryptMode = Cipher.getInstance(transformation);
      decryptMode.init(2, key);
      return new Encryptor() {
         private Cipher encrypt = encryptMode;
         private Cipher decrypt = decryptMode;

         @Override
         public byte[] encrypt(@NotNull String input) {
            try {
               return this.encrypt.doFinal(input.getBytes(charset));
            } catch (Throwable t) {
               try {
                  this.encrypt = Cipher.getInstance(transformation);
                  this.encrypt.init(1, key);
               } catch (Exception var4) {
               }

               throw new RuntimeException(t);
            }
         }

         @NotNull
         @Override
         public String decrypt(byte[] input) {
            try {
               return new String(this.decrypt.doFinal(input), charset);
            } catch (Throwable t) {
               try {
                  this.decrypt = Cipher.getInstance(transformation);
                  this.decrypt.init(2, key);
               } catch (Exception var4) {
               }

               throw new RuntimeException(t);
            }
         }
      };
   }

   byte[] encrypt(@NotNull String var1);

   @NotNull
   String decrypt(byte[] var1);
}
