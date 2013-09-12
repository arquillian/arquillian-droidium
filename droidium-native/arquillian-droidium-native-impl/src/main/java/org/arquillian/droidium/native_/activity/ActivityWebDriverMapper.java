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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.core.spi.Validate;
import org.openqa.selenium.WebDriver;

/**
 * Maps Drone instances to activities they are supposed to instrument.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ActivityWebDriverMapper {

    private static Map<String, WebDriver> map = new ConcurrentHashMap<String, WebDriver>();

    /**
     *
     * @param instance
     * @param activities
     */
    public void put(WebDriver instance, List<String> activities) {
        Validate.notNull(instance, "Drone instance can not be a null object!");
        Validate.notNull(activities, "Activity object can not be a null object!");

        for (String activity : activities) {
            map.put(activity, instance);
        }
    }

    /**
     *
     * @param activity activity you want to get WebDriver instance which this driver controls
     * @return WebDriver instance which acts on this activity
     */
    public WebDriver getInstance(String activity) {
        Validate.notNullOrEmpty(activity, "Activity you want to get an instance of Drone can not be a null object "
            + "nor an empty string!");

        return map.get(activity);
    }

    public void removeActivities(WebDriver driver) {
        for (Map.Entry<String, WebDriver> entry : map.entrySet()) {
            if (entry.getValue() == driver) {
                map.remove(entry.getKey());
            }
        }
    }
}
