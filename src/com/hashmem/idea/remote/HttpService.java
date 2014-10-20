/*
 * HttpService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;

public class HttpService {

    private static HttpService instance = null;

    public static synchronized HttpService getInstance() {
        if (instance == null) {
            instance = new HttpService();
        }

        return instance;
    }

    private HttpService() {}

    public HttpResponse post(String url, String body) throws IOException {
        return Request.Post(url).bodyString(body, ContentType.APPLICATION_JSON).execute().returnResponse();
    }



}
