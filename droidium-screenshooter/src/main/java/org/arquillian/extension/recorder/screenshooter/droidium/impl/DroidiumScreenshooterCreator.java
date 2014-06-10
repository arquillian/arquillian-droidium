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
package org.arquillian.extension.recorder.screenshooter.droidium.impl;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.extension.recorder.screenshooter.Screenshooter;
import org.arquillian.extension.recorder.screenshooter.ScreenshooterConfiguration;
import org.arquillian.extension.recorder.screenshooter.event.ScreenshooterExtensionConfigured;
import org.arquillian.recorder.reporter.impl.TakenResourceRegister;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Observes:
 * <ul>
 * <li>{@link ScreenshooterExtensionConfigured}</li>
 * </ul>
 * Creates {@link ApplicationScoped}:
 * <ul>
 * <li>{@link Screenshooter}</li>
 * <li>{@link TakenResourceRegister}</li>
 * </ul>
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumScreenshooterCreator {

    @Inject
    @ApplicationScoped
    private InstanceProducer<Screenshooter> screenshooter;

    @Inject
    @ApplicationScoped
    private InstanceProducer<TakenResourceRegister> takenResourceRegister;

    @Inject
    private Instance<ScreenshooterConfiguration> configuration;

    /**
     * Creates {@link Screenshooter} instance.
     *
     * @param event
     */
    public void onScreenshooterExtensionConfigured(@Observes ScreenshooterExtensionConfigured event) {

        if (takenResourceRegister.get() == null) {
            this.takenResourceRegister.set(new TakenResourceRegister());
        }

        Screenshooter screenshooter = new DroidiumScreenshooter(takenResourceRegister.get());
        screenshooter.init(configuration.get());

        this.screenshooter.set(screenshooter);
    }

    /**
     * Sets {@link AndroidDevice} to created {@link Screenshooter} instance.
     *
     * @param event
     */
    public void onAndroidDeviceAvailable(@Observes AndroidDeviceReady event) {
        ((DroidiumScreenshooter) screenshooter.get()).setDevice(event.getDevice());
    }
}
