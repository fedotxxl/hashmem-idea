/*
 * Query
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

public class Query {
    private String prefix;
    private String key;

    public Query(String query) {
        if (query == null || query.trim().isEmpty()) {
            prefix = "";
            key = "";
        } else {
            query = query.trim();
            char first = query.charAt(0);

            if (first == '+' || first == '-' || first == '/' || first == ':') {
                prefix = String.valueOf(first);
                key = query.substring(1);
            } else {
                prefix = "";
                key = query;
            }
        }
    }

    public Query(String prefix, String key) {
        this.prefix = prefix;
        this.key = key;
    }

    public boolean isEmpty() {
        return key.isEmpty();
    }

    public String getPrefix() {
        return prefix;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return prefix + key;
    }
}