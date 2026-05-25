package me.neznamy.tab.libs.redis.clients.jedis.graph.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Deprecated
public abstract class GraphEntity {
   protected long id;
   protected final Map<String, Property<?>> propertyMap;

   public GraphEntity() {
      this.propertyMap = new HashMap<>();
   }

   public GraphEntity(int propertiesCapacity) {
      this.propertyMap = new HashMap<>(propertiesCapacity);
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public void addProperty(String name, Object value) {
      this.addProperty(new Property<>(name, value));
   }

   public Set<String> getEntityPropertyNames() {
      return this.propertyMap.keySet();
   }

   public void addProperty(Property property) {
      this.propertyMap.put(property.getName(), property);
   }

   public int getNumberOfProperties() {
      return this.propertyMap.size();
   }

   public Property getProperty(String propertyName) {
      return this.propertyMap.get(propertyName);
   }

   public void removeProperty(String name) {
      this.propertyMap.remove(name);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (!(o instanceof GraphEntity)) {
         return false;
      }

      GraphEntity that = (GraphEntity)o;
      return this.id == that.id && Objects.equals(this.propertyMap, that.propertyMap);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.propertyMap);
   }

   @Override
   public abstract String toString();
}
