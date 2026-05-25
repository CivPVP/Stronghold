package me.neznamy.tab.libs.com.rabbitmq.tools.json;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @deprecated */
public class JSONReader {
   private static final Object OBJECT_END = new Object();
   private static final Object ARRAY_END = new Object();
   private static final Object COLON = new Object();
   private static final Object COMMA = new Object();
   private static final Map<Character, Character> escapes = new HashMap<>();
   private CharacterIterator it;
   private char c;
   private Object token;
   private final StringBuilder buf = new StringBuilder();

   private char next() {
      this.c = this.it.next();
      return this.c;
   }

   private void skipWhiteSpace() {
      boolean cont;
      do {
         cont = true;
         if (Character.isWhitespace(this.c)) {
            this.next();
         } else if (this.c == '/' && this.next() == '/') {
            while (this.c != '\n') {
               this.next();
            }
         } else {
            cont = false;
         }
      } while (cont);
   }

   public Object read(String string) {
      this.it = new StringCharacterIterator(string);
      this.c = this.it.first();
      return this.read();
   }

   private Object read() {
      Object ret = null;
      this.skipWhiteSpace();
      if (this.c == '"' || this.c == '\'') {
         char sep = this.c;
         this.next();
         ret = this.string(sep);
      } else if (this.c == '[') {
         this.next();
         ret = this.array();
      } else if (this.c == ']') {
         ret = ARRAY_END;
         this.next();
      } else if (this.c == ',') {
         ret = COMMA;
         this.next();
      } else if (this.c == '{') {
         this.next();
         ret = this.object();
      } else if (this.c == '}') {
         ret = OBJECT_END;
         this.next();
      } else if (this.c == ':') {
         ret = COLON;
         this.next();
      } else if (this.c == 't' && this.next() == 'r' && this.next() == 'u' && this.next() == 'e') {
         ret = Boolean.TRUE;
         this.next();
      } else if (this.c == 'f' && this.next() == 'a' && this.next() == 'l' && this.next() == 's' && this.next() == 'e') {
         ret = Boolean.FALSE;
         this.next();
      } else if (this.c == 'n' && this.next() == 'u' && this.next() == 'l' && this.next() == 'l') {
         this.next();
      } else {
         if (!Character.isDigit(this.c) && this.c != '-') {
            throw new IllegalStateException(
               "Found invalid token while parsing JSON (around character " + (this.it.getIndex() - this.it.getBeginIndex()) + "): " + ret
            );
         }

         ret = this.number();
      }

      this.token = ret;
      return ret;
   }

   private Object object() {
      Map<String, Object> ret = new HashMap<>();
      String key = (String)this.read();

      while (this.token != OBJECT_END) {
         this.read();
         if (this.token != OBJECT_END) {
            ret.put(key, this.read());
            if (this.read() == COMMA) {
               key = (String)this.read();
            }
         }
      }

      return ret;
   }

   private Object array() {
      List<Object> ret = new ArrayList<>();
      Object value = this.read();

      while (this.token != ARRAY_END) {
         ret.add(value);
         if (this.read() == COMMA) {
            value = this.read();
         }
      }

      return ret;
   }

   private Object number() {
      this.buf.setLength(0);
      if (this.c == '-') {
         this.add();
      }

      this.addDigits();
      if (this.c == '.') {
         this.add();
         this.addDigits();
      }

      if (this.c == 'e' || this.c == 'E') {
         this.add();
         if (this.c == '+' || this.c == '-') {
            this.add();
         }

         this.addDigits();
      }

      String result = this.buf.toString();

      try {
         return Integer.valueOf(result);
      } catch (NumberFormatException nfe) {
         return Double.valueOf(result);
      }
   }

   private Object string(char sep) {
      this.buf.setLength(0);

      while (this.c != sep) {
         if (this.c == '\\') {
            this.next();
            if (this.c == 'u') {
               this.add(this.unicode());
            } else {
               Object value = escapes.get(this.c);
               if (value != null) {
                  this.add((Character)value);
               } else {
                  this.add();
               }
            }
         } else {
            this.add();
         }
      }

      this.next();
      return this.buf.toString();
   }

   private void add(char cc) {
      this.buf.append(cc);
      this.next();
   }

   private void add() {
      this.add(this.c);
   }

   private void addDigits() {
      while (Character.isDigit(this.c)) {
         this.add();
      }
   }

   private char unicode() {
      int value = 0;

      for (int i = 0; i < 4; i++) {
         switch (this.next()) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
               value = (value << 4) + this.c - 48;
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '[':
            case '\\':
            case ']':
            case '^':
            case '_':
            case '`':
            default:
               break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
               value = (value << 4) + this.c - 65 + 10;
               break;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
               value = (value << 4) + this.c - 97 + 10;
         }
      }

      return (char)value;
   }

   static {
      escapes.put('"', '"');
      escapes.put('\\', '\\');
      escapes.put('/', '/');
      escapes.put('b', '\b');
      escapes.put('f', '\f');
      escapes.put('n', '\n');
      escapes.put('r', '\r');
      escapes.put('t', '\t');
   }
}
