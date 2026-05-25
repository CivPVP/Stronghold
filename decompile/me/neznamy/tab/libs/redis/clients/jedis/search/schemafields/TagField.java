package me.neznamy.tab.libs.redis.clients.jedis.search.schemafields;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.search.FieldName;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchProtocol;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class TagField extends SchemaField {
   private boolean indexMissing;
   private boolean indexEmpty;
   private byte[] separator;
   private boolean caseSensitive;
   private boolean withSuffixTrie;
   private boolean sortable;
   private boolean sortableUNF;
   private boolean noIndex;

   public TagField(String fieldName) {
      super(fieldName);
   }

   public TagField(FieldName fieldName) {
      super(fieldName);
   }

   public static TagField of(String fieldName) {
      return new TagField(fieldName);
   }

   public static TagField of(FieldName fieldName) {
      return new TagField(fieldName);
   }

   public TagField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public TagField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   public TagField indexEmpty() {
      this.indexEmpty = true;
      return this;
   }

   public TagField separator(char separator) {
      if (separator < 128) {
         this.separator = new byte[]{(byte)separator};
      } else {
         this.separator = SafeEncoder.encode(String.valueOf(separator));
      }

      return this;
   }

   public TagField caseSensitive() {
      this.caseSensitive = true;
      return this;
   }

   public TagField withSuffixTrie() {
      this.withSuffixTrie = true;
      return this;
   }

   public TagField sortable() {
      this.sortable = true;
      return this;
   }

   public TagField sortableUNF() {
      this.sortableUNF = true;
      return this;
   }

   @Deprecated
   public TagField sortableUnNormalizedForm() {
      return this.sortableUNF();
   }

   public TagField noIndex() {
      this.noIndex = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName);
      args.add(SearchProtocol.SearchKeyword.TAG);
      if (this.indexMissing) {
         args.add(SearchProtocol.SearchKeyword.INDEXMISSING);
      }

      if (this.indexEmpty) {
         args.add(SearchProtocol.SearchKeyword.INDEXEMPTY);
      }

      if (this.separator != null) {
         args.add(SearchProtocol.SearchKeyword.SEPARATOR).add(this.separator);
      }

      if (this.caseSensitive) {
         args.add(SearchProtocol.SearchKeyword.CASESENSITIVE);
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
