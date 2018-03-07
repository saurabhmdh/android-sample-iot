package com.santiance.test.data;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by saurabh.khare on 2018/03/07.
 */

public class CacheManager {

    private static final CacheManager instance = new CacheManager();

    private final Map<String, String> cacheData = new ConcurrentHashMap<>();

    private CacheManager() {}

    public static CacheManager getInstance() {
        return instance;
    }

    public <T> T get(String key, Class<T> valueType) {
        if (!cacheData.containsKey(key)) {
            return null;
        }
        Gson gson = new Gson();
        String data = cacheData.get(key);
        return gson.fromJson(data, valueType);
    }

    public <T> void put(String key, T value, Class<T> valueType) {
        Gson gson = new Gson();
        String data = gson.toJson(value, valueType);
        cacheData.put(key, data);
    }

    public <T> void putList(String key, List<T> list) {
        Type listType = new TypeToken<List<T>>() {}.getType();
        Gson gson = new Gson();
        String json = gson.toJson(list, listType);
        cacheData.put(key, json);
    }

    public <T> List<T> getList(String key) {
        if (!cacheData.containsKey(key)) {
            return null;
        }
        Gson gson = new Gson();
        String data = cacheData.get(key);
        return gson.fromJson(data, new TypeToken<List<T>>() {}.getType());
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        if (!cacheData.containsKey(key)) {
            return null;
        }
        Gson gson = new Gson();
        String data = cacheData.get(key);
        List<T> list = new ArrayList<>();
        JsonArray elements = new JsonParser().parse(data).getAsJsonArray();
        for (JsonElement jsonElement : elements) {
            list.add(gson.fromJson(jsonElement, clazz));
        }
        return list;
    }
    public void clearCache() {
        cacheData.clear();
    }

    @SuppressWarnings("UnusedReturnValue")
    public String removeCache(String key) {
        if (!cacheData.containsKey(key)) {
            return null;
        }
        cacheData.remove(key);
        return key;
    }


    public String getString(@Nullable String key) {
        return cacheData.get(key);
    }

    @SuppressWarnings("UnusedReturnValue")
    public String putString(String key, String value) {
        cacheData.put(key, value);
        return key;
    }
}

