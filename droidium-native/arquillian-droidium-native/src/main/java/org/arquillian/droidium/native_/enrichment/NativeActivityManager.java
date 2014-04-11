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
package org.arquillian.droidium.native_.enrichment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.arquillian.droidium.container.api.ActivityManager;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.native_.configuration.DroidiumDronePointFilter;
import org.arquillian.droidium.native_.exception.NotUniqueWebDriverInstanceException;
import org.arquillian.droidium.native_.exception.WebDriverInstanceNotFoundException;
import org.arquillian.droidium.native_.metadata.DroidiumMetadataKey;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointContext;
import org.openqa.selenium.WebDriver;

/**
 * Manages Android activities by {@link WebDriver} via {@link AndroidDevice} injection point.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class NativeActivityManager implements ActivityManager {

    private DroneContext droneContext;

    void setDroneContext(DroneContext droneContext) {
        if (this.droneContext == null && droneContext != null) {
            this.droneContext = droneContext;
        }
    }

    @Override
    public void startActivity(String activity) {
        Validate.notNullOrEmpty(activity, "Activity you want to start can not be a null object nor an empty string!");

        final List<DronePoint<WebDriver>> dronePoints = getDronePointsForActivity(activity);

        checkSizes(dronePoints);

        DronePoint<WebDriver> dronePoint = dronePoints.get(0);

        if (activity.startsWith(".")) {
            activity = droneContext.get(dronePoint).getMetadata(DroidiumMetadataKey.ANDROID_PACKAGE_NAME.class) + activity;
        }

        WebDriver instance = droneContext.get(dronePoint).getInstance();

        instance.get("and-activity://" + activity);
    }

    @Override
    public void startActivity(Class<?> activity) throws WebDriverInstanceNotFoundException {
        Validate.notNull(activity, "Activity you want to start can not be a null object!");
        startActivity(activity.getName());
    }

    @Override
    public void stopActivity(String activity) throws WebDriverInstanceNotFoundException {
        Validate.notNullOrEmpty(activity, "Activity you want to stop can not be a null object nor an empty string!");

        final List<DronePoint<WebDriver>> dronePoints = getDronePointsForActivity(activity);

        checkSizes(dronePoints);

        droneContext.get(dronePoints.get(0)).getInstanceAs(WebDriver.class).close();
    }

    @Override
    public void stopActivity(Class<?> activity) throws WebDriverInstanceNotFoundException {
        Validate.notNull(activity, "Activity you want to stop can not be a null object!");
        stopActivity(activity.getName());
    }

    private List<DronePoint<WebDriver>> getDronePointsForActivity(String activityToStart) {

        final List<DronePoint<WebDriver>> dronePoints = new ArrayList<DronePoint<WebDriver>>();

        if (activityToStart == null || activityToStart.isEmpty()) {
            return dronePoints;
        }

        final Iterator<DronePoint<WebDriver>> dronePointsIterator = droneContext.find(WebDriver.class).filter(new DroidiumDronePointFilter()).iterator();

        while (dronePointsIterator.hasNext()) {
            final DronePoint<WebDriver> dronePoint = dronePointsIterator.next();

            final DronePointContext<?> dronePointContext = droneContext.get(dronePoint);

            if (!dronePointContext.hasMetadata(DroidiumMetadataKey.ACTIVITIES.class)
                || !dronePointContext.hasMetadata(DroidiumMetadataKey.SELENDROID_PACKAGE_NAME.class)
                || !dronePointContext.hasMetadata(DroidiumMetadataKey.ANDROID_PACKAGE_NAME.class)) {
                continue;
            }

            final List<String> activities = dronePointContext.getMetadata(DroidiumMetadataKey.ACTIVITIES.class);

            final String androidPackageName = dronePointContext.getMetadata(DroidiumMetadataKey.ANDROID_PACKAGE_NAME.class);

            if (activityToStart.startsWith(".")) {

                String fqcn = androidPackageName + activityToStart;

                if (activities.contains(fqcn)) {
                    dronePoints.add(dronePoint);
                }
            } else if (activities.contains(activityToStart)) {
                dronePoints.add(dronePoint);
            }
        }

        return dronePoints;
    }

    /**
     * @param dronePoints
     * @throws WebDriverInstanceNotFoundException in case size of {@code dronePoints} is 0
     * @throws NotUniqueWebDriverInstanceException in cae size of {@code dronePoints} is not 1
     */
    private void checkSizes(final List<DronePoint<WebDriver>> dronePoints) throws WebDriverInstanceNotFoundException, NotUniqueWebDriverInstanceException {
        if (dronePoints.size() == 0) {
            throw new WebDriverInstanceNotFoundException("It seems you are trying to control an "
                + "activity which is not backed by any WebDriver instance.");
        }

        if (dronePoints.size() != 1) {
            throw new NotUniqueWebDriverInstanceException("Activity you want to control is found to be "
                + "controllable by multiple WebDrivers.");
        }
    }

}