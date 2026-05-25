package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.annots.Internal;
import me.neznamy.tab.libs.redis.clients.jedis.args.GeoUnit;
import me.neznamy.tab.libs.redis.clients.jedis.args.SortingOrder;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.LazyRawable;

public class FTSearchParams implements IParams {
   private boolean noContent = false;
   private boolean verbatim = false;
   private boolean noStopwords = false;
   private boolean withScores = false;
   private final List<IParams> filters = new LinkedList<>();
   private Collection<String> inKeys;
   private Collection<String> inFields;
   private Collection<FieldName> returnFieldsNames;
   private boolean summarize;
   private FTSearchParams.SummarizeParams summarizeParams;
   private boolean highlight;
   private FTSearchParams.HighlightParams highlightParams;
   private Integer slop;
   private Long timeout;
   private boolean inOrder;
   private String language;
   private String expander;
   private String scorer;
   private String sortBy;
   private SortingOrder sortOrder;
   private int[] limit;
   private Map<String, Object> params;
   private Integer dialect;
   private Map<String, Boolean> returnFieldDecodeMap = null;

   public static FTSearchParams searchParams() {
      return new FTSearchParams();
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.noContent) {
         args.add(SearchProtocol.SearchKeyword.NOCONTENT);
      }

      if (this.verbatim) {
         args.add(SearchProtocol.SearchKeyword.VERBATIM);
      }

      if (this.noStopwords) {
         args.add(SearchProtocol.SearchKeyword.NOSTOPWORDS);
      }

      if (this.withScores) {
         args.add(SearchProtocol.SearchKeyword.WITHSCORES);
      }

      if (!this.filters.isEmpty()) {
         this.filters.forEach(filter -> filter.addParams(args));
      }

      if (this.inKeys != null && !this.inKeys.isEmpty()) {
         args.add(SearchProtocol.SearchKeyword.INKEYS).add(this.inKeys.size()).addObjects(this.inKeys);
      }

      if (this.inFields != null && !this.inFields.isEmpty()) {
         args.add(SearchProtocol.SearchKeyword.INFIELDS).add(this.inFields.size()).addObjects(this.inFields);
      }

      if (this.returnFieldsNames != null && !this.returnFieldsNames.isEmpty()) {
         args.add(SearchProtocol.SearchKeyword.RETURN);
         LazyRawable returnCountObject = new LazyRawable();
         args.add(returnCountObject);
         int returnCount = 0;

         for (FieldName fn : this.returnFieldsNames) {
            returnCount += fn.addCommandArguments(args);
         }

         returnCountObject.setRaw(Protocol.toByteArray(returnCount));
      }

      if (this.summarizeParams != null) {
         args.addParams(this.summarizeParams);
      } else if (this.summarize) {
         args.add(SearchProtocol.SearchKeyword.SUMMARIZE);
      }

      if (this.highlightParams != null) {
         args.addParams(this.highlightParams);
      } else if (this.highlight) {
         args.add(SearchProtocol.SearchKeyword.HIGHLIGHT);
      }

      if (this.slop != null) {
         args.add(SearchProtocol.SearchKeyword.SLOP).add(this.slop);
      }

      if (this.timeout != null) {
         args.add(SearchProtocol.SearchKeyword.TIMEOUT).add(this.timeout);
      }

      if (this.inOrder) {
         args.add(SearchProtocol.SearchKeyword.INORDER);
      }

      if (this.language != null) {
         args.add(SearchProtocol.SearchKeyword.LANGUAGE).add(this.language);
      }

      if (this.expander != null) {
         args.add(SearchProtocol.SearchKeyword.EXPANDER).add(this.expander);
      }

      if (this.scorer != null) {
         args.add(SearchProtocol.SearchKeyword.SCORER).add(this.scorer);
      }

      if (this.sortBy != null) {
         args.add(SearchProtocol.SearchKeyword.SORTBY).add(this.sortBy);
         if (this.sortOrder != null) {
            args.add(this.sortOrder);
         }
      }

      if (this.limit != null) {
         args.add(SearchProtocol.SearchKeyword.LIMIT).add(this.limit[0]).add(this.limit[1]);
      }

      if (this.params != null && !this.params.isEmpty()) {
         args.add(SearchProtocol.SearchKeyword.PARAMS).add(this.params.size() << 1);
         this.params.entrySet().forEach(entry -> args.add(entry.getKey()).add(entry.getValue()));
      }

