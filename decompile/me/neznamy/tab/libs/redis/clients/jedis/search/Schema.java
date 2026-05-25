package me.neznamy.tab.libs.redis.clients.jedis.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.neznamy.tab.libs.redis.clients.jedis.CommandArguments;
import me.neznamy.tab.libs.redis.clients.jedis.params.IParams;

public class Schema {
   public final List<Schema.Field> fields = new ArrayList<>();

   public static Schema from(Schema.Field... fields) {
      Schema schema = new Schema();

      for (Schema.Field field : fields) {
         schema.addField(field);
      }

      return schema;
   }

   public Schema addTextField(String name, double weight) {
      this.fields.add(new Schema.TextField(name, weight));
      return this;
   }

   public Schema addSortableTextField(String name, double weight) {
      this.fields.add(new Schema.TextField(name, weight, true));
      return this;
   }

   public Schema addGeoField(String name) {
      this.fields.add(new Schema.Field(name, Schema.FieldType.GEO, false));
      return this;
   }

   public Schema addNumericField(String name) {
      this.fields.add(new Schema.Field(name, Schema.FieldType.NUMERIC, false));
      return this;
   }

   public Schema addSortableNumericField(String name) {
      this.fields.add(new Schema.Field(name, Schema.FieldType.NUMERIC, true));
      return this;
   }

   public Schema addTagField(String name) {
      this.fields.add(new Schema.TagField(name));
      return this;
   }

   public Schema addTagField(String name, String separator) {
      this.fields.add(new Schema.TagField(name, separator));
      return this;
   }

   public Schema addTagField(String name, boolean caseSensitive) {
      this.fields.add(new Schema.TagField(name, caseSensitive, false));
      return this;
   }

   public Schema addTagField(String name, String separator, boolean caseSensitive) {
      this.fields.add(new Schema.TagField(name, separator, caseSensitive, false));
      return this;
   }

   public Schema addSortableTagField(String name, String separator) {
      this.fields.add(new Schema.TagField(name, separator, true));
      return this;
   }

   public Schema addSortableTagField(String name, boolean caseSensitive) {
      this.fields.add(new Schema.TagField(name, caseSensitive, true));
      return this;
   }

   public Schema addSortableTagField(String name, String separator, boolean caseSensitive) {
      this.fields.add(new Schema.TagField(name, separator, caseSensitive, true));
      return this;
   }

   public Schema addVectorField(String name, Schema.VectorField.VectorAlgo algorithm, Map<String, Object> attributes) {
      this.fields.add(new Schema.VectorField(name, algorithm, attributes));
      return this;
   }

   public Schema addFlatVectorField(String name, Map<String, Object> attributes) {
      this.fields.add(new Schema.VectorField(name, Schema.VectorField.VectorAlgo.FLAT, attributes));
      return this;
   }

   public Schema addHNSWVectorField(String name, Map<String, Object> attributes) {
      this.fields.add(new Schema.VectorField(name, Schema.VectorField.VectorAlgo.HNSW, attributes));
      return this;
   }

   public Schema addField(Schema.Field field) {
      this.fields.add(field);
      return this;
   }

   public Schema as(String attribute) {
      this.fields.get(this.fields.size() - 1).as(attribute);
      return this;
   }

   @Override
   public String toString() {
      return "Schema{fields=" + this.fields + "}";
   }

   public static class Field implements IParams {
      protected final FieldName fieldName;
      protected final Schema.FieldType type;
      protected final boolean sortable;
      protected final boolean noIndex;

      public Field(String name, Schema.FieldType type) {
         this(name, type, false, false);
      }

      public Field(String name, Schema.FieldType type, boolean sortable) {
         this(name, type, sortable, false);
      }

      public Field(String name, Schema.FieldType type, boolean sortable, boolean noindex) {
         this(FieldName.of(name), type, sortable, noindex);
      }

      public Field(FieldName name, Schema.FieldType type) {
         this(name, type, false, false);
      }

      public Field(FieldName name, Schema.FieldType type, boolean sortable, boolean noIndex) {
         this.fieldName = name;
         this.type = type;
         this.sortable = sortable;
         this.noIndex = noIndex;
      }

      public void as(String attribute) {
         this.fieldName.as(attribute);
      }

      @Override
      public final void addParams(CommandArguments args) {
         this.fieldName.addParams(args);
         args.add(this.type.name());
         this.addTypeArgs(args);
         if (this.sortable) {
            args.add("SORTABLE");
         }

         if (this.noIndex) {
            args.add("NOINDEX");
         }
      }

      protected void addTypeArgs(CommandArguments args) {
      }

      @Override
      public String toString() {
         return "Field{name='" + this.fieldName + "', type=" + this.type + ", sortable=" + this.sortable + ", noindex=" + this.noIndex + "}";
      }
   }

