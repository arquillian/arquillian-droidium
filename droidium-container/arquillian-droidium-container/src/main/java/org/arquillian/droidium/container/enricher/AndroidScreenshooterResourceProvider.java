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
package org.arquillian.droidium.container.enricher;

import java.lang.annotation.Annotation;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.Screenshooter;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * Resource provider which allows to get the access to Android screenshooter.
 *
 * User can use injection of {@link Screenshooter} in order to take screenshots of underlying tests.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidScreenshooterResourceProvider implements ResourceProvider {

    private static final Logger log = Logger.getLogger(AndroidScreenshooterResourceProvider.class.getName());

    @Inject
    Instance<Screenshooter> screenshooter;

    @Override
    public boolean canProvide(Class<?> type) {
        return Screenshooter.class.isAssignableFrom(type);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        Screenshooter screenshooterResource = screenshooter.get();

        if (screenshooterResource == null) {
            log.severe("Unable to inject Android screenshooter into the test.");
            throw new IllegalStateException("Unable to inject Android screenshooter into the test.");
        }

        return screenshooterResource;
    }

}
