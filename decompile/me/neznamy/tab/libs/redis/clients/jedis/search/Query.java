package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;
import me.neznamy.tab.libs.redis.clients.jedis.util.LazyRawable;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

public class Query implements IParams {
   private final List<Query.Filter> _filters = new LinkedList<>();
   private final String _queryString;
   private final Query.Paging _paging = new Query.Paging(0, 10);
   private boolean _verbatim = false;
   private boolean _noContent = false;
   private boolean _noStopwords = false;
   private boolean _withScores = false;
   private String _language = null;
   private String[] _fields = null;
   private String[] _keys = null;
   private String[] _returnFields = null;
   private FieldName[] returnFieldNames = null;
   private String[] highlightFields = null;
   private String[] summarizeFields = null;
   private String[] highlightTags = null;
   private String summarizeSeparator = null;
   private int summarizeNumFragments = -1;
   private int summarizeFragmentLen = -1;
   private String _sortBy = null;
   private boolean _sortAsc = true;
   private boolean wantsHighlight = false;
   private boolean wantsSummarize = false;
   private String _scorer = null;
   private Map<String, Object> _params = null;
   private Integer _dialect;
   private int _slop = -1;
   private long _timeout = -1L;
   private boolean _inOrder = false;
   private String _expander = null;

   public Query() {
      this("*");
   }

