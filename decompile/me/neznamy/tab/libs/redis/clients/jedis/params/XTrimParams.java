package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;

public class XTrimParams implements IParams {
   private Long maxLen;
   private boolean approximateTrimming;
   private boolean exactTrimming;
   private String minId;
   private Long limit;

   public static XTrimParams xTrimParams() {
      return new XTrimParams();
   }

   public XTrimParams maxLen(long maxLen) {
      this.maxLen = maxLen;
      return this;
   }

   public XTrimParams minId(String minId) {
      this.minId = minId;
      return this;
   }

   public XTrimParams approximateTrimming() {
      this.approximateTrimming = true;
      return this;
   }

   public XTrimParams exactTrimming() {
      this.exactTrimming = true;
      return this;
   }

   public XTrimParams limit(long limit) {
      this.limit = limit;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.maxLen != null) {
         args.add(Protocol.Keyword.MAXLEN);
         if (this.approximateTrimming) {
            args.add(Protocol.BYTES_TILDE);
         } else if (this.exactTrimming) {
            args.add(Protocol.BYTES_EQUAL);
         }

         args.add(Protocol.toByteArray(this.maxLen));
      } else if (this.minId != null) {
         args.add(Protocol.Keyword.MINID);
         if (this.approximateTrimming) {
            args.add(Protocol.BYTES_TILDE);
         } else if (this.exactTrimming) {
            args.add(Protocol.BYTES_EQUAL);
         }

         args.add(this.minId);
      }

      if (this.limit != null) {
         args.add(Protocol.Keyword.LIMIT).add(this.limit);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         XTrimParams that = (XTrimParams)o;
         return this.approximateTrimming == that.approximateTrimming
            && this.exactTrimming == that.exactTrimming
            && Objects.equals(this.maxLen, that.maxLen)
            && Objects.equals(this.minId, that.minId)
            && Objects.equals(this.limit, that.limit);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.maxLen, this.approximateTrimming, this.exactTrimming, this.minId, this.limit);
   }
}
