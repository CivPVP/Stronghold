package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.List;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class FieldName implements IParams {
   private final String name;
   private String attribute;

   public FieldName(String name) {
      this.name = name;
   }

   public FieldName(String name, String attribute) {
      this.name = name;
      this.attribute = attribute;
   }

   public FieldName as(String attribute) {
      if (attribute == null) {
         throw new IllegalArgumentException("Setting null as field attribute is not allowed.");
      }

      if (this.attribute != null) {
         throw new IllegalStateException("Attribute for this field is already set.");
      }

      this.attribute = attribute;
      return this;
   }

   public final String getName() {
      return this.name;
   }

   public final String getAttribute() {
      return this.attribute;
   }

   public int addCommandArguments(List<Object> args) {
      args.add(this.name);
      if (this.attribute == null) {
         return 1;
      }

      args.add(SearchProtocol.SearchKeyword.AS);
      args.add(this.attribute);
      return 3;
   }

   public int addCommandArguments(CommandArguments args) {
      args.add(this.name);
      if (this.attribute == null) {
         return 1;
      }

      args.add(SearchProtocol.SearchKeyword.AS);
      args.add(this.attribute);
      return 3;
   }

   @Override
   public void addParams(CommandArguments args) {
      this.addCommandArguments(args);
   }

   @Override
   public String toString() {
      return this.attribute == null ? this.name : this.name + " AS " + this.attribute;
   }

   public static FieldName of(String name) {
      return new FieldName(name);
   }

   public static FieldName[] convert(String... names) {
      if (names == null) {
         return null;
      }

      FieldName[] fields = new FieldName[names.length];

      for (int i = 0; i < names.length; i++) {
         fields[i] = of(names[i]);
      }

      return fields;
   }
}
