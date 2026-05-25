package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class CommandDocument {
   private static final String SUMMARY_STR = "summary";
   private static final String SINCE_STR = "since";
   private static final String GROUP_STR = "group";
   private static final String COMPLEXITY_STR = "complexity";
   private static final String HISTORY_STR = "history";
   private final String summary;
   private final String since;
   private final String group;
   private final String complexity;
   private final List<String> history;
   @Deprecated
   public static final Builder<CommandDocument> COMMAND_DOCUMENT_BUILDER = new Builder<CommandDocument>() {
      public CommandDocument build(Object data) {
         List<Object> commandData = (List<Object>)data;
         String summary = BuilderFactory.STRING.build(commandData.get(1));
         String since = BuilderFactory.STRING.build(commandData.get(3));
         String group = BuilderFactory.STRING.build(commandData.get(5));
         String complexity = BuilderFactory.STRING.build(commandData.get(7));
         List<String> history = null;
         if (BuilderFactory.STRING.build(commandData.get(8)).equals("history")) {
            List<List<Object>> rawHistory = (List<List<Object>>)commandData.get(9);
            history = new ArrayList<>(rawHistory.size());

            for (List<Object> timePoint : rawHistory) {
               history.add(BuilderFactory.STRING.build(timePoint.get(0)) + ": " + BuilderFactory.STRING.build(timePoint.get(1)));
            }
         }

         return new CommandDocument(summary, since, group, complexity, history);
      }
   };

   @Deprecated
   public CommandDocument(String summary, String since, String group, String complexity, List<String> history) {
      this.summary = summary;
      this.since = since;
      this.group = group;
      this.complexity = complexity;
      this.history = history;
   }

   public CommandDocument(Map<String, Object> map) {
      this.summary = (String)map.get("summary");
      this.since = (String)map.get("since");
      this.group = (String)map.get("group");
      this.complexity = (String)map.get("complexity");
      List<Object> historyObject = (List<Object>)map.get("history");
      if (historyObject == null) {
         this.history = null;
      } else if (historyObject.isEmpty()) {
         this.history = Collections.emptyList();
      } else if (historyObject.get(0) instanceof KeyValue) {
         this.history = historyObject.stream().map(o -> (KeyValue)o).map(kv -> (String)kv.getKey() + ": " + (String)kv.getValue()).collect(Collectors.toList());
      } else {
         this.history = historyObject.stream().map(o -> (List)o).map(l -> (String)l.get(0) + ": " + (String)l.get(1)).collect(Collectors.toList());
      }
   }

   public String getSummary() {
      return this.summary;
   }

   public String getSince() {
      return this.since;
   }

   public String getGroup() {
      return this.group;
   }

   public String getComplexity() {
      return this.complexity;
   }

   public List<String> getHistory() {
      return this.history;
   }
}
