/*
 * SyncResponse
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.domain;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyncResponse {

    List<SyncNote> notes;
    List<String> deletedNotes;
    Map<String, List<String>> errors;
    SyncChangesOnServer syncChangesOnServer;

    public SyncResponse(List<SyncNote> notes, List<String> deletedNotes, Map<String, List<String>> errors) {
        this(notes, deletedNotes, errors, new SyncChangesOnServer());
    }

    public SyncResponse(List<SyncNote> notes, List<String> deletedNotes, Map<String, List<String>> errors, SyncChangesOnServer syncChangesOnServer) {
        this.notes = notes;
        this.deletedNotes = deletedNotes;
        this.errors = errors;
        this.syncChangesOnServer = syncChangesOnServer;
    }

    public List<SyncNote> getNotes() {
        return (notes == null) ? new ArrayList<SyncNote>() : notes;
    }

    public List<String> getDeletedNotes() {
        return (deletedNotes == null) ? new ArrayList<String>() : deletedNotes;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public SyncChangesOnServer getSyncChangesOnServer() {
        return syncChangesOnServer;
    }

    public static SyncResponse fromJson(String json) {
        return new Gson().fromJson(json, SyncResponse.class);
    }

    public boolean isNothingToUpdate() {
        return (notes == null || notes.size() == 0) && (deletedNotes == null || deletedNotes.size() == 0);
    }

    public boolean hasErrors() {
        return errors != null && errors.keySet().size() > 0;
    }

    public int getChanged() {
        return syncChangesOnServer.deleted.size() + syncChangesOnServer.updated.size();
    }

    public boolean hasChanged() {
        return getChanged() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SyncResponse)) return false;

        SyncResponse that = (SyncResponse) o;

        if (errors != null ? !errors.equals(that.errors) : that.errors != null) return false;
        if (notes != null ? !notes.equals(that.notes) : that.notes != null) return false;
        if (deletedNotes != null ? !deletedNotes.equals(that.deletedNotes) : that.deletedNotes != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = notes != null ? notes.hashCode() : 0;
        result = 31 * result + (deletedNotes != null ? deletedNotes.hashCode() : 0);
        result = 31 * result + (errors != null ? errors.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SyncResponse{" +
                "notes=" + notes +
                ", deletedNotes=" + deletedNotes +
                ", errors=" + errors +
                '}';
    }
}
