/*
 * HttpService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpService {

    private static HttpService instance = null;

    public static synchronized HttpService getInstance() {
        if (instance == null) {
            instance = new HttpService();
        }

        return instance;
    }

    private HttpService() {}

    public HttpResponse get(String url) throws IOException  {
        return execute(new GetMethod(url));
    }

    public HttpResponse post(String url) throws IOException {
        return post(url, null);
    }

    public HttpResponse post(String url, String body) throws IOException {
        PostMethod postMethod = new PostMethod(url);

        if (body != null) {
            StringRequestEntity requestEntity = new StringRequestEntity(
                    body,
                    "application/json",
                    "UTF-8");

            postMethod.setRequestEntity(requestEntity);
        }

        return execute(postMethod);
    }

    private HttpResponse execute(HttpMethod method) throws IOException {
        try {
            HttpClient httpClient = new HttpClient();
            int responseStatusCode = httpClient.executeMethod(method);
            InputStream stream = method.getResponseBodyAsStream();
            String responseBody = CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));

            stream.close();

            return new HttpResponse(responseStatusCode, responseBody);
        } finally {
            method.releaseConnection();
        }
    }


}
