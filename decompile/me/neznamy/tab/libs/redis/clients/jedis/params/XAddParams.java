package me.neznamy.tab.libs.redis.clients.jedis.params;

import java.util.Objects;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.StreamEntryID;
import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.args.RawableFactory;

public class XAddParams implements IParams {
   private Rawable id;
   private Long maxLen;
   private boolean approximateTrimming;
   private boolean exactTrimming;
   private boolean nomkstream;
   private String minId;
   private Long limit;

   public static XAddParams xAddParams() {
      return new XAddParams();
   }

   public XAddParams noMkStream() {
      this.nomkstream = true;
      return this;
   }

   public XAddParams id(byte[] id) {
      this.id = RawableFactory.from(id);
      return this;
   }

   public XAddParams id(String id) {
      this.id = RawableFactory.from(id);
      return this;
   }

   public XAddParams id(StreamEntryID id) {
      return this.id(id.toString());
   }

   public XAddParams id(long time, long sequence) {
      return this.id(time + "-" + sequence);
   }

   public XAddParams id(long time) {
      return this.id(time + "-*");
   }

   public XAddParams maxLen(long maxLen) {
      this.maxLen = maxLen;
      return this;
   }

   public XAddParams minId(String minId) {
      this.minId = minId;
      return this;
   }

   public XAddParams approximateTrimming() {
      this.approximateTrimming = true;
      return this;
   }

   public XAddParams exactTrimming() {
      this.exactTrimming = true;
      return this;
   }

   public XAddParams limit(long limit) {
      this.limit = limit;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.nomkstream) {
         args.add(Protocol.Keyword.NOMKSTREAM);
      }

      if (this.maxLen != null) {
         args.add(Protocol.Keyword.MAXLEN);
         if (this.approximateTrimming) {
            args.add(Protocol.BYTES_TILDE);
         } else if (this.exactTrimming) {
            args.add(Protocol.BYTES_EQUAL);
         }

         args.add(this.maxLen);
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

      args.add(this.id != null ? this.id : StreamEntryID.NEW_ENTRY);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         XAddParams that = (XAddParams)o;
         return this.approximateTrimming == that.approximateTrimming
            && this.exactTrimming == that.exactTrimming
            && this.nomkstream == that.nomkstream
            && Objects.equals(this.id, that.id)
            && Objects.equals(this.maxLen, that.maxLen)
            && Objects.equals(this.minId, that.minId)
            && Objects.equals(this.limit, that.limit);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.maxLen, this.approximateTrimming, this.exactTrimming, this.nomkstream, this.minId, this.limit);
   }
}
