package me.neznamy.tab.libs.com.rabbitmq.client;

import java.io.IOException;

public class UnknownClassOrMethodId extends IOException {
   private static final long serialVersionUID = 1L;
   private static final int NO_METHOD_ID = -1;
   public final int classId;
   public final int methodId;

   public UnknownClassOrMethodId(int classId) {
      this(classId, -1);
   }

   public UnknownClassOrMethodId(int classId, int methodId) {
      this.classId = classId;
      this.methodId = methodId;
   }

   @Override
   public String toString() {
      return this.methodId == -1 ? super.toString() + "<" + this.classId + ">" : super.toString() + "<" + this.classId + "." + this.methodId + ">";
   }
}