   public Query(String queryString) {
      this._queryString = queryString;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.add(SafeEncoder.encode(this._queryString));
      if (this._verbatim) {
         args.add(SearchProtocol.SearchKeyword.VERBATIM.getRaw());
      }

      if (this._noContent) {
         args.add(SearchProtocol.SearchKeyword.NOCONTENT.getRaw());
      }

      if (this._noStopwords) {
         args.add(SearchProtocol.SearchKeyword.NOSTOPWORDS.getRaw());
      }

      if (this._withScores) {
         args.add(SearchProtocol.SearchKeyword.WITHSCORES.getRaw());
      }

      if (this._language != null) {
         args.add(SearchProtocol.SearchKeyword.LANGUAGE.getRaw());
         args.add(SafeEncoder.encode(this._language));
      }

      if (this._scorer != null) {
         args.add(SearchProtocol.SearchKeyword.SCORER.getRaw());
         args.add(SafeEncoder.encode(this._scorer));
      }

      if (this._fields != null && this._fields.length > 0) {
         args.add(SearchProtocol.SearchKeyword.INFIELDS.getRaw());
         args.add(Protocol.toByteArray(this._fields.length));

         for (String f : this._fields) {
            args.add(SafeEncoder.encode(f));
         }
      }

      if (this._sortBy != null) {
         args.add(SearchProtocol.SearchKeyword.SORTBY.getRaw());
         args.add(SafeEncoder.encode(this._sortBy));
         args.add((this._sortAsc ? SearchProtocol.SearchKeyword.ASC : SearchProtocol.SearchKeyword.DESC).getRaw());
      }

      if (this._paging.offset != 0 || this._paging.num != 10) {
         args.add(SearchProtocol.SearchKeyword.LIMIT.getRaw()).add(Protocol.toByteArray(this._paging.offset)).add(Protocol.toByteArray(this._paging.num));
      }

      if (!this._filters.isEmpty()) {
         this._filters.forEach(filter -> filter.addParams(args));
      }

      if (this.wantsHighlight) {
         args.add(SearchProtocol.SearchKeyword.HIGHLIGHT.getRaw());
         if (this.highlightFields != null) {
            args.add(SearchProtocol.SearchKeyword.FIELDS.getRaw());
            args.add(Protocol.toByteArray(this.highlightFields.length));

            for (String s : this.highlightFields) {
               args.add(SafeEncoder.encode(s));
            }
         }

         if (this.highlightTags != null) {
            args.add(SearchProtocol.SearchKeyword.TAGS.getRaw());

            for (String t : this.highlightTags) {
               args.add(SafeEncoder.encode(t));
            }
         }
      }

      if (this.wantsSummarize) {
         args.add(SearchProtocol.SearchKeyword.SUMMARIZE.getRaw());
         if (this.summarizeFields != null) {
            args.add(SearchProtocol.SearchKeyword.FIELDS.getRaw());
            args.add(Protocol.toByteArray(this.summarizeFields.length));

            for (String s : this.summarizeFields) {
               args.add(SafeEncoder.encode(s));
            }
         }

         if (this.summarizeNumFragments != -1) {
            args.add(SearchProtocol.SearchKeyword.FRAGS.getRaw());
            args.add(Protocol.toByteArray(this.summarizeNumFragments));
         }

         if (this.summarizeFragmentLen != -1) {
            args.add(SearchProtocol.SearchKeyword.LEN.getRaw());
            args.add(Protocol.toByteArray(this.summarizeFragmentLen));
         }

         if (this.summarizeSeparator != null) {
            args.add(SearchProtocol.SearchKeyword.SEPARATOR.getRaw());
            args.add(SafeEncoder.encode(this.summarizeSeparator));
         }
      }

      if (this._keys != null && this._keys.length > 0) {
         args.add(SearchProtocol.SearchKeyword.INKEYS.getRaw());
         args.add(Protocol.toByteArray(this._keys.length));

         for (String f : this._keys) {
            args.add(SafeEncoder.encode(f));
         }
      }

      if (this._returnFields != null && this._returnFields.length > 0) {
         args.add(SearchProtocol.SearchKeyword.RETURN.getRaw());
         args.add(Protocol.toByteArray(this._returnFields.length));

         for (String f : this._returnFields) {
            args.add(SafeEncoder.encode(f));
         }
      } else if (this.returnFieldNames != null && this.returnFieldNames.length > 0) {
         args.add(SearchProtocol.SearchKeyword.RETURN.getRaw());
         LazyRawable returnCountObject = new LazyRawable();
         args.add(returnCountObject);
         int returnCount = 0;

         for (FieldName fn : this.returnFieldNames) {
            returnCount += fn.addCommandArguments(args);
         }

         returnCountObject.setRaw(Protocol.toByteArray(returnCount));
      }

      if (this._params != null && this._params.size() > 0) {
         args.add(SearchProtocol.SearchKeyword.PARAMS.getRaw());
         args.add(this._params.size() << 1);

         for (Entry<String, Object> entry : this._params.entrySet()) {
            args.add(entry.getKey());
            args.add(entry.getValue());
         }
      }

      if (this._dialect != null) {
         args.add(SearchProtocol.SearchKeyword.DIALECT.getRaw());
         args.add(this._dialect);
      }

      if (this._slop >= 0) {
         args.add(SearchProtocol.SearchKeyword.SLOP.getRaw());
         args.add(this._slop);
      }

      if (this._timeout >= 0L) {
         args.add(SearchProtocol.SearchKeyword.TIMEOUT.getRaw());
         args.add(this._timeout);
      }

      if (this._inOrder) {
         args.add(SearchProtocol.SearchKeyword.INORDER.getRaw());
      }

      if (this._expander != null) {
         args.add(SearchProtocol.SearchKeyword.EXPANDER.getRaw());
         args.add(SafeEncoder.encode(this._expander));
      }
   }

   public Query limit(Integer offset, Integer limit) {
      this._paging.offset = offset;
      this._paging.num = limit;
      return this;
   }

   public Query addFilter(Query.Filter f) {
      this._filters.add(f);
      return this;
   }

   public Query setVerbatim() {
      this._verbatim = true;
      return this;
   }

   public boolean getNoContent() {
      return this._noContent;
   }

   public Query setNoContent() {
      this._noContent = true;
      return this;
   }

   public Query setNoStopwords() {
      this._noStopwords = true;
      return this;
   }

   public boolean getWithScores() {
      return this._withScores;
   }

   public Query setWithScores() {
      this._withScores = true;
      return this;
   }

   public Query setLanguage(String language) {
      this._language = language;
      return this;
   }

   public Query setScorer(String scorer) {
      this._scorer = scorer;
      return this;
   }

   public Query limitFields(String... fields) {
      this._fields = fields;
      return this;
   }

   public Query limitKeys(String... keys) {
      this._keys = keys;
      return this;
   }

   public Query returnFields(String... fields) {
      this._returnFields = fields;
      this.returnFieldNames = null;
      return this;
   }

   public Query returnFields(FieldName... fields) {
      this.returnFieldNames = fields;
      this._returnFields = null;
      return this;
   }

