/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.extension.recorder.video.droidium.impl;

import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.extension.recorder.video.Recorder;
import org.arquillian.extension.recorder.video.VideoConfiguration;
import org.arquillian.extension.recorder.video.droidium.configuration.DroidiumVideoConfiguration;
import org.arquillian.extension.recorder.video.event.VideoExtensionConfigured;
import org.arquillian.recorder.reporter.impl.TakenResourceRegister;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumVideoRecorderCreator {

    @Inject
    @ApplicationScoped
    private InstanceProducer<Recorder> recorder;

    @Inject
    @ApplicationScoped
    private InstanceProducer<TakenResourceRegister> takenResourceRegister;

    @Inject
    private Instance<VideoConfiguration> configuration;

    public void onVideoRecorderExtensionConfigured(@Observes(precedence = -100) VideoExtensionConfigured event) {

        if (takenResourceRegister.get() == null) {
            this.takenResourceRegister.set(new TakenResourceRegister());
        }

        Recorder recorder = new DroidiumVideoRecorder(takenResourceRegister.get());
        recorder.init((DroidiumVideoConfiguration) configuration.get());

        this.recorder.set(recorder);
    }

    public void onAndroidDeviceReady(@Observes AndroidDeviceReady event) {
        ((DroidiumVideoRecorder) recorder.get()).setAndroidDevice(event.getDevice());
    }
}
