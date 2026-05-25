package me.neznamy.tab.libs.com.rabbitmq.tools.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** @deprecated */
public class JSONWriter {
   private boolean indentMode = false;
   private int indentLevel = 0;
   private final StringBuilder buf = new StringBuilder();
   static final char[] hex = "0123456789ABCDEF".toCharArray();

   public JSONWriter() {
   }

   public JSONWriter(boolean indenting) {
      this.indentMode = indenting;
   }

   public boolean getIndentMode() {
      return this.indentMode;
   }

   public void setIndentMode(boolean value) {
      this.indentMode = value;
   }

   private void newline() {
      if (this.indentMode) {
         this.add('\n');

         for (int i = 0; i < this.indentLevel; i++) {
            this.add(' ');
         }
      }
   }

   public String write(Object object) {
      this.buf.setLength(0);
      this.value(object);
      return this.buf.toString();
   }

   public String write(long n) {
      return this.write(Long.valueOf(n));
   }

   public Object write(double d) {
      return this.write(Double.valueOf(d));
   }

   public String write(char c) {
      return this.write(Character.valueOf(c));
   }

   public String write(boolean b) {
      return this.write(Boolean.valueOf(b));
   }

   private void value(Object object) {
      if (object == null) {
         this.add("null");
      } else if (object instanceof JSONSerializable) {
         ((JSONSerializable)object).jsonSerialize(this);
      } else if (object instanceof Class) {
         this.string(object);
      } else if (object instanceof Boolean) {
         this.bool((Boolean)object);
      } else if (object instanceof Number) {
         this.add(object);
      } else if (object instanceof String) {
         this.string(object);
      } else if (object instanceof Character) {
         this.string(object);
      } else if (object instanceof Map) {
         this.map((Map<String, Object>)object);
      } else if (object.getClass().isArray()) {
         this.array(object);
      } else if (object instanceof Collection) {
         this.array(((Collection)object).iterator());
      } else {
         this.bean(object);
      }
   }

   private void bean(Object object) {
      this.writeLimited(object.getClass(), object, null);
   }

   public void writeLimited(Class<?> klass, Object object, String[] properties) {
      Set<String> propertiesSet = null;
      if (properties != null) {
         propertiesSet = new HashSet<>();

         for (String p : properties) {
            propertiesSet.add(p);
         }
      }

      this.add('{');
      this.indentLevel += 2;
      this.newline();
      boolean needComma = false;

      BeanInfo info;
      try {
         info = Introspector.getBeanInfo(klass);
      } catch (IntrospectionException ie) {
         info = null;
      }

      if (info != null) {
         PropertyDescriptor[] props = info.getPropertyDescriptors();

         for (int i = 0; i < props.length; i++) {
            PropertyDescriptor prop = props[i];
            String name = prop.getName();
            if ((propertiesSet != null || !name.equals("class")) && (propertiesSet == null || propertiesSet.contains(name))) {
               Method accessor = prop.getReadMethod();
               if (accessor != null && !Modifier.isStatic(accessor.getModifiers())) {
                  try {
                     Object value = accessor.invoke(object, (Object[])null);
                     if (needComma) {
                        this.add(',');
                        this.newline();
                     }

                     needComma = true;
                     this.add(name, value);
                  } catch (Exception var14) {
                  }
               }
            }
         }
      }

      Field[] ff = object.getClass().getDeclaredFields();

      for (int i = 0; i < ff.length; i++) {
         Field field = ff[i];
         int fieldMod = field.getModifiers();
         String name = field.getName();
         if ((propertiesSet == null || propertiesSet.contains(name)) && !Modifier.isStatic(fieldMod)) {
            try {
               Object v = field.get(object);
               if (needComma) {
                  this.add(',');
                  this.newline();
               }

               needComma = true;
               this.add(name, v);
            } catch (Exception var13) {
            }
         }
      }

      this.indentLevel -= 2;
      this.newline();
      this.add('}');
   }

   private void add(String name, Object value) {
      this.add('"');
      this.add(name);
      this.add("\":");
      this.value(value);
   }

   private void map(Map<String, Object> map) {
      this.add('{');
      this.indentLevel += 2;
      this.newline();
      Iterator<String> it = map.keySet().iterator();
      if (it.hasNext()) {
         this.mapEntry(it.next(), map);
      }

      while (it.hasNext()) {
         this.add(',');
         this.newline();
         Object key = it.next();
         this.value(key);
         this.add(':');
         this.value(map.get(key));
      }

      this.indentLevel -= 2;
      this.newline();
      this.add('}');
   }

   private void mapEntry(Object key, Map<String, Object> map) {
      this.value(key);
      this.add(':');
      this.value(map.get(key));
   }

   private void array(Iterator<?> it) {
      this.add('[');
      if (it.hasNext()) {
         this.value(it.next());
      }

      while (it.hasNext()) {
         this.add(',');
         this.value(it.next());
      }

      this.add(']');
   }

   private void array(Object object) {
      this.add('[');
      int length = Array.getLength(object);
      if (length > 0) {
         this.value(Array.get(object, 0));
      }

      for (int i = 1; i < length; i++) {
         this.add(',');
         this.value(Array.get(object, i));
      }

      this.add(']');
   }

   private void bool(boolean b) {
      this.add(b ? "true" : "false");
   }

   private void string(Object obj) {
      this.add('"');
      CharacterIterator it = new StringCharacterIterator(obj.toString());

      for (char c = it.first(); c != '\uffff'; c = it.next()) {
         if (c == '"') {
            this.add("\\\"");
         } else if (c == '\\') {
            this.add("\\\\");
         } else if (c == '/') {
            this.add("\\/");
         } else if (c == '\b') {
            this.add("\\b");
         } else if (c == '\f') {
            this.add("\\f");
         } else if (c == '\n') {
            this.add("\\n");
         } else if (c == '\r') {
            this.add("\\r");
         } else if (c == '\t') {
            this.add("\\t");
         } else if (Character.isISOControl(c)) {
            this.unicode(c);
         } else {
            this.add(c);
         }
      }

      this.add('"');
   }

   private void add(Object obj) {
      this.buf.append(obj);
   }

   private void add(char c) {
      this.buf.append(c);
   }

   private void unicode(char c) {
      this.add("\\u");
      int n = c;

      for (int i = 0; i < 4; i++) {
         int digit = (n & 61440) >> 12;
         this.add(hex[digit]);
         n <<= 4;
      }
   }
}
