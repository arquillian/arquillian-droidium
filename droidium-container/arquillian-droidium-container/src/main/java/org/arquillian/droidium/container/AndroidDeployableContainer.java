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

import org.arquillian.droidium.container.activity.DefaultActivityManagerProvider;
import org.arquillian.droidium.container.api.ActivityManagerProvider;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.FileType;
import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.deployment.AndroidDeploymentRegister;
import org.arquillian.droidium.container.impl.AndroidApplicationHelper;
import org.arquillian.droidium.container.impl.AndroidApplicationManager;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.sign.APKSigner;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.container.spi.event.AndroidDeploy;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.droidium.container.spi.event.AndroidUndeploy;
import org.arquillian.droidium.container.utils.AndroidIdentifierGenerator;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
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
 * Produces SuiteScoped:
 * <ul>
 * <li>{@link AndroidApplicationHelper}</li>
 * <li>{@link APKSigner}</li>
 * <li>{@link AndroidApplicationManager}</li>
 * <li>{@link AndroidDeploymentRegister}</li>
 * <li>{@link AndroidSDK}</li>
 * <li>{@link IdentifierGenerator}</li>
 * <li>{@link ProcessExecutor}</li>
 * <li>{@link AndroidContainerConfiguration}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link AndroidContainerStart}</li>
 * <li>{@link AndroidContainerStop}</li>
 * <li>{@link AndroidDeploy}</li>
 * <li>{@link AndroidUndeploy}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class AndroidDeployableContainer implements DeployableContainer<AndroidContainerConfiguration> {

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidContainerConfiguration> configuration;

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidSDK> androidSDK;

    @Inject
    @SuiteScoped
    private InstanceProducer<IdentifierGenerator<FileType>> idGenerator;

    @Inject
    @SuiteScoped
    private InstanceProducer<ProcessExecutor> executor;

    @Inject
    @SuiteScoped
    private InstanceProducer<APKSigner> signer;

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidApplicationManager> androidApplicationManager;

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidApplicationHelper> androidApplicationHelper;

    @Inject
    @SuiteScoped
    private InstanceProducer<AndroidDeploymentRegister> androidDeploymentRegister;

    @Inject
    private Event<AndroidContainerStart> androidContainerStartEvent;

    @Inject
    private Event<AndroidContainerStop> androidContainerStopEvent;

    @Inject
    private Event<AndroidDeploy> deployArchiveEvent;

    @Inject
    private Event<AndroidUndeploy> undeployArchiveEvent;

    @Inject
    private Instance<AndroidDevice> androidDevice;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

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

        AndroidContainerConfiguration conf = this.configuration.get();

        this.androidSDK.set(new AndroidSDK(conf));
        this.idGenerator.set(new AndroidIdentifierGenerator());
        this.executor.set(new ProcessExecutor(conf.getAndroidSystemEnvironmentProperties()));

        this.signer.set(new APKSigner(this.executor.get(), this.androidSDK.get(), conf));
        this.androidApplicationHelper.set(new AndroidApplicationHelper(executor.get(), androidSDK.get()));
        this.androidDeploymentRegister.set(new AndroidDeploymentRegister());
    }

    @Override
    public void start() throws LifecycleException {
        this.androidContainerStartEvent.fire(new AndroidContainerStart());
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        deployArchiveEvent.fire(new AndroidDeploy(archive));
        return new ProtocolMetaData();
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        undeployArchiveEvent.fire(new AndroidUndeploy(archive));
    }

    @Override
    public void stop() throws LifecycleException {
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

    public void onAndroidDeviceReady(@Observes AndroidDeviceReady event) {
        ActivityManagerProvider activityManagerProvider = getActivityManagerProvider();
        androidDevice.get().setActivityManagerProvider(activityManagerProvider);
        this.androidApplicationManager
            .set(new AndroidApplicationManager(androidDevice.get(), executor.get(), androidSDK.get()));
    }

    /**
     * Deletes temporary directory where all resources (files, resigned apks, logs ... ) are stored for native plugin.
     *
     * This resource directory will not be deleted when you suppress it by {@code removeTmpDir="false"} in the configuration.
     *
     * @param event
     */
    public void onBeforeStop(@Observes BeforeStop event) {
        if (event.getDeployableContainer().getConfigurationClass() == AndroidContainerConfiguration.class
            && configuration.get().getRemoveTmpDir()) {
            DroidiumFileUtils.removeDir(DroidiumFileUtils.getTmpDir());
        }
    }

    private ActivityManagerProvider getActivityManagerProvider() {
        return serviceLoader.get().onlyOne(ActivityManagerProvider.class, DefaultActivityManagerProvider.class);
    }

}
