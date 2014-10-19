/*
 * OneTimeContainer
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OneTimeContainer<T> {

    private ConcurrentMap<T, Boolean> map = new ConcurrentHashMap<T, Boolean>();

    public void put(T o) {
        map.put(o, Boolean.TRUE);
    }

    public synchronized boolean checkContainsAndRemove(T o) {
        Boolean has = map.get(o);

        if (has != null && has == Boolean.TRUE) {
            map.put(o, Boolean.FALSE);
            return true;
        } else {
            return false;
        }
    }

    public void checkAndDo(T o, boolean contains, Runnable runnable) {
        if (checkContainsAndRemove(o) == contains) {
            runnable.run();
        }
    }
}
