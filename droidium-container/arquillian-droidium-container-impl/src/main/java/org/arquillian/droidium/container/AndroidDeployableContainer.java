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
package org.arquillian.droidium.container;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.configuration.AndroidManagedContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.container.spi.event.AndroidDeployArchive;
import org.arquillian.droidium.container.spi.event.AndroidProtocolDescriptionEvent;
import org.arquillian.droidium.container.spi.event.AndroidProtocolMetaDataEvent;
import org.arquillian.droidium.container.spi.event.AndroidUndeployArchive;
import org.arquillian.droidium.container.utils.AndroidIdentifierGenerator;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * <p>
 * Android Managed container for the Arquillian project
 * </p>
 *
 * Deployable Android Container class with the whole lifecycle.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class AndroidDeployableContainer implements DeployableContainer<AndroidManagedContainerConfiguration> {

    private static final Logger logger = Logger.getLogger(AndroidDeployableContainer.class.getSimpleName());

    @Inject
    @ContainerScoped
    private InstanceProducer<AndroidManagedContainerConfiguration> configuration;

    @Inject
    @ContainerScoped
    private InstanceProducer<AndroidSDK> androidSDK;

    @Inject
    @ContainerScoped
    private InstanceProducer<IdentifierGenerator> idGenerator;

    @Inject
    @ContainerScoped
    private InstanceProducer<ProcessExecutor> executor;

    @Inject
    private Event<AndroidContainerStart> androidContainerStartEvent;

    @Inject
    private Event<AndroidContainerStop> androidContainerStopEvent;

    @Inject
    private Event<AndroidDeployArchive> deployArchiveEvent;

    @Inject
    private Event<AndroidUndeployArchive> undeployArchiveEvent;

    @Inject
    private Event<AndroidProtocolMetaDataEvent> androidProtocolMetaDataEvent;

    @Inject
    private Event<AndroidProtocolDescriptionEvent> androidProtocolDescriptionEvent;

    @Override
    public Class<AndroidManagedContainerConfiguration> getConfigurationClass() {
        return AndroidManagedContainerConfiguration.class;
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        logger.log(Level.INFO, "Getting default protocol");
        AndroidProtocolDescriptionEvent protocolDescriptionEvent = new AndroidProtocolDescriptionEvent();
        androidProtocolDescriptionEvent.fire(protocolDescriptionEvent);
        return protocolDescriptionEvent.getProtocolDescription();
    }

    @Override
    public void setup(AndroidManagedContainerConfiguration configuration) {
        this.configuration.set(configuration);
        this.androidSDK.set(new AndroidSDK(this.configuration.get()));
        this.idGenerator.set(new AndroidIdentifierGenerator());
        this.executor.set(new ProcessExecutor());
    }

    @Override
    public void start() throws LifecycleException {
        logger.log(Level.INFO, "Starting the container {0}.", getContainerName());
        this.androidContainerStartEvent.fire(new AndroidContainerStart());
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        logger.log(Level.INFO, "Deploying the archive to the container {0}.", getContainerName());
        deployArchiveEvent.fire(new AndroidDeployArchive(archive));
        return new ProtocolMetaData();
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        logger.log(Level.INFO, "Undeploying an archive from the container {0}.", getContainerName());

        undeployArchiveEvent.fire(new AndroidUndeployArchive(archive));
    }

    @Override
    public void stop() throws LifecycleException {
        logger.log(Level.INFO, "Stopping the container {0}.", getContainerName());
        this.androidContainerStopEvent.fire(new AndroidContainerStop());
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Undeployment of a descriptor is not supported.");
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Deployment of a descriptor is not supported");
    }

    /**
     * Gets name of the container.
     *
     * When container is backed by emulator, it gets its AVD name, otherwise it gets serial number of the physical device. If we
     * are connecting to a running emulator, its console port is taken as the identification.
     *
     * @return descriptive name of the container
     */
    private String getContainerName() {
        if (configuration.get().getAvdName() == null) {
            if (configuration.get().getSerialId() == null) {
                if (configuration.get().getConsolePort() != null) {
                    return "running at port " + configuration.get().getConsolePort();
                }
                else {
                    return "unsuccessful to determine container id";
                }
            } else {
                return configuration.get().getSerialId();
            }
        } else {
            return configuration.get().getAvdName();
        }
    }

}
