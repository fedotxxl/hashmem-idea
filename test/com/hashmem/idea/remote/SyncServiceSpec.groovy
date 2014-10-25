/*
 * SyncServiceSpec
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote

import com.hashmem.idea.domain.SyncNote
import com.hashmem.idea.domain.SyncResponse
import spock.lang.Specification

class SyncServiceSpec extends Specification {

    def "should correctly parse sync respone"() {
        expect:
        assert SyncResponse.fromJson(json) == syncResponse

        where:
        json | syncResponse
        '{"notes":[],"deletedNotes":[],"errors":null}'                                                                                                                               | syncNotes([], [], null)
        '{"notes":[],"deletedNotes":["c","d"],"errors":{"content.too-large":["a"],"key.invalid":["ac"]}}'                                                                            | syncNotes([], ["c", "d"], ['content.too-large': ['a'], 'key.invalid': ['ac']])
        '{"notes":[{"key":"efg","splittedKey":["efg"],"content":"hello","firstLine":"hello","lastUpdated":1414261397984}],"deletedNotes":["c","d","e","b","C","efc"],"errors":null}' | syncNotes([["key": "efg", "splittedKey": ["efg"], "content": "hello", "firstLine": "hello", "lastUpdated": 1414261397984]], ["c", "d", "e", "b", "C", "efc"], null)
    }

    private syncNotes(Collection<Map> notesAsMap, Collection<String> deletedNotes, Map<String, Collection<String>> errors) {
        def notes = notesAsMap?.collect {
            new SyncNote(key: it.key, content: it.content, lastUpdated: it.lastUpdated)
        }

        return new SyncResponse(notes, deletedNotes, errors)
    }
}
