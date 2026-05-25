package me.neznamy.tab.libs.redis.clients.jedis.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisDataException;
import me.neznamy.tab.libs.redis.clients.jedis.exceptions.JedisException;
import me.neznamy.tab.libs.redis.clients.jedis.graph.entities.Edge;
import me.neznamy.tab.libs.redis.clients.jedis.graph.entities.GraphEntity;
import me.neznamy.tab.libs.redis.clients.jedis.graph.entities.Node;
import me.neznamy.tab.libs.redis.clients.jedis.graph.entities.Path;
import me.neznamy.tab.libs.redis.clients.jedis.graph.entities.Point;
import me.neznamy.tab.libs.redis.clients.jedis.util.SafeEncoder;

@Deprecated
class ResultSetBuilder extends Builder<ResultSet> {
   private final GraphCache graphCache;
   private static final ResultSetBuilder.ScalarType[] SCALAR_TYPES = ResultSetBuilder.ScalarType.values();
   private static final ResultSet.ColumnType[] COLUMN_TYPES = ResultSet.ColumnType.values();

   ResultSetBuilder(GraphCache cache) {
      this.graphCache = cache;
   }

   public ResultSet build(Object data) {
      List<Object> rawResponse = (List<Object>)data;
      if (rawResponse.get(rawResponse.size() - 1) instanceof JedisDataException) {
         throw (JedisDataException)rawResponse.get(rawResponse.size() - 1);
      }

      Object headerObject;
      Object recordsObject;
      Object statisticsObject;
      if (rawResponse.size() == 1) {
         headerObject = Collections.emptyList();
         recordsObject = Collections.emptyList();
         statisticsObject = rawResponse.get(0);
      } else {
         if (rawResponse.size() != 3) {
            throw new JedisException("Unrecognized graph response format.");
         }

         headerObject = rawResponse.get(0);
         recordsObject = rawResponse.get(1);
         statisticsObject = rawResponse.get(2);
      }

      ResultSetBuilder.HeaderImpl header = this.parseHeader(headerObject);
      List<Record> records = this.parseRecords(header, recordsObject);
      ResultSetBuilder.StatisticsImpl statistics = this.parseStatistics(statisticsObject);
      return new ResultSetBuilder.ResultSetImpl(header, records, statistics);
   }

   private List<Record> parseRecords(Header header, Object data) {
      List<List<Object>> rawResultSet = (List<List<Object>>)data;
      if (rawResultSet != null && !rawResultSet.isEmpty()) {
         List<Record> results = new ArrayList<>(rawResultSet.size());

         for (List<Object> row : rawResultSet) {
            List<Object> parsedRow = new ArrayList<>(row.size());

            for (int i = 0; i < row.size(); i++) {
               List<Object> obj = (List<Object>)row.get(i);
               ResultSet.ColumnType objType = header.getSchemaTypes().get(i);
               switch (objType) {
                  case NODE:
                     parsedRow.add(this.deserializeNode(obj));
                     break;
                  case RELATION:
                     parsedRow.add(this.deserializeEdge(obj));
                     break;
                  case SCALAR:
                     parsedRow.add(this.deserializeScalar(obj));
                     break;
                  default:
                     parsedRow.add(null);
               }
            }

            Record record = new ResultSetBuilder.RecordImpl(header.getSchemaNames(), parsedRow);
            results.add(record);
         }

         return results;
      } else {
         return new ArrayList<>(0);
      }
   }

   private Node deserializeNode(List<Object> rawNodeData) {
      List<Long> labelsIndices = (List<Long>)rawNodeData.get(1);
      List<List<Object>> rawProperties = (List<List<Object>>)rawNodeData.get(2);
      Node node = new Node(labelsIndices.size(), rawProperties.size());
      this.deserializeGraphEntityId(node, (Long)rawNodeData.get(0));

      for (Long labelIndex : labelsIndices) {
         String label = this.graphCache.getLabel(labelIndex.intValue());
         node.addLabel(label);
      }

      this.deserializeGraphEntityProperties(node, rawProperties);
      return node;
   }

   private void deserializeGraphEntityId(GraphEntity graphEntity, long id) {
      graphEntity.setId(id);
   }

   private Edge deserializeEdge(List<Object> rawEdgeData) {
      List<List<Object>> rawProperties = (List<List<Object>>)rawEdgeData.get(4);
      Edge edge = new Edge(rawProperties.size());
      this.deserializeGraphEntityId(edge, (Long)rawEdgeData.get(0));
      String relationshipType = this.graphCache.getRelationshipType(((Long)rawEdgeData.get(1)).intValue());
      edge.setRelationshipType(relationshipType);
      edge.setSource((Long)rawEdgeData.get(2));
      edge.setDestination((Long)rawEdgeData.get(3));
      this.deserializeGraphEntityProperties(edge, rawProperties);
      return edge;
   }

