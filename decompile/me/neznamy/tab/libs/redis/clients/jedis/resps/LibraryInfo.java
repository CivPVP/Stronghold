package me.neznamy.tab.libs.redis.clients.jedis.resps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.neznamy.tab.libs.redis.clients.jedis.Builder;
import me.neznamy.tab.libs.redis.clients.jedis.BuilderFactory;
import me.neznamy.tab.libs.redis.clients.jedis.util.KeyValue;

public class LibraryInfo {
   private final String libraryName;
   private final String engine;
   private final List<Map<String, Object>> functions;
   private final String libraryCode;
   public static final Builder<LibraryInfo> LIBRARY_INFO = new Builder<LibraryInfo>() {
      public LibraryInfo build(Object data) {
         if (data == null) {
            return null;
         }

         List list = (List)data;
         if (list.isEmpty()) {
            return null;
         }

         if (list.get(0) instanceof KeyValue) {
            String libname = null;
            String enginename = null;
            String librarycode = null;
            List<Map<String, Object>> functions = null;

            for (KeyValue kv : list) {
               switch ((String)BuilderFactory.STRING.build(kv.getKey())) {
                  case "library_name":
                     libname = BuilderFactory.STRING.build(kv.getValue());
                     break;
                  case "engine":
                     enginename = BuilderFactory.STRING.build(kv.getValue());
                     break;
                  case "functions":
                     functions = ((List)kv.getValue()).stream().map(o -> BuilderFactory.ENCODED_OBJECT_MAP.build(o)).collect(Collectors.toList());
                     break;
                  case "library_code":
                     librarycode = BuilderFactory.STRING.build(kv.getValue());
               }
            }

            return new LibraryInfo(libname, enginename, functions, librarycode);
         } else {
            String libname = BuilderFactory.STRING.build(list.get(1));
            String engine = BuilderFactory.STRING.build(list.get(3));
            List<Object> rawFunctions = (List<Object>)list.get(5);
            List<Map<String, Object>> functions = rawFunctions.stream().map(o -> BuilderFactory.ENCODED_OBJECT_MAP.build(o)).collect(Collectors.toList());
            if (list.size() <= 6) {
               return new LibraryInfo(libname, engine, functions);
            }

            String code = BuilderFactory.STRING.build(list.get(7));
            return new LibraryInfo(libname, engine, functions, code);
         }
      }
   };
   @Deprecated
   public static final Builder<LibraryInfo> LIBRARY_BUILDER = LIBRARY_INFO;
   public static final Builder<List<LibraryInfo>> LIBRARY_INFO_LIST = new Builder<List<LibraryInfo>>() {
      public List<LibraryInfo> build(Object data) {
         List<Object> list = (List<Object>)data;
         return list.stream().map(o -> LibraryInfo.LIBRARY_INFO.build(o)).collect(Collectors.toList());
      }
   };

   public LibraryInfo(String libraryName, String engineName, List<Map<String, Object>> functions) {
      this(libraryName, engineName, functions, null);
   }

   public LibraryInfo(String libraryName, String engineName, List<Map<String, Object>> functions, String code) {
      this.libraryName = libraryName;
      this.engine = engineName;
      this.functions = functions;
      this.libraryCode = code;
   }

   public String getLibraryName() {
      return this.libraryName;
   }

   public String getEngine() {
      return this.engine;
   }

   public List<Map<String, Object>> getFunctions() {
      return this.functions;
   }

   public String getLibraryCode() {
      return this.libraryCode;
   }
}
