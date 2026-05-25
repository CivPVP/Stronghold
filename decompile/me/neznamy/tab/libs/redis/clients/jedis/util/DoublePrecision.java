package me.neznamy.tab.libs.redis.clients.jedis.util;

public final class DoublePrecision {
   private DoublePrecision() {
      throw new InstantiationError("Must not instantiate this class");
   }

   public static Double parseFloatingPointNumber(String str) throws NumberFormatException {
      if (str == null) {
         return null;
      }

      try {
         return Double.valueOf(str);
      } catch (NumberFormatException e) {
         switch (str) {
            case "inf":
            case "+inf":
               return Double.POSITIVE_INFINITY;
            case "-inf":
               return Double.NEGATIVE_INFINITY;
            case "nan":
            case "-nan":
               return Double.NaN;
            default:
               throw e;
         }
      }
   }

   public static Double parseEncodedFloatingPointNumber(Object val) throws NumberFormatException {
      if (val == null) {
         return null;
      } else {
         return val instanceof Double ? (Double)val : parseFloatingPointNumber((String)val);
      }
   }
}