   public enum FieldType {
      TAG,
      TEXT,
      GEO,
      NUMERIC,
      VECTOR;
   }

   public static class TagField extends Schema.Field {
      private final String separator;
      private final boolean caseSensitive;

      public TagField(String name) {
         this(name, null);
      }

      public TagField(String name, String separator) {
         this(name, separator, false);
      }

      public TagField(String name, boolean sortable) {
         this(name, null, sortable);
      }

      public TagField(String name, String separator, boolean sortable) {
         this(name, separator, false, sortable);
      }

      public TagField(String name, boolean caseSensitive, boolean sortable) {
         this(name, null, caseSensitive, sortable);
      }

      public TagField(String name, String separator, boolean caseSensitive, boolean sortable) {
         super(name, Schema.FieldType.TAG, sortable);
         this.separator = separator;
         this.caseSensitive = caseSensitive;
      }

      public TagField(FieldName name, String separator, boolean sortable) {
         this(name, separator, false, sortable);
      }

      public TagField(FieldName name, String separator, boolean caseSensitive, boolean sortable) {
         super(name, Schema.FieldType.TAG, sortable, false);
         this.separator = separator;
         this.caseSensitive = caseSensitive;
      }

      @Override
      public void addTypeArgs(CommandArguments args) {
         if (this.separator != null) {
            args.add("SEPARATOR");
            args.add(this.separator);
         }

         if (this.caseSensitive) {
            args.add("CASESENSITIVE");
         }
      }

      @Override
      public String toString() {
         return "TagField{name='"
            + this.fieldName
            + "', type="
            + this.type
            + ", sortable="
            + this.sortable
            + ", noindex="
            + this.noIndex
            + ", separator='"
            + this.separator
            + ", caseSensitive='"
            + this.caseSensitive
            + "'}";
      }
   }

   public static class TextField extends Schema.Field {
      private final double weight;
      private final boolean nostem;
      private final String phonetic;

      public TextField(String name) {
         this(name, 1.0);
      }

      public TextField(FieldName name) {
         this(name, 1.0, false, false, false, null);
      }

      public TextField(String name, double weight) {
         this(name, weight, false);
      }

      public TextField(String name, double weight, boolean sortable) {
         this(name, weight, sortable, false);
      }

      public TextField(String name, double weight, boolean sortable, boolean nostem) {
         this(name, weight, sortable, nostem, false);
      }

      public TextField(String name, double weight, boolean sortable, boolean nostem, boolean noindex) {
         this(name, weight, sortable, nostem, noindex, null);
      }

      public TextField(String name, double weight, boolean sortable, boolean nostem, boolean noindex, String phonetic) {
         super(name, Schema.FieldType.TEXT, sortable, noindex);
         this.weight = weight;
         this.nostem = nostem;
         this.phonetic = phonetic;
      }

      public TextField(FieldName name, double weight, boolean sortable, boolean nostem, boolean noindex, String phonetic) {
         super(name, Schema.FieldType.TEXT, sortable, noindex);
         this.weight = weight;
         this.nostem = nostem;
         this.phonetic = phonetic;
      }

      @Override
      protected void addTypeArgs(CommandArguments args) {
         if (this.weight != 1.0) {
            args.add("WEIGHT");
            args.add(Double.toString(this.weight));
         }

         if (this.nostem) {
            args.add("NOSTEM");
         }

         if (this.phonetic != null) {
            args.add("PHONETIC");
            args.add(this.phonetic);
         }
      }

      @Override
      public String toString() {
         return "TextField{name='"
            + this.fieldName
            + "', type="
            + this.type
            + ", sortable="
            + this.sortable
            + ", noindex="
            + this.noIndex
            + ", weight="
            + this.weight
            + ", nostem="
            + this.nostem
            + ", phonetic='"
            + this.phonetic
            + "'}";
      }
   }

   public static class VectorField extends Schema.Field {
      private final Schema.VectorField.VectorAlgo algorithm;
      private final Map<String, Object> attributes;

      public VectorField(String name, Schema.VectorField.VectorAlgo algorithm, Map<String, Object> attributes) {
         super(name, Schema.FieldType.VECTOR);
         this.algorithm = algorithm;
         this.attributes = attributes;
      }

      @Override
      public void addTypeArgs(CommandArguments args) {
         args.add(this.algorithm);
         args.add(this.attributes.size() << 1);

         for (Entry<String, Object> entry : this.attributes.entrySet()) {
            args.add(entry.getKey());
            args.add(entry.getValue());
         }
      }

      @Override
      public String toString() {
         return "VectorField{name='" + this.fieldName + "', type=" + this.type + ", algorithm=" + this.algorithm + ", attributes=" + this.attributes + "}";
      }

      public enum VectorAlgo {
         FLAT,
         HNSW;
      }
   }
}
