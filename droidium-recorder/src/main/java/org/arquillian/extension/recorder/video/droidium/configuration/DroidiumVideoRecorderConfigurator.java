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
package org.arquillian.extension.recorder.video.droidium.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.extension.recorder.video.VideoConfiguration;
import org.arquillian.extension.recorder.video.VideoConfigurationException;
import org.arquillian.extension.recorder.video.VideoConfigurator;
import org.arquillian.extension.recorder.video.VideoRecorderEnvironmentCleaner;
import org.arquillian.extension.recorder.video.VideoStrategy;
import org.arquillian.extension.recorder.video.event.VideoExtensionConfigured;
import org.arquillian.recorder.reporter.ReporterConfiguration;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * Observes:
 * <ul>
 * <li>{@link VideoExtensionConfigured}</li>
 * </ul>
 * Creates:
 * <ul>
 * <li>{@link VideoConfiguration}</li>
 * <li>{@link VideoStrategy}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumVideoRecorderConfigurator extends VideoConfigurator {

    private static final Logger logger = Logger.getLogger(DroidiumVideoRecorderConfigurator.class.getSimpleName());

    @Inject
    @ApplicationScoped
    private InstanceProducer<VideoConfiguration> configuration;

    @Inject
    @ApplicationScoped
    private InstanceProducer<VideoStrategy> strategy;

    @Inject
    private Instance<ReporterConfiguration> reporterConfiguration;

    @Inject
    private Event<VideoExtensionConfigured> extensionConfiguredEvent;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Instance<VideoRecorderEnvironmentCleaner> cleaner;

    public void configureExtension(@Observes VideoExtensionConfigured event, ArquillianDescriptor descriptor) {
        VideoConfiguration configuration = new DroidiumVideoConfiguration(reporterConfiguration.get());

        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (extension.getExtensionName().equals(EXTENSION_NAME)) {
                configuration.setConfiguration(extension.getExtensionProperties());
                configuration.validate();
                break;
            }
        }

        this.configuration.set(configuration);

        if (logger.isLoggable(Level.INFO)) {
            System.out.println("Configuration of Arquillian Droidium Video Recorder:");
            System.out.println(this.configuration.get().toString());
        }
        // there will be 2 strategies in this list at least - SkippingVideoStrategy and DefaultVideoStrategy
        // if this extension is not on the class path, SkippingVideoStrategy was already produced hence
        // the extension will work in a "dummy" mode where nothing will be ever recorded. If this is on the class path,
        // we have recorder implementation hence we will use at least DefaultVideoStrategy if no other strategy is used

        List<VideoStrategy> strategies = new ArrayList<VideoStrategy>(serviceLoader.get().all(VideoStrategy.class));

        strategy.set(resolveVideoStrategy(strategies));

        strategy.get().setConfiguration(this.configuration.get());

        setup();
    }

    private VideoStrategy resolveVideoStrategy(List<VideoStrategy> strategies) {

        Collections.sort(strategies, new Comparator<VideoStrategy>() {

            @Override
            public int compare(VideoStrategy strategy1, VideoStrategy strategy2) {
                return strategy1.precedence() - strategy2.precedence();
            }
        });

        // effectively returns DefaultVideoStrategy when only two strategies are on the class path

        return strategies.get(strategies.size() - 1);
    }

    private void setup() {
        try {
            this.cleaner.get().clean(configuration.get());
        } catch (Exception e) {
            throw new VideoConfigurationException("Unable to clean before video extension gets to work.", e);
        }
    }
}
