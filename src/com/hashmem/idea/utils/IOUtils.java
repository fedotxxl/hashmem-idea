/*
 * IOUtils
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.utils;

import java.io.InputStream;

public class IOUtils {

    //http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
    public static String toString(InputStream stream) {
        java.util.Scanner s = new java.util.Scanner(stream, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
