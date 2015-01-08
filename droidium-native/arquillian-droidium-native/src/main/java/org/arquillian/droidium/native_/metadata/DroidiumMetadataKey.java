/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.native_.metadata;

import java.util.List;

import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointContext.MetadataKey;

/**
 * Holds all Drone metadata keys for Droidium
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public interface DroidiumMetadataKey {

    /**
     * Representation of package name of Android application under tests.
     *
     * Under this key, a name of Android package deployed via {@code @Deployment} method is saved to {@link DronePoint}
     * metadata. This key differs from {@link DEPLOYMENT} key which reflects {@code @Deployment} name itself.
     */
    public interface TESTED_APP_PACKAGE_NAME extends MetadataKey<String> {
    }

    /**
     * Representation modified package name of original Selendroid deployment that is used to identify deployment on Android Device.
     *
     * This key must be unique across all deployments and it is used to retrieve additional information to @{link DronePoint}.
     */
    interface INSTRUMENTATION_TEST_PACKAGE_NAME extends MetadataKey<String> {
    }

    /**
     * Under this key, a deployment name from {@code @Deployment} method is saved as value to {@link DronePoint} metadata on
     * which some particular {@link DronePoint} operates.
     *
     * This key differs from {@link TESTED_APP_PACKAGE_NAME} which reflects name of Android APK package itself.
     */
    interface DEPLOYMENT extends MetadataKey<String> {
    }

    /**
     * Under this key, a list of activities which is some Drone acting upon is saved to {@link DronePoint} metadata.
     */
    interface ACTIVITIES extends MetadataKey<List<String>> {
    }

    /**
     * Under this key, port number is saved to {@link DronePoint} metadata via which some particular Drone interacts with
     * Selendroid deployment instrumenting Arquillian deployment.
     */
    interface PORT extends MetadataKey<String> {
    }

}
