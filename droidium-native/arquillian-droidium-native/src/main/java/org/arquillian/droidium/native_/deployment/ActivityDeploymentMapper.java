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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.jboss.arquillian.core.spi.Validate;

/**
 * Maps activities to deployment packages / APKs. Activity is described by its fully qualified domain name and it acts as a key
 * in underlying hash map. Deployment name is taken from {@code @Deployment} annotation which is put on deployment method of
 * Arquillian in some test case itself. It basically means that you can instrument these activities by Drone instance which is
 * hooked to the port via {@code @Instrumentable.viaPort}
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ActivityDeploymentMapper {

    private static final Logger logger = Logger.getLogger(ActivityDeploymentMapper.class.getName());

    private static final Map<String, String> map = new ConcurrentHashMap<String, String>();

    /**
     * Maps {@code activity} to {@code deployment}. Every activity which is meant to be instrumented is paired with its
     * deployment. Deployment name is taken from {@code @Deployment}. There can be more than one activity per deployment.
     *
     * @param activity
     * @param deployment
     * @throws IllegalArgumentException if either {@code activity} or {@code deployment} is a null object or an empty string.
     */
    public void put(String activity, String deployment) {
        Validate.notNullOrEmpty(activity, "Activity name can not be an empty string or a null object! Use FQDN please.");
        Validate.notNullOrEmpty(deployment,
            "Deployment name of underlying package where activity is located can not be "
                + "a null object nor an empty string! Please specify deployment name of @Deployment where the activity "
                + "is located. You tried to put activity '" + activity + "'.");

        if (map.containsKey(activity)) {
            logger.fine("You are trying to put activity-deployment pair into the mapper but there is already such activity stored.");
        }

        map.put(activity, deployment);
    }

    /**
     *
     * @return whole mapping
     */
    public Map<String, String> get() {
        return map;
    }

    /**
     *
     * @param activity activity to get the deployment name of
     * @return deployment name from {@code @Deployment} method where {@code activity} is located.
     * @throws IllegalArgumentException if {@code activity} is a null object or an empty string
     */
    public String getDeploymentName(String activity) {
        Validate.notNullOrEmpty(activity,
            "The activity to want to know the deployment name of can not be a null object nor an empty string!");
        return map.get(activity);
    }

    /**
     *
     * @param deploymentName name of the deployment from {@code @Deployment} you want to get the list of all activities of
     * @return list of activities which are located in backed package of {@code @Deployment}. It basically means that you can
     *         instrument these activities by Drone instance which is hooked to the port where {@code @Instrumentable.viaPort}
     *         is hooked as well
     */
    public List<String> getActivities(String deploymentName) {
        List<String> activities = new ArrayList<String>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(deploymentName)) {
                activities.add(entry.getKey());
            }
        }

        return activities;
    }
}
