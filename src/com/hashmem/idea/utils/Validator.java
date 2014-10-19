/*
 * Validator
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.utils;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;

public class Validator {

    //copied from app.common.utils.js
    private static final Pattern LINKY_URL_REGEXP = Pattern.compile("^((ftp|https?):\\/\\/|(mailto:)?[A-Za-z0-9._%+-]+@)\\S*[^\\s\\.\\;\\,\\(\\)\\{\\}\\<\\>]$");

    public static boolean isLink(String text) {
        return !StringUtils.isEmpty(text) && LINKY_URL_REGEXP.matcher(text).matches();
    }

}
