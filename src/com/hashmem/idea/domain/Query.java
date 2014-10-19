/*
 * Query
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.domain;

public class Query {
    private String prefix;
    private String key;
    private Type type = null;

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
        return prefix.isEmpty() && key.isEmpty();
    }

    public boolean isEmptyKey() {
        return key.isEmpty();
    }

    public String getPrefix() {
        return prefix;
    }

    public String getKey() {
        return key;
    }

    public boolean isCommandPrefix() {
        return getType() == Type.COMMAND;
    }

    public Type getType() {
        if (type == null) {
            type = (prefix.isEmpty()) ? Type.OPEN : Type.myValueOf(prefix.charAt(0));
        }

        return type;
    }

    @Override
    public String toString() {
        return prefix + key;
    }

    public static enum Type {
        OPEN, OPEN_SITE('/'), DELETE('-'), CREATE('+'), COMMAND(':');

        private char prefix;

        Type() {
        }

        Type(char prefix) {
            this.prefix = prefix;
        }

        public static Type myValueOf(char prefix) {
            for (Type t : values()) {
                if (t.prefix == prefix) return t;
            }

            return OPEN;
        }
    }
}