package me.neznamy.tab.libs.redis.clients.jedis.graph.entities;

import java.util.List;
import java.util.Objects;

@Deprecated
public final class Path {
   private final List<Node> nodes;
   private final List<Edge> edges;

   public Path(List<Node> nodes, List<Edge> edges) {
      this.nodes = nodes;
      this.edges = edges;
   }

   public List<Node> getNodes() {
      return this.nodes;
   }

   public List<Edge> getEdges() {
      return this.edges;
   }

   public int length() {
      return this.edges.size();
   }

   public int nodeCount() {
      return this.nodes.size();
   }

   public Node firstNode() {
      return this.nodes.get(0);
   }

   public Node lastNode() {
      return this.nodes.get(this.nodes.size() - 1);
   }

   public Node getNode(int index) {
      return this.nodes.get(index);
   }

   public Edge getEdge(int index) {
      return this.edges.get(index);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Path path = (Path)o;
         return Objects.equals(this.nodes, path.nodes) && Objects.equals(this.edges, path.edges);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.nodes, this.edges);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("Path{");
      sb.append("nodes=").append(this.nodes);
      sb.append(", edges=").append(this.edges);
      sb.append('}');
      return sb.toString();
   }
}
