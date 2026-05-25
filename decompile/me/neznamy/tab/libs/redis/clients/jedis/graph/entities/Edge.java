package me.neznamy.tab.libs.redis.clients.jedis.graph.entities;

import java.util.Objects;

@Deprecated
public class Edge extends GraphEntity {
   private String relationshipType;
   private long source;
   private long destination;

   public Edge() {
   }

   public Edge(int propertiesCapacity) {
      super(propertiesCapacity);
   }

   public String getRelationshipType() {
      return this.relationshipType;
   }

   public void setRelationshipType(String relationshipType) {
      this.relationshipType = relationshipType;
   }

   public long getSource() {
      return this.source;
   }

   public void setSource(long source) {
      this.source = source;
   }

   public long getDestination() {
      return this.destination;
   }

   public void setDestination(long destination) {
      this.destination = destination;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (!(o instanceof Edge)) {
         return false;
      }

      if (!super.equals(o)) {
         return false;
      }

      Edge edge = (Edge)o;
      return this.source == edge.source && this.destination == edge.destination && Objects.equals(this.relationshipType, edge.relationshipType);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.relationshipType, this.source, this.destination);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("Edge{");
      sb.append("relationshipType='").append(this.relationshipType).append('\'');
      sb.append(", source=").append(this.source);
      sb.append(", destination=").append(this.destination);
      sb.append(", id=").append(this.id);
      sb.append(", propertyMap=").append(this.propertyMap);
      sb.append('}');
      return sb.toString();
   }
}
