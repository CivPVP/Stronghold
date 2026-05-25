package me.neznamy.tab.libs.redis.clients.jedis.search;

import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class IndexDefinition implements IParams {
   private final IndexDefinition.Type type;
   private String[] prefixes;
   private String filter;
   private String languageField;
   private String language;
   private String scoreFiled;
   private double score = 1.0;

   public IndexDefinition() {
      this(null);
   }

   public IndexDefinition(IndexDefinition.Type type) {
      this.type = type;
   }

   public IndexDefinition.Type getType() {
      return this.type;
   }

   public String[] getPrefixes() {
      return this.prefixes;
   }

   public IndexDefinition setPrefixes(String... prefixes) {
      this.prefixes = prefixes;
      return this;
   }

   public String getFilter() {
      return this.filter;
   }

   public IndexDefinition setFilter(String filter) {
      this.filter = filter;
      return this;
   }

   public String getLanguageField() {
      return this.languageField;
   }

   public IndexDefinition setLanguageField(String languageField) {
      this.languageField = languageField;
      return this;
   }

   public String getLanguage() {
      return this.language;
   }

   public IndexDefinition setLanguage(String language) {
      this.language = language;
      return this;
   }

   public String getScoreFiled() {
      return this.scoreFiled;
   }

   public IndexDefinition setScoreFiled(String scoreFiled) {
      this.scoreFiled = scoreFiled;
      return this;
   }

   public double getScore() {
      return this.score;
   }

   public IndexDefinition setScore(double score) {
      this.score = score;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.type != null) {
         args.add(SearchProtocol.SearchKeyword.ON.name());
         args.add(this.type.name());
      }

      if (this.prefixes != null && this.prefixes.length > 0) {
         args.add(SearchProtocol.SearchKeyword.PREFIX.name());
         args.add(Integer.toString(this.prefixes.length));
         args.addObjects(this.prefixes);
      }

      if (this.filter != null) {
         args.add(SearchProtocol.SearchKeyword.FILTER.name());
         args.add(this.filter);
      }

      if (this.languageField != null) {
         args.add(SearchProtocol.SearchKeyword.LANGUAGE_FIELD.name());
         args.add(this.languageField);
      }

      if (this.language != null) {
         args.add(SearchProtocol.SearchKeyword.LANGUAGE.name());
         args.add(this.language);
      }

      if (this.scoreFiled != null) {
         args.add(SearchProtocol.SearchKeyword.SCORE_FIELD.name());
         args.add(this.scoreFiled);
      }

      if (this.score != 1.0) {
         args.add(SearchProtocol.SearchKeyword.SCORE.name());
         args.add(Double.toString(this.score));
      }
   }

   public enum Type {
      HASH,
      JSON;
   }
}
