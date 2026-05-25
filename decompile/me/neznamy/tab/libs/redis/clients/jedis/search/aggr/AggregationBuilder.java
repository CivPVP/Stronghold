package me.neznamy.tab.libs.redis.clients.jedis.search.aggr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.Protocol;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;
import me.neznamy.tab.libs.redis.clients.jedis.search.FieldName;
import me.neznamy.tab.libs.redis.clients.jedis.search.SearchProtocol;
import me.neznamy.tab.libs.redis.clients.jedis.util.LazyRawable;

public class AggregationBuilder implements IParams {
   private final List<Object> aggrArgs = new ArrayList<>();
   private Integer dialect;
   private boolean isWithCursor = false;

   public AggregationBuilder(String query) {
      this.aggrArgs.add(query);
   }

   public AggregationBuilder() {
      this("*");
   }

   public AggregationBuilder load(String... fields) {
      return this.load(FieldName.convert(fields));
   }

   public AggregationBuilder load(FieldName... fields) {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.LOAD);
      LazyRawable rawLoadCount = new LazyRawable();
      this.aggrArgs.add(rawLoadCount);
      int loadCount = 0;

      for (FieldName fn : fields) {
         loadCount += fn.addCommandArguments(this.aggrArgs);
      }

      rawLoadCount.setRaw(Protocol.toByteArray(loadCount));
      return this;
   }

   public AggregationBuilder loadAll() {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.LOAD);
      this.aggrArgs.add(Protocol.BYTES_ASTERISK);
      return this;
   }

   public AggregationBuilder limit(int offset, int count) {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.LIMIT);
      this.aggrArgs.add(offset);
      this.aggrArgs.add(count);
      return this;
   }

   public AggregationBuilder limit(int count) {
      return this.limit(0, count);
   }

   public AggregationBuilder sortBy(SortedField... fields) {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.SORTBY);
      this.aggrArgs.add(fields.length << 1);

      for (SortedField field : fields) {
         this.aggrArgs.add(field.getField());
         this.aggrArgs.add(field.getOrder());
      }

      return this;
   }

   public AggregationBuilder sortByAsc(String field) {
      return this.sortBy(SortedField.asc(field));
   }

   public AggregationBuilder sortByDesc(String field) {
      return this.sortBy(SortedField.desc(field));
   }

   public AggregationBuilder sortByMax(int max) {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.MAX);
      this.aggrArgs.add(max);
      return this;
   }

   public AggregationBuilder sortBy(int max, SortedField... fields) {
      this.sortBy(fields);
      this.sortByMax(max);
      return this;
   }

   public AggregationBuilder apply(String projection, String alias) {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.APPLY);
      this.aggrArgs.add(projection);
      this.aggrArgs.add(SearchProtocol.SearchKeyword.AS);
      this.aggrArgs.add(alias);
      return this;
   }

   public AggregationBuilder groupBy(Group group) {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.GROUPBY);
      group.addArgs(this.aggrArgs);
      return this;
   }

   public AggregationBuilder groupBy(Collection<String> fields, Collection<Reducer> reducers) {
      String[] fieldsArr = new String[fields.size()];
      Group g = new Group(fields.toArray(fieldsArr));
      reducers.forEach(r -> g.reduce(r));
      this.groupBy(g);
      return this;
   }

   public AggregationBuilder groupBy(String field, Reducer... reducers) {
      return this.groupBy(Collections.singletonList(field), Arrays.asList(reducers));
   }

   public AggregationBuilder filter(String expression) {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.FILTER);
      this.aggrArgs.add(expression);
      return this;
   }

   public AggregationBuilder cursor(int count) {
      this.isWithCursor = true;
      this.aggrArgs.add(SearchProtocol.SearchKeyword.WITHCURSOR);
      this.aggrArgs.add(SearchProtocol.SearchKeyword.COUNT);
      this.aggrArgs.add(count);
      return this;
   }

   public AggregationBuilder cursor(int count, long maxIdle) {
      this.isWithCursor = true;
      this.aggrArgs.add(SearchProtocol.SearchKeyword.WITHCURSOR);
      this.aggrArgs.add(SearchProtocol.SearchKeyword.COUNT);
      this.aggrArgs.add(count);
      this.aggrArgs.add(SearchProtocol.SearchKeyword.MAXIDLE);
      this.aggrArgs.add(maxIdle);
      return this;
   }

   public AggregationBuilder verbatim() {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.VERBATIM);
      return this;
   }

   public AggregationBuilder timeout(long timeout) {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.TIMEOUT);
      this.aggrArgs.add(timeout);
      return this;
   }

   public AggregationBuilder addScores() {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.ADDSCORES);
      return this;
   }

   public AggregationBuilder params(Map<String, Object> params) {
      this.aggrArgs.add(SearchProtocol.SearchKeyword.PARAMS);
      this.aggrArgs.add(params.size() << 1);
      params.forEach((k, v) -> {
         this.aggrArgs.add(k);
         this.aggrArgs.add(v);
      });
      return this;
   }

   public AggregationBuilder dialect(int dialect) {
      this.dialect = dialect;
      return this;
   }

   public AggregationBuilder dialectOptional(int dialect) {
      if (dialect != 0 && this.dialect == null) {
         this.dialect = dialect;
      }

      return this;
   }

   public boolean isWithCursor() {
      return this.isWithCursor;
   }

   @Override
   public void addParams(CommandArguments commArgs) {
      commArgs.addObjects(this.aggrArgs);
      if (this.dialect != null) {
         commArgs.add(SearchProtocol.SearchKeyword.DIALECT).add(this.dialect);
      }
   }
}
