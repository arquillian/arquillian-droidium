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
package org.arquillian.droidium.native_.deployment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.jboss.arquillian.core.spi.Validate;

/**
 * Maps list of activities as values to keys as deployment names of deployments which are instrumentable.
 *
 * @see DeploymentActivitiesScanner
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DeploymentActivitiesMapper {

    private static final Logger logger = Logger.getLogger(DeploymentActivitiesMapper.class.getName());

    private final Map<String, List<String>> deploymentActivitiesMap = new ConcurrentHashMap<String, List<String>>();

    /**
     * Maps {@code activities} to {@code deployment}. Every activity which is meant to be instrumented is paired with its
     * deployment. Deployment name is taken from {@code @Deployment}.
     *
     * @param deployment
     * @param activities
     * @throws IllegalArgumentException if either {@code deployment} or {@code activities} is a null object or an empty string
     *         respectively.
     */
    public void put(String deployment, List<String> activities) {
        Validate.notNull(activities, "Activity list a null object!");
        Validate.notNullOrEmpty(deployment,
            "Deployment name of underlying package where activities are located can not be "
                + "a null object nor an empty string! Please specify deployment name of @Deployment where the activities "
                + "are located.");

        if (activities.size() == 0) {
            logger.fine("You can not add list of activities which is empty.");
            return;
        }

        if (deploymentActivitiesMap.containsKey(deployment)) {
            logger.fine("You are trying to put activity-deployment pair into the mapper but there is already such deployment stored.");
            return;
        }

        deploymentActivitiesMap.put(deployment, activities);
    }

    /**
     *
     * @return umodifiable map of underlying mapping
     */
    public Map<String, List<String>> get() {
        return Collections.unmodifiableMap(deploymentActivitiesMap);
    }

    /**
     *
     * @param activity activity to get the deployment name of
     * @return deployment name from {@code @Deployment} method where {@code activity} is located or null if not found
     * @throws IllegalArgumentException if {@code activity} is a null object or an empty string
     */
    public String getDeploymentName(String activity) {
        Validate.notNullOrEmpty(activity,
            "The activity to want to know the deployment name of can not be a null object nor an empty string!");

        for (Map.Entry<String, List<String>> entry : deploymentActivitiesMap.entrySet()) {
            if (entry.getValue().contains(activity)) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     *
     * @param deploymentName name of the deployment from {@code @Deployment} you want to get the list of all activities of
     * @return unmodifiable list of activities which are located in backed package of {@code @Deployment}.
     */
    public List<String> getActivities(String deploymentName) {
        return Collections.unmodifiableList(deploymentActivitiesMap.get(deploymentName));
    }
}
