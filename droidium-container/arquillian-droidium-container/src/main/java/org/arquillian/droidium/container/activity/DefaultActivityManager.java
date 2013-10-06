/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.droidium.container.activity;

import org.arquillian.droidium.container.api.ActivityManager;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.jboss.arquillian.core.spi.Validate;

/**
 * Manages activities in case there is not native plugin on the class path, we are able to start and stop activities anyway.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DefaultActivityManager implements ActivityManager {

    private AndroidDevice device;

    public DefaultActivityManager(AndroidDevice device) {
        Validate.notNull(device, "Android device via which you want to manage activies can not be a null object!");
        this.device = device;
    }

    @Override
    public void startActivity(String activity) {
        Validate.notNullOrEmpty(activity, "Activity you want to start can not be a null object nor an empty string!");
        device.executeShellCommand("am start -n " + getActivityComponent(activity));
    }

    @Override
    public void startActivity(Class<?> activity) {
        Validate.notNull(activity, "Activity you want to start can not be a null object!");
        startActivity(activity.getName());
    }

    @Override
    public void stopActivity(String activity) {
        Validate.notNullOrEmpty(activity, "Activity you want to stop can not be a null object nor an empty string!");
        if (!activity.contains(".")) {
            throw new IllegalArgumentException("Stopping of activity equals to killing the package it belongs to. It seems "
                + "you have not specified FQDN of activity you want to stop so package name can not be extracted from it.");
        }
        device.executeShellCommand("am kill " + activity.substring(0, activity.lastIndexOf(".")));
    }

    @Override
    public void stopActivity(Class<?> activity) {
        Validate.notNull(activity, "Activity you want to stop can not be a null object!");
        stopActivity(activity.getName().substring(0, activity.getName().lastIndexOf(".")));
    }

    public static String getActivityComponent(String activity) {
        // string like some.package.name/some.other.package.SomeActivity
        if (activity.matches("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*"
            + "/([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*")) {
            return activity;
        }

        // string in form some.package.name/.SomeActivity
        if (activity.matches("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*/\\.\\w+")) {
            return activity;
        }

        // string if form some.package.name.SomeActivity
        if (activity.matches("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*") && activity.contains(".")) {
            // return some.package.name/.SomeActivity
            return activity.substring(0, activity.lastIndexOf(".")) + "/." + activity.substring(activity.lastIndexOf(".") + 1);
        }

        throw new IllegalArgumentException("Unable to get activity to operate upon from '" + activity
            + "'. Please enter activity like some.package.name.SomeActivity or some.package.name/.SomeActivity "
            + "or some.package/some.other.package.Activity");
    }

}
