/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.arquillian.droidium.container.api;

import java.util.List;

/**
 * An abstraction of Android Debug Bridge
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public interface AndroidBridge {

    /**
     * Lists all devices currently available.
     *
     * @return List of available devices
     */
    List<AndroidDevice> getDevices();

    /**
     * Connects to the bridge.
     *
     * @throws AndroidExecutionException
     */

    void connect() throws AndroidExecutionException;

    /**
     * Checks if bridge is connected.
     *
     * @return {@code true} if connected, {@code false} otherwise
     */

    boolean isConnected();

    /**
     * Disconnects bridge and disposes connection.
     *
     * @throws AndroidExecutionException
     */

    void disconnect() throws AndroidExecutionException;

    /**
     * Checks if there are some devices on the bridge.
     *
     * @return true if there are some devices on the bridge, false otherwise
     */
    boolean hasDevices();

    /**
     * Lists all devices currently available which are emulators.
     *
     * @return List of available emulators
     */
    List<AndroidDevice> getEmulators();
}
