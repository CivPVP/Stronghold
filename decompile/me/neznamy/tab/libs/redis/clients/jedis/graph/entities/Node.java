package me.neznamy.tab.libs.redis.clients.jedis.graph.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Deprecated
public class Node extends GraphEntity {
   private final List<String> labels;

   public Node() {
      this.labels = new ArrayList<>();
   }

   public Node(int labelsCapacity, int propertiesCapacity) {
      super(propertiesCapacity);
      this.labels = new ArrayList<>(labelsCapacity);
   }

   public void addLabel(String label) {
      this.labels.add(label);
   }

   public void removeLabel(String label) {
      this.labels.remove(label);
   }

   public String getLabel(int index) {
      return this.labels.get(index);
   }

   public int getNumberOfLabels() {
      return this.labels.size();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (!(o instanceof Node)) {
         return false;
      }

      if (!super.equals(o)) {
         return false;
      }

      Node node = (Node)o;
      return Objects.equals(this.labels, node.labels);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.labels);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("Node{");
      sb.append("labels=").append(this.labels);
      sb.append(", id=").append(this.id);
      sb.append(", propertyMap=").append(this.propertyMap);
      sb.append('}');
      return sb.toString();
   }
}
