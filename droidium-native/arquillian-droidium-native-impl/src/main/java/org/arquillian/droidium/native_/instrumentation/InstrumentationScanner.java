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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.native_.api.Instrumentable;
import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.spi.TestClass;

/**
 * Scans test class in order to map deployment names to their instrumentation configurations.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public final class InstrumentationScanner {

    /**
     * Scans {@code @Deployment} methods in test case and resolves instrumentation logic.
     *
     * @param testClass test class to get the instrumentation mapping from
     * @return map of deployment names as keys and their instrumentation configurations as values
     * @throws InstrumentationMapperException when {@link #validate(Map)} on the parsed mapping fails
     * @throws IllegalArgumentException if {@code testClass} is a null object
     */
    public static Map<String, InstrumentationConfiguration> scan(TestClass testClass) throws InstrumentationMapperException {
        Validate.notNull(testClass, "Test class to get the instrumentation mapping from can not be a null object!");

        final Map<String, InstrumentationConfiguration> instrumentation = new ConcurrentHashMap<String, InstrumentationConfiguration>();

        for (Method method : testClass.getMethods(Deployment.class)) {
            if (method.getAnnotation(Instrumentable.class) != null) {
                instrumentation.put(getDeploymentName(method), getInstrumentationConfiguration(method));
            }
        }

        validate(instrumentation);

        return instrumentation;
    }

    /**
     * Validates instrumentation mapping. Mapping is invalid if there is a duplicity in the instrumentation configuration
     * meaning when there is same instrumentation configuration for more than one deployment name. Instrumentation configuration
     * is equal to another one iff {@code it.equals(another)}.
     *
     * @param instrumentation
     * @return true if {@code instrumentation} is valid, false otherwise.
     * @throws InstrumentationMapperException if mapping is invalid
     */
    public static boolean validate(Map<String, InstrumentationConfiguration> instrumentation)
        throws InstrumentationMapperException {
        Set<InstrumentationConfiguration> tempSet = new HashSet<InstrumentationConfiguration>();
        for (Map.Entry<String, InstrumentationConfiguration> item : instrumentation.entrySet()) {
            if (!tempSet.add(item.getValue())) {
                throw new InstrumentationMapperException("There is a duplicity in the instrumentation configuration. Check "
                    + "that the name of @Deployment is different for every deployment method and that "
                    + "port number in @Instrumentable is different for every @Deployment method as well.");
            }
        }
        return true;
    }

    /**
     * Gets the name of deployment from the deployment method.
     *
     * @param method a method to get the deployment name from
     * @return name of the deployment from the {@code method} as a string
     * @throws IllegalArgumentException if {@code method} is null or if it is not annotated by {@code @Deployment} or if
     */
    public static String getDeploymentName(Method method) {
        Validate.notNull(method, "Method to get the deployment name from can not be a null object!");

        Deployment deploymentAnnotation = null;
        deploymentAnnotation = method.getAnnotation(Deployment.class);

        if (deploymentAnnotation == null) {
            throw new IllegalArgumentException("You want to know the name of a deployment from a deployment method without "
                + "@Deployment annotation.");
        }

        return deploymentAnnotation.name();
    }

    /**
     * Gets the instrumentation configuration from the deployment method which is annotated by {@code Instrumentable}.
     *
     * @param method a method to get the instrumentation configuration from
     * @return instrumentation configuration for the {@code method}
     * @throws IllegalArgumentException if {@code method} is null or if it is not annotated by {@code @Instrumentable}
     */
    public static InstrumentationConfiguration getInstrumentationConfiguration(Method method) {
        Validate.notNull(method, "Method to get the instrumentation configuration from can not be a null object!");

        Annotation instrumentationAnnotation = null;
        instrumentationAnnotation = method.getAnnotation(Instrumentable.class);

        if (instrumentationAnnotation == null) {
            throw new IllegalArgumentException("You want to get the instrumentation configuration from a deployment method "
                + "without @Instrumentable annotation.");
        }

        InstrumentationConfiguration instrumentationConfiguration = new InstrumentationConfiguration();
        instrumentationConfiguration.setPort(((Instrumentable) instrumentationAnnotation).viaPort());

        return instrumentationConfiguration;
    }
}
