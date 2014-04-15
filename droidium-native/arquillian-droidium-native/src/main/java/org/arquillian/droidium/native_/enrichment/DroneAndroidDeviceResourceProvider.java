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
package org.arquillian.droidium.native_.enrichment;

import java.lang.annotation.Annotation;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.enrichment.AndroidDeviceResourceProvider;
import org.arquillian.droidium.native_.DroidiumNativeExtension;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.test.api.ArquillianResource;

/**
 * Adds {@link DroneContext} to injected AndroidDevice {@link NativeActivityManager} in order to resolve {@link DronePoint}s
 * which will manage activities.
 *
 * This resource provider extends basic {@link AndroidDeviceResourceProvider} from which it gets {@code AndroidDevice} as
 * normally but it overrides that provider when native plugin is in class path so this one is used instead of the basic one.
 *
 * @see DroidiumNativeExtension#register(org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder)
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroneAndroidDeviceResourceProvider extends AndroidDeviceResourceProvider {

    @Inject
    private Instance<DroneContext> droneContext;

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {

        AndroidDevice device = (AndroidDevice) super.lookup(resource, qualifiers);

        ((NativeActivityManager) device.getActivityManager()).setDroneContext(droneContext.get());

        return device;
    }
}
