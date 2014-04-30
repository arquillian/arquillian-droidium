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

import org.arquillian.droidium.container.activity.DefaultActivityManager;
import org.arquillian.droidium.container.api.ActivityManager;
import org.arquillian.droidium.container.api.ActivityManagerProvider;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.AndroidApplicationManager;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.container.spi.event.AndroidDeploy;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.droidium.container.spi.event.AndroidUnDeploy;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * <p>
 * Android Managed container for the Arquillian project
 * </p>
 *
 * Deployable Android Container class with the whole lifecycle.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AndroidDeviceReady}</li>
 * </ul>
 * Produces ContainerScoped:
 * <ul>
 * <li>{@link AndroidApplicationManager}</li>
 * <li>{@link AndroidContainerConfiguration}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link AndroidContainerStart}</li>
 * <li>{@link AndroidContainerStop}</li>
 * <li>{@link AndroidDeploy}</li>
 * <li>{@link AndroidUnDeploy}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class AndroidDeployableContainer implements DeployableContainer<AndroidContainerConfiguration> {

    @Inject
    @ContainerScoped
    private InstanceProducer<AndroidContainerConfiguration> configuration;

    @Inject
    private Instance<AndroidSDK> androidSDK;

    @Inject
    private Instance<AndroidDevice> androidDevice;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<AndroidContainerStart> androidContainerStart;

    @Inject
    private Event<AndroidContainerStop> androidContainerStop;

    @Inject
    private Event<AndroidDeploy> androidDeploy;

    @Inject
    private Event<AndroidUnDeploy> androidUnDeploy;

    @Override
    public Class<AndroidContainerConfiguration> getConfigurationClass() {
        return AndroidContainerConfiguration.class;
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Android 1.0");
    }

    @Override
    public void setup(AndroidContainerConfiguration configuration) {
        this.configuration.set(configuration);
        androidSDK.get().setupWith(this.configuration.get());
    }

    @Override
    public void start() throws LifecycleException {
        androidContainerStart.fire(new AndroidContainerStart());
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        androidDeploy.fire(new AndroidDeploy(archive));
        return new ProtocolMetaData();
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        androidUnDeploy.fire(new AndroidUnDeploy(archive));
    }

    @Override
    public void stop() throws LifecycleException {
        androidContainerStop.fire(new AndroidContainerStop());
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
     * Sets {@link ActivityManagerProvider} for {@code AndroidDevice} and produces {@link AndroidApplicationManager}.
     *
     * @param event
     */
    public void onAndroidDeviceReady(@Observes AndroidDeviceReady event) {
        ActivityManager activityManager = serviceLoader.get().onlyOne(ActivityManager.class, DefaultActivityManager.class);

        if (activityManager instanceof DefaultActivityManager) {
            androidDevice.get().setActivityManager(new DefaultActivityManager(androidDevice.get()));
        }
    }

}