   private void deserializeGraphEntityProperties(GraphEntity entity, List<List<Object>> rawProperties) {
      for (List<Object> rawProperty : rawProperties) {
         String name = this.graphCache.getPropertyName(((Long)rawProperty.get(0)).intValue());
         List<Object> propertyScalar = rawProperty.subList(1, rawProperty.size());
         entity.addProperty(name, this.deserializeScalar(propertyScalar));
      }
   }

   private Object deserializeScalar(List<Object> rawScalarData) {
      ResultSetBuilder.ScalarType type = this.getValueTypeFromObject(rawScalarData.get(0));
      Object obj = rawScalarData.get(1);
      switch (type) {
         case NULL:
            return null;
         case BOOLEAN:
            return Boolean.parseBoolean(SafeEncoder.encode((byte[])obj));
         case DOUBLE:
            return BuilderFactory.DOUBLE.build(obj);
         case INTEGER:
            return (Long)obj;
         case STRING:
            return SafeEncoder.encode((byte[])obj);
         case ARRAY:
            return this.deserializeArray(obj);
         case NODE:
            return this.deserializeNode((List<Object>)obj);
         case EDGE:
            return this.deserializeEdge((List<Object>)obj);
         case PATH:
            return this.deserializePath(obj);
         case MAP:
            return this.deserializeMap(obj);
         case POINT:
            return this.deserializePoint(obj);
         case UNKNOWN:
         default:
            return obj;
      }
   }

   private Object deserializePoint(Object rawScalarData) {
      return new Point(BuilderFactory.DOUBLE_LIST.build(rawScalarData));
   }

   private Map<String, Object> deserializeMap(Object rawScalarData) {
      List<Object> keyTypeValueEntries = (List<Object>)rawScalarData;
      int size = keyTypeValueEntries.size();
      Map<String, Object> map = new HashMap<>(size >> 1);

      for (int i = 0; i < size; i += 2) {
         String key = SafeEncoder.encode((byte[])keyTypeValueEntries.get(i));
         Object value = this.deserializeScalar((List<Object>)keyTypeValueEntries.get(i + 1));
         map.put(key, value);
      }

      return map;
   }

   private Path deserializePath(Object rawScalarData) {
      List<List<Object>> array = (List<List<Object>>)rawScalarData;
      List<Node> nodes = (List<Node>)this.deserializeScalar(array.get(0));
      List<Edge> edges = (List<Edge>)this.deserializeScalar(array.get(1));
      return new Path(nodes, edges);
   }

   private List<Object> deserializeArray(Object rawScalarData) {
      List<List<Object>> array = (List<List<Object>>)rawScalarData;
      List<Object> res = new ArrayList<>(array.size());

      for (List<Object> arrayValue : array) {
         res.add(this.deserializeScalar(arrayValue));
      }

      return res;
   }

   private ResultSetBuilder.ScalarType getValueTypeFromObject(Object rawScalarType) {
      return getScalarType(((Long)rawScalarType).intValue());
   }

   private static ResultSetBuilder.ScalarType getScalarType(int index) {
      try {
         return SCALAR_TYPES[index];
      } catch (IndexOutOfBoundsException e) {
         throw new JedisException("Unrecognized response type");
      }
   }

   private ResultSetBuilder.HeaderImpl parseHeader(Object data) {
      if (data == null) {
         return new ResultSetBuilder.HeaderImpl();
      }

      List<List<Object>> list = (List<List<Object>>)data;
      List<ResultSet.ColumnType> types = new ArrayList<>(list.size());
      List<String> texts = new ArrayList<>(list.size());

      for (List<Object> tuple : list) {
         types.add(COLUMN_TYPES[((Long)tuple.get(0)).intValue()]);
         texts.add(SafeEncoder.encode((byte[])tuple.get(1)));
      }

      return new ResultSetBuilder.HeaderImpl(types, texts);
   }

   private ResultSetBuilder.StatisticsImpl parseStatistics(Object data) {
      Map<String, String> map = ((List)data)
         .stream()
         .map(SafeEncoder::encode)
         .map(s -> s.split(": "))
         .collect(Collectors.toMap(sa -> (String)sa[0], sa -> (String)sa[1]));
      return new ResultSetBuilder.StatisticsImpl(map);
   }

   private class HeaderImpl implements Header {
      private final List<ResultSet.ColumnType> schemaTypes;
      private final List<String> schemaNames;

      private HeaderImpl() {
         this.schemaTypes = Collections.emptyList();
         this.schemaNames = Collections.emptyList();
      }

      private HeaderImpl(List<ResultSet.ColumnType> schemaTypes, List<String> schemaNames) {
         this.schemaTypes = schemaTypes;
         this.schemaNames = schemaNames;
      }

      @Override
      public List<String> getSchemaNames() {
         return this.schemaNames;
      }

      @Override
      public List<ResultSet.ColumnType> getSchemaTypes() {
         return this.schemaTypes;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }

         if (!(o instanceof ResultSetBuilder.HeaderImpl)) {
            return false;
         }

