package me.neznamy.tab.libs.com.rabbitmq.tools.jsonrpc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import me.neznamy.tab.libs.com.rabbitmq.tools.json.JSONUtil;

public class ServiceDescription {
   public static final String JSON_RPC_VERSION = "1.1";
   private String name;
   private String id;
   private String version;
   private String summary;
   private String help;
   private Map<String, ProcedureDescription> procedures;

   public ServiceDescription(Map<String, Object> rawServiceDescription) {
      JSONUtil.tryFill(this, rawServiceDescription);
   }

   public ServiceDescription(Class<?> klass) {
      this.procedures = new HashMap<>();

      for (Method m : klass.getMethods()) {
         ProcedureDescription proc = new ProcedureDescription(m);
         this.addProcedure(proc);
      }
   }

   public ServiceDescription() {
   }

   public Collection<ProcedureDescription> getProcs() {
      return this.procedures.values();
   }

   public void setProcs(Collection<Map<String, Object>> p) {
      this.procedures = new HashMap<>();

      for (Map<String, Object> pm : p) {
         ProcedureDescription proc = new ProcedureDescription(pm);
         this.addProcedure(proc);
      }
   }

   private void addProcedure(ProcedureDescription proc) {
      this.procedures.put(proc.getName() + "/" + proc.arity(), proc);
   }

   public ProcedureDescription getProcedure(String newname, int arity) {
      ProcedureDescription proc = this.procedures.get(newname + "/" + arity);
      if (proc == null) {
         throw new IllegalArgumentException("Procedure not found: " + newname + ", arity " + arity);
      } else {
         return proc;
      }
   }

   public String getName() {
      return this.name;
   }

   public String getId() {
      return this.id;
   }

   public String getVersion() {
      return this.version;
   }

   public String getSummary() {
      return this.summary;
   }

   public String getHelp() {
      return this.help;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public void setSummary(String summary) {
      this.summary = summary;
   }

   public void setHelp(String help) {
      this.help = help;
   }
}
