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
package org.arquillian.droidium.native_.selendroid;

import java.util.logging.Logger;

import org.arquillian.droidium.native_.selendroid.impl.SelendroidFactory;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;

/**
 * Entry point for Selendroid driver extension.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidDriverExtension implements LoadableExtension {

    private static final Logger logger = Logger.getLogger(SelendroidDriverExtension.class.getName());

    @Override
    public void register(ExtensionBuilder builder) {

        logger.info("Registration of Arquillian Droidium Native Selendroid Driver extension");

        builder.service(Configurator.class, SelendroidFactory.class);
        builder.service(Instantiator.class, SelendroidFactory.class);
        builder.service(Destructor.class, SelendroidFactory.class);
    }

}
