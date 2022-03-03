package dotnet54.classifiers.tschief.results;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dotnet54.applications.tschief.TSChiefOptions;
import dotnet54.classifiers.tschief.TSChiefNode;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.DateFormat;

public class JsonHelper {

    public JsonHelper(){

    }

    public transient static Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .serializeSpecialFloatingPointValues()
            .setDateFormat(DateFormat.LONG)
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
//            .setPrettyPrinting()
            .setVersion(1.0)
            .create();

    /* Gson serialization related */
    public static class ClassTypeAdapter extends TypeAdapter<Class<?>> {
        @Override
        public void write(JsonWriter jsonWriter, Class<?> clazz) throws IOException {

            try {
                StringBuilder buffer = new StringBuilder();

                if(clazz == null){
                    jsonWriter.nullValue();
                    return;
                }

                jsonWriter.beginObject();
                Field[] fields = clazz.getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);
                    boolean isTransient = Modifier.isTransient(field.getModifiers());
                    if (isTransient) {
                        continue;
                    }

                    Object value = field.get(this);
                    if (field.getType().isArray() & value != null) {
                        buffer.setLength(0);
                        buffer.append('[');
                        int length = Array.getLength(value);
                        jsonWriter.name(field.getName() + "["+ length + "]");

                        for (int i = 0; i < length; i ++) {
                            Object arrayElement = Array.get(value, i);
                            buffer.append(arrayElement.toString());
                            if (i != length -1) {
                                buffer.append(',');
                            }
                        }
                        buffer.append(']');
                        jsonWriter.value(buffer.toString());
                    }else {
                        jsonWriter.name(field.getName());
                        if (value != null) {
                            jsonWriter.value(value.toString());
                        }else {
                            jsonWriter.nullValue();
                        }
                    }

                }
                jsonWriter.endObject();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public Class<?> read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            Class<?> clazz = null;
            try {
                clazz = Class.forName(jsonReader.nextString());
            } catch (ClassNotFoundException exception) {
                throw new IOException(exception);
            }
            return clazz;
        }
    }


    public static class ObjectTypeAdapter extends TypeAdapter<Object> {
        @Override
        public void write(JsonWriter jsonWriter, Object obj) throws IOException {

            try {
                StringBuilder buffer = new StringBuilder();

                if(obj == null){
                    jsonWriter.nullValue();
                    return;
                }

                jsonWriter.beginObject();
                Field[] fields = obj.getClass().getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);

                    int modifiers = field.getModifiers();
                    if (Modifier.isTransient(modifiers)) {
                        continue;
                    }

                    Object value;
//                    if (Modifier.isStatic(modifiers)){
//                        value = field.get(null);
//                    }else{
                        value = field.get(obj);
//                    }

                    if (field.getType().isArray() & value != null) {
                        buffer.setLength(0);
                        buffer.append('[');
                        int length = Array.getLength(value);
                        jsonWriter.name(field.getName() + "["+ length + "]");

                        for (int i = 0; i < length; i ++) {
                            Object arrayElement = Array.get(value, i);
                            buffer.append(arrayElement.toString());
                            if (i != length -1) {
                                buffer.append(',');
                            }
                        }
                        buffer.append(']');
                        jsonWriter.value(buffer.toString());
                    }else {
                        jsonWriter.name(field.getName());
                        if (value != null) {
                            jsonWriter.value(value.toString());
                        }else {
                            jsonWriter.nullValue();
                        }
                    }

                }
                jsonWriter.endObject();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public Class<?> read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            Class<?> clazz = null;
            try {
                clazz = Class.forName(jsonReader.nextString());
            } catch (ClassNotFoundException exception) {
                throw new IOException(exception);
            }
            return clazz;
        }
    }

    public static class ClassTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            if(!Class.class.isAssignableFrom(typeToken.getRawType())) {
                return null;
            }
            return (TypeAdapter<T>) new ClassTypeAdapter();
        }

    }


    public static TypeAdapter<Number> doubleAdapterWithSpecialCharSupport() {
        return new TypeAdapter<Number>() {
            @Override public Double read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                return in.nextDouble();
            }
            @Override public void write(JsonWriter out, Number value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                double doubleValue = value.doubleValue();

//                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
//                    throw new IllegalArgumentException(value
//                            + " is not a valid double value as per JSON specification. To override this"
//                            + " behavior, use GsonBuilder.serializeSpecialFloatingPointValues() method.");
//                }

                if (Double.isNaN(doubleValue)) {
                    out.value("NaN");
                } else if (Double.isInfinite(doubleValue)){
                    out.value("Infinity");
                }else{
                    out.value(value);
                }
            }
        };
    }

    public static class TIntObjectMapSerializer implements JsonSerializer<TIntObjectMap<TSChiefNode>> {
        @Override
        public JsonElement serialize(TIntObjectMap src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject map = new JsonObject();
            int[] keys = src.keys();
            for (int i = 0; i < keys.length; i++) {
                map.add(keys[i] + "", context.serialize(src.get(keys[i])) );
            }
            return map;
        }
    }

    public static class TIntObjectMapSerializer2<T> implements JsonSerializer<TIntObjectMap<T>> {
        @Override
        public JsonElement serialize(TIntObjectMap src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject map = new JsonObject();
            int[] keys = src.keys();
            for (int i = 0; i < keys.length; i++) {
                map.add(keys[i] + "", context.serialize(src.get(keys[i])) );
            }
            return map;
        }
    }


    public static class TIntIntMapSerializer implements JsonSerializer<TIntIntMap> {
        @Override
        public JsonElement serialize(TIntIntMap src, Type typeOfSrc, JsonSerializationContext context) {

            JsonObject map = new JsonObject();
            int[] keys = src.keys();
            for (int i = 0; i < keys.length; i++) {
                map.add(keys[i] + "", new JsonPrimitive(src.get(keys[i])) );
            }

            return map;
        }
    }

}