   public Query highlightFields(Query.HighlightTags tags, String... fields) {
      if (fields == null || fields.length > 0) {
         this.highlightFields = fields;
      }

      if (tags != null) {
         this.highlightTags = new String[]{tags.open, tags.close};
      } else {
         this.highlightTags = null;
      }

      this.wantsHighlight = true;
      return this;
   }

   public Query highlightFields(String... fields) {
      return this.highlightFields(null, fields);
   }

   public Query summarizeFields(int contextLen, int fragmentCount, String separator, String... fields) {
      if (fields == null || fields.length > 0) {
         this.summarizeFields = fields;
      }

      this.summarizeFragmentLen = contextLen;
      this.summarizeNumFragments = fragmentCount;
      this.summarizeSeparator = separator;
      this.wantsSummarize = true;
      return this;
   }

   public Query summarizeFields(String... fields) {
      return this.summarizeFields(-1, -1, null, fields);
   }

   public Query setSortBy(String field, boolean ascending) {
      this._sortBy = field;
      this._sortAsc = ascending;
      return this;
   }

   public Query addParam(String name, Object value) {
      if (this._params == null) {
         this._params = new HashMap<>();
      }

      this._params.put(name, value);
      return this;
   }

   public Query dialect(int dialect) {
      this._dialect = dialect;
      return this;
   }

   public Query dialectOptional(int dialect) {
      if (dialect != 0 && this._dialect == null) {
         this._dialect = dialect;
      }

      return this;
   }

   public Query slop(int slop) {
      this._slop = slop;
      return this;
   }

   public Query timeout(long timeout) {
      this._timeout = timeout;
      return this;
   }

   public Query setInOrder() {
      this._inOrder = true;
      return this;
   }

   public Query setExpander(String field) {
      this._expander = field;
      return this;
   }

   public abstract static class Filter implements IParams {
      public final String property;

      public Filter(String property) {
         this.property = property;
      }
   }

   public static class GeoFilter extends Query.Filter {
      public static final String KILOMETERS = "km";
      public static final String METERS = "m";
      public static final String FEET = "ft";
      public static final String MILES = "mi";
      private final double lon;
      private final double lat;
      private final double radius;
      private final String unit;

      public GeoFilter(String property, double lon, double lat, double radius, String unit) {
         super(property);
         this.lon = lon;
         this.lat = lat;
         this.radius = radius;
         this.unit = unit;
      }

      @Override
      public void addParams(CommandArguments args) {
         args.add(SearchProtocol.SearchKeyword.GEOFILTER.getRaw());
         args.add(SafeEncoder.encode(this.property));
         args.add(Protocol.toByteArray(this.lon));
         args.add(Protocol.toByteArray(this.lat));
         args.add(Protocol.toByteArray(this.radius));
         args.add(SafeEncoder.encode(this.unit));
      }
   }

   public static class HighlightTags {
      private final String open;
      private final String close;

      public HighlightTags(String open, String close) {
         this.open = open;
         this.close = close;
      }
   }

   public static class NumericFilter extends Query.Filter {
      private final double min;
      private final boolean exclusiveMin;
      private final double max;
      private final boolean exclusiveMax;

      public NumericFilter(String property, double min, boolean exclusiveMin, double max, boolean exclusiveMax) {
         super(property);
         this.min = min;
         this.max = max;
         this.exclusiveMax = exclusiveMax;
         this.exclusiveMin = exclusiveMin;
      }

      public NumericFilter(String property, double min, double max) {
         this(property, min, false, max, false);
      }

      private byte[] formatNum(double num, boolean exclude) {
         return exclude ? SafeEncoder.encode("(" + num) : Protocol.toByteArray(num);
      }

      @Override
      public void addParams(CommandArguments args) {
         args.add(SearchProtocol.SearchKeyword.FILTER.getRaw());
         args.add(SafeEncoder.encode(this.property));
         args.add(this.formatNum(this.min, this.exclusiveMin));
         args.add(this.formatNum(this.max, this.exclusiveMax));
      }
   }

   public static class Paging {
      int offset;
      int num;

      public Paging(int offset, int num) {
         this.offset = offset;
         this.num = num;
      }
   }
}
