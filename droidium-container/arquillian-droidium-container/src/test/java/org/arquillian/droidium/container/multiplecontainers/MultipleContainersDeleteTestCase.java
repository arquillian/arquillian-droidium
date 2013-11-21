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
package org.arquillian.droidium.container.multiplecontainers;

import java.util.ArrayList;
import java.util.Collection;
import org.arquillian.droidium.container.AndroidDeployableContainer;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.multiplecontainers.ContainerType;
import org.arquillian.droidium.multiplecontainers.MultipleLocalContainersRegistry;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.as.arquillian.container.CommonDeployableContainer;
import org.jboss.as.arquillian.container.managed.ManagedContainerConfiguration;
import org.jboss.as.arquillian.container.managed.ManagedDeployableContainer;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipleContainersDeleteTestCase extends AbstractContainerTestBase {

    @Inject
    private Instance<Injector> injector;

    @Mock
    private ServiceLoader serviceLoader;

    @SuppressWarnings("rawtypes")
    private Collection<DeployableContainer> containers = new ArrayList<DeployableContainer>();

    private DeployableContainer<AndroidContainerConfiguration> androidContainer = new AndroidDeployableContainer();

    private CommonDeployableContainer<ManagedContainerConfiguration> jbossContainer = new ManagedDeployableContainer();

    private MultipleLocalContainersRegistry registry;

    @Before
    public void setup() {

        containers.add(androidContainer);
        containers.add(jbossContainer);

        Mockito.when(serviceLoader.all(DeployableContainer.class)).thenReturn(containers);

        registry = new MultipleLocalContainersRegistry(injector.get());

        registry.create(Descriptors.create(ArquillianDescriptor.class).group("container").container("android").setDefault(),
            serviceLoader);
        registry.create(Descriptors.create(ArquillianDescriptor.class).group("container").container("jboss"), serviceLoader);
    }

    @After
    public void tearDown() {
        containers.clear();
    }

    @Test
    public void removeAndroidContainerTest() {
        Assert.assertEquals(2, registry.getContainers().size());

        registry.remove(ContainerType.ANDROID);

        Assert.assertEquals(1, registry.getContainers().size());
    }

    @Test
    public void removeJBossContainerTest() {
        Assert.assertEquals(2, registry.getContainers().size());

        registry.remove(ContainerType.JBOSS);

        Assert.assertEquals(1, registry.getContainers().size());
    }

    @Test
    public void removeAllOneByOneTest() {
        Assert.assertEquals(2, registry.getContainers().size());

        registry.remove(ContainerType.ANDROID);
        registry.remove(ContainerType.JBOSS);

        Assert.assertEquals(0, registry.getContainers().size());
    }

    @Test
    public void removeAllTest() {
        Assert.assertEquals(2, registry.getContainers().size());

        registry.removeAll();

        Assert.assertEquals(0, registry.getContainers().size());
    }

    @Test
    public void removeAndroidContainerByQualifierTest() {
        Assert.assertEquals(2, registry.getContainers().size());

        registry.remove("android");

        Assert.assertEquals(1, registry.getContainers().size());
    }

}
