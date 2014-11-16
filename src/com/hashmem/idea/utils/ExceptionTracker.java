/*
 * ExceptionTracker
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.utils;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.hashmem.idea.HashMemBundle;
import com.hashmem.idea.remote.HttpService;
import com.hashmem.idea.service.SettingsService;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.SystemProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExceptionTracker {

    private static final Logger log = Logger.getInstance(ExceptionTracker.class);

    private static ExceptionTracker instance = null;

    public static synchronized ExceptionTracker getInstance() {
        if (instance == null) {
            instance = new ExceptionTracker();
        }

        return instance;
    }

    private ExceptionTracker() {}

    public void trackAndRethrow(Throwable t) {
        track(t);

        Throwables.propagate(t);
    }

    public void track(Throwable t) {
        try {
            doTrack(t);
        } catch (Throwable e) {
            log.error("Exception on tracking exception: " + e.getMessage(), t); // =(
        }
    }

    private void doTrack(Throwable e) throws IOException {
        HttpService.getInstance().post(HashMemBundle.message("server.exception_tracker"), new TrackedException(e).toJson());
    }

    private static class TrackedException {

        private Throwable e;

        private TrackedException(Throwable e) {
            this.e = e;
        }

        private Map getData() {
            Map<String, String> params = Maps.newHashMap();

            params.put("os.name", SystemProperties.getOsName());
            params.put("java.version", SystemProperties.getJavaVersion());
            params.put("java.vm.vendor", SystemProperties.getJavaVmVendor());

            ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
            ApplicationNamesInfo namesInfo = ApplicationNamesInfo.getInstance();
            Application application = ApplicationManager.getApplication();
            params.put("app.name", namesInfo.getProductName());
            params.put("app.name.full", namesInfo.getFullProductName());
            params.put("app.name.version", appInfo.getVersionName());
            params.put("app.eap", Boolean.toString(appInfo.isEAP()));
            params.put("app.internal", Boolean.toString(application.isInternal()));
            params.put("app.build", appInfo.getBuild().asString());
            params.put("app.version.major", appInfo.getMajorVersion());
            params.put("app.version.minor", appInfo.getMinorVersion());

            return params;
        }

        public String toJson() {
            Map answer = new HashMap();

            answer.put("app", SettingsService.APPLICATION_ID);
            answer.put("e", Throwables.getStackTraceAsString(e));
            answer.put("data", getData());

            return new Gson().toJson(answer);
        }
    }
}
