/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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
package org.arquillian.droidium.container.enricher;

import java.lang.annotation.Annotation;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * Resource provider which allows to get the control of Android device.
 *
 * User can use this to install an APK, do forwarding or execute an arbitrary command on the device manually.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * @see AndroidDevice
 */
public class AndroidDeviceResourceProvider implements ResourceProvider {

    @Inject
    private Instance<AndroidDevice> androidDevice;

    @Override
    public boolean canProvide(Class<?> type) {
        return AndroidDevice.class.isAssignableFrom(type);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {

        AndroidDevice device = androidDevice.get();

        if (device == null) {
            throw new IllegalStateException("Unable to inject Android device instance into the test.");
        }

        return device;
    }

}