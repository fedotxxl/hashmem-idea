/*
 * SyncChangesOnServer
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.domain;

import java.util.ArrayList;
import java.util.List;

public class SyncChangesOnServer {

    List<String> updated;
    List<String> deleted;

    public SyncChangesOnServer() {
        this(null, null);
    }

    public SyncChangesOnServer(List<String> updated, List<String> deleted) {
        this.updated = (updated == null) ? new ArrayList<String>() : updated;
        this.deleted = (deleted == null) ? new ArrayList<String>() : deleted;
    }

    public List<String> getUpdated() {
        return updated;
    }

    public List<String> getDeleted() {
        return deleted;
    }
}
