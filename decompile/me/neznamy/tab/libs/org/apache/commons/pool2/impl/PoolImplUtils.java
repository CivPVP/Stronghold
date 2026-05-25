package me.neznamy.tab.libs.org.apache.commons.pool2.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import me.neznamy.tab.libs.org.apache.commons.pool2.PooledObjectFactory;

class PoolImplUtils {
   static Class<?> getFactoryType(Class<? extends PooledObjectFactory> factoryClass) {
      Class<PooledObjectFactory> type = PooledObjectFactory.class;
      Object genericType = getGenericType(type, factoryClass);
      if (genericType instanceof Integer) {
         ParameterizedType pi = getParameterizedType(type, factoryClass);
         if (pi != null) {
            Type[] bounds = ((TypeVariable)pi.getActualTypeArguments()[(Integer)genericType]).getBounds();
            if (bounds != null && bounds.length > 0) {
               Type bound0 = bounds[0];
               if (bound0 instanceof Class) {
                  return (Class<?>)bound0;
               }
            }
         }

         return Object.class;
      } else {
         return (Class<?>)genericType;
      }
   }

   private static <T> Object getGenericType(Class<T> type, Class<? extends T> clazz) {
      if (type != null && clazz != null) {
         ParameterizedType pi = getParameterizedType(type, clazz);
         if (pi != null) {
            return getTypeParameter(clazz, pi.getActualTypeArguments()[0]);
         } else {
            Class<? extends T> superClass = (Class<? extends T>)clazz.getSuperclass();
            Object result = getGenericType(type, superClass);
            if (result instanceof Class) {
               return result;
            } else if (result instanceof Integer) {
               ParameterizedType superClassType = (ParameterizedType)clazz.getGenericSuperclass();
               return getTypeParameter(clazz, superClassType.getActualTypeArguments()[(Integer)result]);
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static <T> ParameterizedType getParameterizedType(Class<T> type, Class<? extends T> clazz) {
      for (Type iface : clazz.getGenericInterfaces()) {
         if (iface instanceof ParameterizedType) {
            ParameterizedType pi = (ParameterizedType)iface;
            if (pi.getRawType() instanceof Class && type.isAssignableFrom((Class<?>)pi.getRawType())) {
               return pi;
            }
         }
      }

      return null;
   }

   private static Object getTypeParameter(Class<?> clazz, Type argType) {
      if (argType instanceof Class) {
         return argType;
      }

      TypeVariable<?>[] tvs = clazz.getTypeParameters();

      for (int i = 0; i < tvs.length; i++) {
         if (tvs[i].equals(argType)) {
            return i;
         }
      }

      return null;
   }

   static boolean isPositive(Duration delay) {
      return delay != null && !delay.isNegative() && !delay.isZero();
   }

   static Instant max(Instant a, Instant b) {
      return a.compareTo(b) > 0 ? a : b;
   }

   static Instant min(Instant a, Instant b) {
      return a.compareTo(b) < 0 ? a : b;
   }

   static Duration nonNull(Duration value, Duration defaultValue) {
      return value != null ? value : Objects.requireNonNull(defaultValue, "defaultValue");
   }

   static ChronoUnit toChronoUnit(TimeUnit timeUnit) {
      switch ((TimeUnit)Objects.requireNonNull(timeUnit)) {
         case NANOSECONDS:
            return ChronoUnit.NANOS;
         case MICROSECONDS:
            return ChronoUnit.MICROS;
         case MILLISECONDS:
            return ChronoUnit.MILLIS;
         case SECONDS:
            return ChronoUnit.SECONDS;
         case MINUTES:
            return ChronoUnit.MINUTES;
         case HOURS:
            return ChronoUnit.HOURS;
         case DAYS:
            return ChronoUnit.DAYS;
         default:
            throw new IllegalArgumentException(timeUnit.toString());
      }
   }

   static Duration toDuration(long amount, TimeUnit timeUnit) {
      return Duration.of(amount, toChronoUnit(timeUnit));
   }
}