         ResultSetBuilder.HeaderImpl header = (ResultSetBuilder.HeaderImpl)o;
         return Objects.equals(this.getSchemaTypes(), header.getSchemaTypes()) && Objects.equals(this.getSchemaNames(), header.getSchemaNames());
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.getSchemaTypes(), this.getSchemaNames());
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder("HeaderImpl{");
         sb.append("schemaTypes=").append(this.schemaTypes);
         sb.append(", schemaNames=").append(this.schemaNames);
         sb.append('}');
         return sb.toString();
      }
   }

   private class RecordImpl implements Record {
      private final List<String> header;
      private final List<Object> values;

      public RecordImpl(List<String> header, List<Object> values) {
         this.header = header;
         this.values = values;
      }

      @Override
      public <T> T getValue(int index) {
         return (T)this.values.get(index);
      }

      @Override
      public <T> T getValue(String key) {
         return this.getValue(this.header.indexOf(key));
      }

      @Override
      public String getString(int index) {
         return this.values.get(index).toString();
      }

      @Override
      public String getString(String key) {
         return this.getString(this.header.indexOf(key));
      }

      @Override
      public List<String> keys() {
         return this.header;
      }

      @Override
      public List<Object> values() {
         return this.values;
      }

      @Override
      public boolean containsKey(String key) {
         return this.header.contains(key);
      }

      @Override
      public int size() {
         return this.header.size();
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }

         if (!(o instanceof ResultSetBuilder.RecordImpl)) {
            return false;
         }

         ResultSetBuilder.RecordImpl record = (ResultSetBuilder.RecordImpl)o;
         return Objects.equals(this.header, record.header) && Objects.equals(this.values, record.values);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.header, this.values);
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder("Record{");
         sb.append("values=").append(this.values);
         sb.append('}');
         return sb.toString();
      }
   }

   private class ResultSetImpl implements ResultSet {
      private final Header header;
      private final List<Record> results;
      private final Statistics statistics;

      private ResultSetImpl(Header header, List<Record> results, Statistics statistics) {
         this.header = header;
         this.results = results;
         this.statistics = statistics;
      }

      @Override
      public Header getHeader() {
         return this.header;
      }

      @Override
      public Statistics getStatistics() {
         return this.statistics;
      }

      @Override
      public int size() {
         return this.results.size();
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }

         if (!(o instanceof ResultSetBuilder.ResultSetImpl)) {
            return false;
         }

         ResultSetBuilder.ResultSetImpl resultSet = (ResultSetBuilder.ResultSetImpl)o;
         return Objects.equals(this.getHeader(), resultSet.getHeader())
            && Objects.equals(this.getStatistics(), resultSet.getStatistics())
            && Objects.equals(this.results, resultSet.results);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.getHeader(), this.getStatistics(), this.results);
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder("ResultSetImpl{");
         sb.append("header=").append(this.header);
         sb.append(", statistics=").append(this.statistics);
         sb.append(", results=").append(this.results);
         sb.append('}');
         return sb.toString();
      }

      @Override
      public Iterator<Record> iterator() {
         return this.results.iterator();
      }
   }

   private enum ScalarType {
      UNKNOWN,
      NULL,
      STRING,
      INTEGER,
      BOOLEAN,
      DOUBLE,
      ARRAY,
      EDGE,
      NODE,
      PATH,
      MAP,
      POINT;
   }

   private class StatisticsImpl implements Statistics {
      private final Map<String, String> statistics;

      private StatisticsImpl(Map<String, String> statistics) {
         this.statistics = statistics;
      }

      public String getStringValue(String label) {
         return this.statistics.get(label);
      }

      private int getIntValue(String label) {
         String value = this.getStringValue(label);
         return value == null ? 0 : Integer.parseInt(value);
      }

      @Override
      public int nodesCreated() {
         return this.getIntValue("Nodes created");
      }

      @Override
      public int nodesDeleted() {
         return this.getIntValue("Nodes deleted");
      }

      @Override
      public int indicesCreated() {
         return this.getIntValue("Indices created");
      }

      @Override
      public int indicesDeleted() {
         return this.getIntValue("Indices deleted");
      }

      @Override
      public int labelsAdded() {
         return this.getIntValue("Labels added");
      }

      @Override
      public int relationshipsDeleted() {
         return this.getIntValue("Relationships deleted");
      }

      @Override
      public int relationshipsCreated() {
         return this.getIntValue("Relationships created");
      }

      @Override
      public int propertiesSet() {
         return this.getIntValue("Properties set");
      }

      @Override
      public boolean cachedExecution() {
         return "1".equals(this.getStringValue("Cached execution"));
      }

      @Override
      public String queryIntervalExecutionTime() {
         return this.getStringValue("Query internal execution time");
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }

         if (!(o instanceof ResultSetBuilder.StatisticsImpl)) {
            return false;
         }

         ResultSetBuilder.StatisticsImpl that = (ResultSetBuilder.StatisticsImpl)o;
         return Objects.equals(this.statistics, that.statistics);
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.statistics);
      }

      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder("Statistics{");
         sb.append(this.statistics);
         sb.append('}');
         return sb.toString();
      }
   }
}
