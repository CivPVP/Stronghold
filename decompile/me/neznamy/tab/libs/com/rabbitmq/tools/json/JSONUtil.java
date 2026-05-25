package me.neznamy.tab.libs.com.rabbitmq.tools.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONUtil {
   private static final Logger LOGGER = LoggerFactory.getLogger(JSONUtil.class);

   public static Object fill(Object target, Map<String, Object> source) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
      return fill(target, source, true);
   }

   public static Object fill(Object target, Map<String, Object> source, boolean useProperties) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
      if (useProperties) {
         BeanInfo info = Introspector.getBeanInfo(target.getClass());
         PropertyDescriptor[] props = info.getPropertyDescriptors();

         for (int i = 0; i < props.length; i++) {
            PropertyDescriptor prop = props[i];
            String name = prop.getName();
            Method setter = prop.getWriteMethod();
            if (setter != null && !Modifier.isStatic(setter.getModifiers())) {
               setter.invoke(target, source.get(name));
            }
         }
      }

      Field[] ff = target.getClass().getDeclaredFields();

      for (int i = 0; i < ff.length; i++) {
         Field field = ff[i];
         int fieldMod = field.getModifiers();
         if (Modifier.isPublic(fieldMod) && !Modifier.isFinal(fieldMod) && !Modifier.isStatic(fieldMod)) {
            try {
               field.set(target, source.get(field.getName()));
            } catch (IllegalArgumentException var9) {
            }
         }
      }

      return target;
   }

   public static void tryFill(Object target, Map<String, Object> source) {
      try {
         fill(target, source);
      } catch (IntrospectionException ie) {
         LOGGER.error("Error in tryFill", ie);
      } catch (IllegalAccessException iae) {
         LOGGER.error("Error in tryFill", iae);
      } catch (InvocationTargetException ite) {
         LOGGER.error("Error in tryFill", ite);
      }
   }
}
