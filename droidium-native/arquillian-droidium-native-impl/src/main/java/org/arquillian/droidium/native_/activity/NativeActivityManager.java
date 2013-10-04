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
package org.arquillian.droidium.native_.activity;

import org.arquillian.droidium.container.api.ActivityManager;
import org.jboss.arquillian.core.spi.Validate;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class NativeActivityManager implements ActivityManager {

    private ActivityWebDriverMapper activityWebDriverMapper;

    public NativeActivityManager(ActivityWebDriverMapper activityWebDriverMapper) {
        Validate.notNull(activityWebDriverMapper, "you passed null object");
        this.activityWebDriverMapper = activityWebDriverMapper;
    }

    @Override
    public void startActivity(String activity) throws NoMatchingWebDriverInstanceFoundException {
        Validate.notNullOrEmpty(activity, "Activity you want to start can not be a null object nor an empty string!");
        WebDriver driver = activityWebDriverMapper.getInstance(activity);
        if (driver == null) {
            throw new NoMatchingWebDriverInstanceFoundException("It seems you are trying to start an "
                + "activity which is not backed by any WebDriver instance.");
        }

        driver.get("and-activity://" + activityWebDriverMapper.getActivity(driver, activity));
    }

    @Override
    public void startActivity(Class<?> activity) throws NoMatchingWebDriverInstanceFoundException {
        Validate.notNull(activity, "Activity you want to start can not be a null object!");
        startActivity(activity.getName());
    }

    @Override
    public void stopActivity(String activity) throws NoMatchingWebDriverInstanceFoundException {
        Validate.notNullOrEmpty(activity, "Activity you want to stop can not be a null object nor an empty string!");
        WebDriver driver = activityWebDriverMapper.getInstance(activity);
        if (driver == null) {
            throw new NoMatchingWebDriverInstanceFoundException("It seems you are trying to stop an "
                + "activity which is not backed by any WebDriver instance.");
        }
        driver.close();
    }

    @Override
    public void stopActivity(Class<?> activity) throws NoMatchingWebDriverInstanceFoundException {
        Validate.notNull(activity, "Activity you want to stop can not be a null object!");
        startActivity(activity.getName());
    }

}