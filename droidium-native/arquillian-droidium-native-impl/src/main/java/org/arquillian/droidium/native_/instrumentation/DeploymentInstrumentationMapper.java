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
package org.arquillian.droidium.native_.instrumentation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.jboss.arquillian.core.spi.Validate;

/**
 * Holds deployment names with underlying instrumentation configurations.
 *
 * @see DeploymentInstrumentationResolver
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DeploymentInstrumentationMapper {

    private static final Logger logger = Logger.getLogger(DeploymentInstrumentationMapper.class.getName());

    private static Map<String, InstrumentationConfiguration> map = new ConcurrentHashMap<String, InstrumentationConfiguration>();

    /**
     *
     * @param resolvedInstrumentation
     * @throws IllegalArgumentException if {@code resolvedInstrumentation} is a null object
     */
    public void set(Map<String, InstrumentationConfiguration> resolvedInstrumentation) {
        Validate.notNull(resolvedInstrumentation, "Resolved instrumentation passed can not be a null object!");
        map = resolvedInstrumentation;
    }

    /**
     *
     * @param deploymentName
     * @param resolvedInstrumentation
     */
    public void put(String deploymentName, InstrumentationConfiguration resolvedInstrumentation) {
        Validate.notNullOrEmpty(deploymentName,
            "Deployment name to save resolved instrumentation for can not be a null object nor an empty string!");
        Validate.notNull(resolvedInstrumentation, "Resolved instrumentation can not be a null object!");

        if (map.containsKey(deploymentName)) {
            logger.fine("You are trying to put instrumentation for already existing deployment name.");
        }

        map.put(deploymentName, resolvedInstrumentation);
    }

    /**
     *
     * @return whole mapping
     */
    public Map<String, InstrumentationConfiguration> get() {
        return map;
    }

    /**
     *
     * @param deploymentName name of deployment (taken from {@code @Deployment method} to get instrumentation configuration of
     * @return instrumentation configuration for given {@code deploymentName}, null if there is no such mapping
     */
    public InstrumentationConfiguration getDeploymentName(String deploymentName) {
        Validate.notNullOrEmpty(deploymentName, "Deployment name to get the instrumentation configuration of "
            + "can not be a null object nor an empty string!");
        return map.get(deploymentName);
    }

}
