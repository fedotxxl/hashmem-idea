/*
 * NoteKey
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.domain;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class NoteKey {

    private static final Pattern VALIDATE_KEY = Pattern.compile("^[0-9а-яА-Яa-zA-Z][0-9а-яА-Яa-zA-Z\\.\\-_]*$");

    public static boolean isValid(String key) {
        return !StringUtils.isEmpty(key) && VALIDATE_KEY.matcher(key).matches();
    }


}
