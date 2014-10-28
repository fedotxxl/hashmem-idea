/*
 * Query
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.domain;

import org.apache.commons.lang.StringUtils;

public class Query {
    private String prefix;
    private String key;
    private String content = null;
    private Type type = null;

    public Query(String query) {
        if (StringUtils.isEmpty(query)) {
            prefix = "";
            key = "";
        } else {
            query = query.trim();
            char first = query.charAt(0);

            if (first == '+' || first == '-' || first == '/' || first == ':') {
                prefix = String.valueOf(first);
                query = query.substring(1);
            } else {
                prefix = "";
            }

            int contentDelimiter = query.indexOf(" ");
            if (contentDelimiter > 0) {
                key = query.substring(0, contentDelimiter);
                content = query.substring(contentDelimiter + 1);
            } else {
                key = query;
            }
        }
    }

    public Query(String prefix, String key) {
        this.prefix = prefix;
        this.key = key;
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(prefix) && StringUtils.isEmpty(key);
    }

    public boolean isEmptyKey() {
        return StringUtils.isEmpty(key);
    }

    public String getPrefix() {
        return prefix;
    }

    public String getKey() {
        return key;
    }

    public String getContent() {
        return content;
    }

    public boolean isCommandPrefix() {
        return getType() == Type.COMMAND;
    }

    public boolean isValid() {
        Type type = getType();
        boolean isValidKey = NoteKey.isValid(key);
        boolean isEmptyContent = (content == null);

        if (type == Type.CREATE) {
            return isValidKey;
        } else if (type == Type.OPEN || type == Type.DELETE) {
            return isValidKey && isEmptyContent;
        } else if (type == Type.OPEN_SITE) {
            return (isEmptyKey() || isValidKey) && isEmptyContent;
        } else if (type == Type.COMMAND) {
            return isEmptyContent;
        } else {
            return false;
        }
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