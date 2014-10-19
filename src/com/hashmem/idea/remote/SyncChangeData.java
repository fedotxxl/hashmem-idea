/*
 * SyncChangeData
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyncChangeData {

    private Map<String, Long> deleted = Maps.newHashMap();
    private Map<String, Long> updated = Maps.newHashMap();

    void markAsUpdated(String key, long date) {
        updated.put(key, date);
        deleted.remove(key);
    }

    void markAsDeleted(String key, long date) {
        deleted.put(key, date);
        updated.remove(key);
    }

    void forget(String key) {
        updated.remove(key);
        deleted.remove(key);
    }

    void forgetAll() {
        deleted = Maps.newHashMap();
        updated = Maps.newHashMap();
    }

    List<String> getUpdatedSince(long since) {
        return getSince(updated, since);
    }

    List<String> getDeletedSince(long since) {
        return getSince(deleted, since);
    }

    Long getLastUpdated(String key) {
        return updated.containsKey(key) ? updated.get(key) : deleted.get(key);
    }

    private List<String> getSince(Map<String, Long> objects, long since) {
        List<String> answer = new ArrayList<String>();

        for (Map.Entry<String, Long> e : objects.entrySet()) {
            if (e.getValue() > since) {
                answer.add(e.getKey());
            }
        }

        return answer;
    }

}
