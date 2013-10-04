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

import java.util.ArrayList;
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
     * Gets WebDriver instance via which a user can control activity. Activity name can be specified as FQDN or its simple name.
     * {@code getInstance("foo.bar.Baz")} is the same as {@code getInstance("Baz")}. In case there are classes which FQDN does
     * not equal but their simple name does, e.g. {@code foo.bar.Baz} and {@code joe.doe.Baz}, when executing
     * {@getInstance("Baz")}, {@code NotUniqueWebDriverInstanceException} is thrown since we are not sure
     * what WebDriver instance a user wants to get.
     *
     * You do not have to use simple name nor FQDN, it is sufficient to use shortest suffix of {@code activity} which is unique
     * accross all suffixes of all activity classes.
     *
     *
     * @param activity activity you want to get WebDriver instance for which this driver controls
     * @return WebDriver instance which acts on this activity
     * @throws WebDriverInstanceNotFoundException thrown in case WebDriver instance for such {@code activity} is not found.
     * @throws NotUniqueWebDriverInstanceException thrown in case there is more than one WebDriver instance for given
     *         {@code activity}.
     */
    public WebDriver getInstance(String activity) throws WebDriverInstanceNotFoundException,
        NotUniqueWebDriverInstanceException {
        Validate.notNullOrEmpty(activity, "Activity you want to get an instance of Drone can not be a null object "
            + "nor an empty string!");

        WebDriver instance = getAsFQDN(activity);

        if (instance == null) {
            instance = getAsSimpleName(activity);
        }

        return instance;
    }

    private WebDriver getAsSimpleName(String activity) {

        List<WebDriver> foundDrivers = new ArrayList<WebDriver>();

        for (Map.Entry<String, WebDriver> entry : map.entrySet()) {
            if (entry.getKey().endsWith(activity)) {
                foundDrivers.add(entry.getValue());
            }
        }

        if (foundDrivers.size() == 0) {
            throw new WebDriverInstanceNotFoundException("There is not any WebDriver instance found for "
                + "activity of name '" + activity + "'.");
        }

        if (foundDrivers.size() > 1) {
            throw new NotUniqueWebDriverInstanceException("There is more than one WebDriver instance found for "
                + "activity class of name '" + activity + "'. It is highly probable that there are two classes "
                + "which FQDN name differs but their simple name does not.");
        }

        return foundDrivers.get(0);
    }

    private WebDriver getAsFQDN(String activity) {
        return map.get(activity);
    }

    public String getActivity(WebDriver driver, String activity) {
        for (Map.Entry<String, WebDriver> entry : map.entrySet()) {
            if (entry.getValue() == driver) {
                if (entry.getKey().equals(activity) || entry.getKey().endsWith(activity)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public void removeActivities(WebDriver driver) {
        for (Map.Entry<String, WebDriver> entry : map.entrySet()) {
            if (entry.getValue() == driver) {
                map.remove(entry.getKey());
            }
        }
    }
}
