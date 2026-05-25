package me.neznamy.tab.libs.redis.clients.jedis.gears;

import java.util.Collections;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

@Deprecated
public class TFunctionListParams implements IParams {
   private boolean withCode = false;
   private int verbose;
   private String libraryName;

   public static TFunctionListParams listParams() {
      return new TFunctionListParams();
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.withCode) {
         args.add(RedisGearsProtocol.GearsKeyword.WITHCODE);
      }

      if (this.verbose > 0 && this.verbose < 4) {
         args.add(String.join("", Collections.nCopies(this.verbose, "v")));
      } else if (this.verbose != 0) {
         throw new IllegalArgumentException("verbose must be between 1 and 3");
      }

      if (this.libraryName != null) {
         args.add(RedisGearsProtocol.GearsKeyword.LIBRARY).add(this.libraryName);
      }
   }

   public TFunctionListParams withCode() {
      this.withCode = true;
      return this;
   }

   public TFunctionListParams verbose(int verbose) {
      this.verbose = verbose;
      return this;
   }

   public TFunctionListParams library(String libraryName) {
      this.libraryName = libraryName;
      return this;
   }
}
