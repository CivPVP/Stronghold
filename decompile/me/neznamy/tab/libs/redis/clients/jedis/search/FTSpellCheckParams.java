package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.args.Rawable;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class FTSpellCheckParams implements IParams {
   private Collection<Entry<String, Rawable>> terms;
   private Integer distance;
   private Integer dialect;

   public static FTSpellCheckParams spellCheckParams() {
      return new FTSpellCheckParams();
   }

   public FTSpellCheckParams includeTerm(String dictionary) {
      return this.addTerm(dictionary, SearchProtocol.SearchKeyword.INCLUDE);
   }

   public FTSpellCheckParams excludeTerm(String dictionary) {
      return this.addTerm(dictionary, SearchProtocol.SearchKeyword.EXCLUDE);
   }

   private FTSpellCheckParams addTerm(String dictionary, Rawable type) {
      if (this.terms == null) {
         this.terms = new ArrayList<>();
      }

      this.terms.add(KeyValue.of(dictionary, type));
      return this;
   }

   public FTSpellCheckParams distance(int distance) {
      this.distance = distance;
      return this;
   }

   public FTSpellCheckParams dialect(int dialect) {
      this.dialect = dialect;
      return this;
   }

   public FTSpellCheckParams dialectOptional(int dialect) {
      if (dialect != 0 && this.dialect == null) {
         this.dialect = dialect;
      }

      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.terms != null) {
         this.terms.forEach(kv -> args.add(SearchProtocol.SearchKeyword.TERMS).add(kv.getValue()).add(kv.getKey()));
      }

      if (this.distance != null) {
         args.add(SearchProtocol.SearchKeyword.DISTANCE).add(this.distance);
      }

      if (this.dialect != null) {
         args.add(SearchProtocol.SearchKeyword.DIALECT).add(this.dialect);
      }
   }
}
