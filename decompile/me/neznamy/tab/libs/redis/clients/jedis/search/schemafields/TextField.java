package me.neznamy.tab.libs.redis.clients.jedis.search.schemafields;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.search.FieldName;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchProtocol;

public class TextField extends SchemaField {
   private boolean indexMissing;
   private boolean indexEmpty;
   private Double weight;
   private boolean noStem;
   private String phoneticMatcher;
   private boolean withSuffixTrie;
   private boolean sortable;
   private boolean sortableUNF;
   private boolean noIndex;

   public TextField(String fieldName) {
      super(fieldName);
   }

   public TextField(FieldName fieldName) {
      super(fieldName);
   }

   public static TextField of(String fieldName) {
      return new TextField(fieldName);
   }

   public static TextField of(FieldName fieldName) {
      return new TextField(fieldName);
   }

   public TextField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public TextField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   public TextField indexEmpty() {
      this.indexEmpty = true;
      return this;
   }

   public TextField weight(double weight) {
      this.weight = weight;
      return this;
   }

   public TextField noStem() {
      this.noStem = true;
      return this;
   }

   public TextField phonetic(String matcher) {
      this.phoneticMatcher = matcher;
      return this;
   }

   public TextField withSuffixTrie() {
      this.withSuffixTrie = true;
      return this;
   }

   public TextField sortable() {
      this.sortable = true;
      return this;
   }

   public TextField sortableUNF() {
      this.sortableUNF = true;
      return this;
   }

   @Deprecated
   public TextField sortableUnNormalizedForm() {
      return this.sortableUNF();
   }

   public TextField noIndex() {
      this.noIndex = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName);
      args.add(SearchProtocol.SearchKeyword.TEXT);
      if (this.indexMissing) {
         args.add(SearchProtocol.SearchKeyword.INDEXMISSING);
      }

      if (this.indexEmpty) {
         args.add(SearchProtocol.SearchKeyword.INDEXEMPTY);
      }

      if (this.weight != null) {
         args.add(SearchProtocol.SearchKeyword.WEIGHT).add(this.weight);
      }

      if (this.noStem) {
         args.add(SearchProtocol.SearchKeyword.NOSTEM);
      }

      if (this.phoneticMatcher != null) {
         args.add(SearchProtocol.SearchKeyword.PHONETIC).add(this.phoneticMatcher);
      }

      if (this.withSuffixTrie) {
         args.add(SearchProtocol.SearchKeyword.WITHSUFFIXTRIE);
      }

      if (this.sortableUNF) {
         args.add(SearchProtocol.SearchKeyword.SORTABLE).add(SearchProtocol.SearchKeyword.UNF);
      } else if (this.sortable) {
         args.add(SearchProtocol.SearchKeyword.SORTABLE);
      }

      if (this.noIndex) {
         args.add(SearchProtocol.SearchKeyword.NOINDEX);
      }
   }
}
