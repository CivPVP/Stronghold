package me.neznamy.tab.shared.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

public class ReflectionUtils {
   public static boolean classExists(@NotNull String path) {
      try {
         Class.forName(path);
         return true;
      } catch (Throwable e) {
         return false;
      }
   }

   public static boolean methodExists(@NotNull Class<?> clazz, @NotNull String method, @NotNull Class<?>... parameterTypes) {
      try {
         clazz.getMethod(method, parameterTypes);
         return true;
      } catch (NoSuchMethodException e) {
         return false;
      }
   }

   @NotNull
   public static List<Field> getFields(@NotNull Class<?> clazz, @NotNull Class<?> type) {
      List<Field> list = new ArrayList<>();

      for (Field field : clazz.getDeclaredFields()) {
         if (field.getType() == type) {
            list.add(setAccessible(field));
         }
      }

      return list;
   }

   @NotNull
   public static List<Field> getInstanceFields(@NotNull Class<?> clazz, @NotNull Class<?> fieldType) {
      List<Field> list = new ArrayList<>();

      for (Field field : clazz.getDeclaredFields()) {
         if (field.getType() == fieldType && !Modifier.isStatic(field.getModifiers())) {
            list.add(setAccessible(field));
         }
      }

      return list;
   }

   @NotNull
   public static <T extends AccessibleObject> T setAccessible(@NotNull T o) {
      o.setAccessible(true);
      return o;
   }

   @NotNull
   public static Constructor<?> getOnlyConstructor(@NotNull Class<?> clazz) {
      Constructor<?>[] constructors = clazz.getConstructors();
      if (constructors.length != 1) {
         throw new IllegalStateException(
            "Class "
               + clazz.getName()
               + " is expected to have 1 constructor, but has "
               + constructors.length
               + ": \n"
               + Arrays.stream(constructors).map(Constructor::toString).collect(Collectors.joining("\n"))
         );
      } else {
         return constructors[0];
      }
   }

   @NotNull
   public static Field getOnlyField(@NotNull Class<?> clazz, @NotNull Class<?> type) {
      List<Field> list = new ArrayList<>();

      for (Field field : clazz.getDeclaredFields()) {
         if (field.getType() == type) {
            list.add(setAccessible(field));
         }
      }

      if (list.size() != 1) {
         throw new IllegalStateException(
            "Class "
               + clazz.getName()
               + " is expected to have 1 field of type "
               + type.getName()
               + ", but has "
               + list.size()
               + ": "
               + list.stream().map(Field::getName).collect(Collectors.toList())
         );
      } else {
         return list.get(0);
      }
   }

   @NotNull
   public static Field getField(@NotNull Class<?> clazz, @NotNull String... names) {
      for (String name : names) {
         try {
            return setAccessible(clazz.getDeclaredField(name));
         } catch (NoSuchFieldException var7) {
         }
      }

      throw new IllegalArgumentException("Class " + clazz.getName() + " does not contain a field with potential names " + Arrays.toString(names));
   }

   @Generated
   private ReflectionUtils() {
   }
}
