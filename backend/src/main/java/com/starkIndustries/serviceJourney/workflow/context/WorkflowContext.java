package com.starkIndustries.serviceJourney.workflow.context;

import java.util.HashMap;
import java.util.Map;

public class WorkflowContext {

    private final Map<String, Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    public Integer getInteger(String key) {
        Object value = data.get(key);

        if (value instanceof Integer) {
            return (Integer) value;
        }

        return null;
    }

    public Boolean getBoolean(String key) {
        Object value = data.get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return null;
    }

    public Map<String, Object> getAll() {
        return data;
    }
}