      if (this.dialect != null) {
         args.add(SearchProtocol.SearchKeyword.DIALECT).add(this.dialect);
      }
   }

   public FTSearchParams noContent() {
      this.noContent = true;
      return this;
   }

   public FTSearchParams verbatim() {
      this.verbatim = true;
      return this;
   }

   public FTSearchParams noStopwords() {
      this.noStopwords = true;
      return this;
   }

   public FTSearchParams withScores() {
      this.withScores = true;
      return this;
   }

   public FTSearchParams filter(String field, double min, double max) {
      return this.filter(new FTSearchParams.NumericFilter(field, min, max));
   }

   public FTSearchParams filter(String field, double min, boolean exclusiveMin, double max, boolean exclusiveMax) {
      return this.filter(new FTSearchParams.NumericFilter(field, min, exclusiveMin, max, exclusiveMax));
   }

   public FTSearchParams filter(FTSearchParams.NumericFilter numericFilter) {
      this.filters.add(numericFilter);
      return this;
   }

   public FTSearchParams geoFilter(String field, double lon, double lat, double radius, GeoUnit unit) {
      return this.geoFilter(new FTSearchParams.GeoFilter(field, lon, lat, radius, unit));
   }

   public FTSearchParams geoFilter(FTSearchParams.GeoFilter geoFilter) {
      this.filters.add(geoFilter);
      return this;
   }

   public FTSearchParams inKeys(String... keys) {
      return this.inKeys(Arrays.asList(keys));
   }

   public FTSearchParams inKeys(Collection<String> keys) {
      this.inKeys = keys;
      return this;
   }

   public FTSearchParams inFields(String... fields) {
      return this.inFields(Arrays.asList(fields));
   }

   public FTSearchParams inFields(Collection<String> fields) {
      if (this.inFields == null) {
         this.inFields = new ArrayList<>(fields);
      } else {
         this.inFields.addAll(fields);
      }

      return this;
   }

   public FTSearchParams returnFields(String... fields) {
      if (this.returnFieldsNames == null) {
         this.returnFieldsNames = new ArrayList<>();
      }

      Arrays.stream(fields).forEach(f -> this.returnFieldsNames.add(FieldName.of(f)));
      return this;
   }

   public FTSearchParams returnField(FieldName field) {
      return this.returnFields(Collections.singleton(field));
   }

   public FTSearchParams returnFields(FieldName... fields) {
      return this.returnFields(Arrays.asList(fields));
   }

   public FTSearchParams returnFields(Collection<FieldName> fields) {
      if (this.returnFieldsNames == null) {
         this.returnFieldsNames = new ArrayList<>();
      }

      this.returnFieldsNames.addAll(fields);
      return this;
   }

   public FTSearchParams returnField(String field, boolean decode) {
      this.returnFields(field);
      this.addReturnFieldDecode(field, decode);
      return this;
   }

   public FTSearchParams returnField(FieldName field, boolean decode) {
      this.returnFields(field);
      this.addReturnFieldDecode(field.getAttribute() != null ? field.getAttribute() : field.getName(), decode);
      return this;
   }

   private void addReturnFieldDecode(String returnName, boolean decode) {
      if (this.returnFieldDecodeMap == null) {
         this.returnFieldDecodeMap = new HashMap<>();
      }

      this.returnFieldDecodeMap.put(returnName, decode);
   }

   public FTSearchParams summarize() {
      this.summarize = true;
      return this;
   }

   public FTSearchParams summarize(FTSearchParams.SummarizeParams summarizeParams) {
      this.summarizeParams = summarizeParams;
      return this;
   }

   public FTSearchParams highlight() {
      this.highlight = true;
      return this;
   }

   public FTSearchParams highlight(FTSearchParams.HighlightParams highlightParams) {
      this.highlightParams = highlightParams;
      return this;
   }

   public FTSearchParams scorer(String scorer) {
      this.scorer = scorer;
      return this;
   }

   public FTSearchParams slop(int slop) {
      this.slop = slop;
      return this;
   }

   public FTSearchParams timeout(long timeout) {
      this.timeout = timeout;
      return this;
   }

   public FTSearchParams inOrder() {
      this.inOrder = true;
      return this;
   }

   public FTSearchParams language(String language) {
      this.language = language;
      return this;
   }

   public FTSearchParams sortBy(String sortBy, SortingOrder order) {
      this.sortBy = sortBy;
      this.sortOrder = order;
      return this;
   }

   public FTSearchParams limit(int offset, int num) {
      this.limit = new int[]{offset, num};
      return this;
   }

   public FTSearchParams addParam(String name, Object value) {
      if (this.params == null) {
         this.params = new HashMap<>();
      }

      this.params.put(name, value);
      return this;
   }

   public FTSearchParams params(Map<String, Object> paramValues) {
      if (this.params == null) {
         this.params = new HashMap<>(paramValues);
      } else {
         this.params.putAll(this.params);
      }

      return this;
   }

   public FTSearchParams dialect(int dialect) {
      this.dialect = dialect;
      return this;
   }

   @Internal
   public FTSearchParams dialectOptional(int dialect) {
      if (dialect != 0 && this.dialect == null) {
         this.dialect = dialect;
      }

      return this;
   }

   @Internal
   public boolean getNoContent() {
      return this.noContent;
   }

   @Internal
   public boolean getWithScores() {
      return this.withScores;
   }

   @Internal
   public Map<String, Boolean> getReturnFieldDecodeMap() {
      return this.returnFieldDecodeMap;
   }

   public static FTSearchParams.SummarizeParams summarizeParams() {
      return new FTSearchParams.SummarizeParams();
   }

   public static FTSearchParams.HighlightParams highlightParams() {
      return new FTSearchParams.HighlightParams();
   }

   public static class GeoFilter implements IParams {
      private final String field;
      private final double lon;
      private final double lat;
      private final double radius;
      private final GeoUnit unit;

      public GeoFilter(String field, double lon, double lat, double radius, GeoUnit unit) {
         this.field = field;
         this.lon = lon;
         this.lat = lat;
         this.radius = radius;
         this.unit = unit;
      }

      @Override
      public void addParams(CommandArguments args) {
         args.add(SearchProtocol.SearchKeyword.GEOFILTER).add(this.field).add(this.lon).add(this.lat).add(this.radius).add(this.unit);
      }
   }

   public static class HighlightParams implements IParams {
      private Collection<String> fields;
      private String[] tags;

      public FTSearchParams.HighlightParams fields(String fields) {
         return this.fields(Arrays.asList(fields));
      }

      public FTSearchParams.HighlightParams fields(Collection<String> fields) {
         this.fields = fields;
         return this;
      }

      public FTSearchParams.HighlightParams tags(String open, String close) {
         this.tags = new String[]{open, close};
         return this;
      }

      @Override
      public void addParams(CommandArguments args) {
         args.add(SearchProtocol.SearchKeyword.HIGHLIGHT);
         if (this.fields != null) {
            args.add(SearchProtocol.SearchKeyword.FIELDS).add(this.fields.size()).addObjects(this.fields);
         }

         if (this.tags != null) {
            args.add(SearchProtocol.SearchKeyword.TAGS).add(this.tags[0]).add(this.tags[1]);
         }
      }
   }

   public static class NumericFilter implements IParams {
      private final String field;
      private final double min;
      private final boolean exclusiveMin;
      private final double max;
      private final boolean exclusiveMax;

      public NumericFilter(String field, double min, double max) {
         this(field, min, false, max, false);
      }

      public NumericFilter(String field, double min, boolean exclusiveMin, double max, boolean exclusiveMax) {
         this.field = field;
         this.min = min;
         this.max = max;
         this.exclusiveMax = exclusiveMax;
         this.exclusiveMin = exclusiveMin;
      }

      @Override
      public void addParams(CommandArguments args) {
         args.add(SearchProtocol.SearchKeyword.FILTER)
            .add(this.field)
            .add(this.formatNum(this.min, this.exclusiveMin))
            .add(this.formatNum(this.max, this.exclusiveMax));
      }

      private Object formatNum(double num, boolean exclude) {
         return exclude ? "(" + num : Protocol.toByteArray(num);
      }
   }

   public static class SummarizeParams implements IParams {
      private Collection<String> fields;
      private Integer fragsNum;
      private Integer fragSize;
      private String separator;

      public FTSearchParams.SummarizeParams fields(String... fields) {
         return this.fields(Arrays.asList(fields));
      }

      public FTSearchParams.SummarizeParams fields(Collection<String> fields) {
         this.fields = fields;
         return this;
      }

      public FTSearchParams.SummarizeParams fragsNum(int num) {
         this.fragsNum = num;
         return this;
      }

      public FTSearchParams.SummarizeParams fragSize(int size) {
         this.fragSize = size;
         return this;
      }

      public FTSearchParams.SummarizeParams separator(String separator) {
         this.separator = separator;
         return this;
      }

      @Override
      public void addParams(CommandArguments args) {
         args.add(SearchProtocol.SearchKeyword.SUMMARIZE);
         if (this.fields != null) {
            args.add(SearchProtocol.SearchKeyword.FIELDS).add(this.fields.size()).addObjects(this.fields);
         }

         if (this.fragsNum != null) {
            args.add(SearchProtocol.SearchKeyword.FRAGS).add(this.fragsNum);
         }

         if (this.fragSize != null) {
            args.add(SearchProtocol.SearchKeyword.LEN).add(this.fragSize);
         }

         if (this.separator != null) {
            args.add(SearchProtocol.SearchKeyword.SEPARATOR).add(this.separator);
         }
      }
   }
}
