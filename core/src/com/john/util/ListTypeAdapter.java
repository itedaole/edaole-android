
package com.john.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * GSON 解析泛型JsonArray适配器
 * 
 * @author zhaozhongyang
 * @2011-12-16 下午01:49:14
 * @param <T>
 */
public class ListTypeAdapter<T> implements JsonDeserializer<List<T>> {
    private Class<T> clazz;

    private Gson gson;

    public ListTypeAdapter(Class<T> clazz) {
        super();
        this.clazz = clazz;
        gson = new Gson();
    }

    @Override
    public List<T> deserialize(JsonElement je, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        List<T> list = new ArrayList<T>();
        JsonArray jsonArr = je.getAsJsonArray();
        for (Iterator<JsonElement> iter = jsonArr.iterator(); iter.hasNext();) {
            JsonObject jo = (JsonObject)iter.next();
            String str = jo.toString();
            T item = gson.fromJson(str, clazz);
            list.add(item);
        }
        return list;
    }
}
