/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.arquillian.droidium.container.muliplecontainers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;

import org.arquillian.droidium.container.AndroidDeployableContainer;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.multiplecontainers.MultipleLocalContainersRegistry;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.as.arquillian.container.CommonDeployableContainer;
import org.jboss.as.arquillian.container.managed.ManagedContainerConfiguration;
import org.jboss.as.arquillian.container.managed.ManagedDeployableContainer;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests registration of two containers with different adapters via multiple containers extension.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipleContainersTestCase extends AbstractContainerTestBase {

    @Inject
    private Instance<Injector> injector;

    @Mock
    private ServiceLoader serviceLoader;

    private DeployableContainer<AndroidContainerConfiguration> deployableContainer = new AndroidDeployableContainer();

    private CommonDeployableContainer<ManagedContainerConfiguration> deployableJBossContainer = new ManagedDeployableContainer();

    @Test
    public void testCreateOnlyAndroidContainer() {

        @SuppressWarnings("rawtypes")
        Collection<DeployableContainer> containers = new ArrayList<DeployableContainer>();
        containers.add(deployableContainer);

        Mockito.when(serviceLoader.all(DeployableContainer.class)).thenReturn(containers);

        ContainerRegistry registry = new MultipleLocalContainersRegistry(injector.get());

        // adapterImplClass is not required since it is only the adapter on the classpath
        registry.create(Descriptors.create(ArquillianDescriptor.class)
                .group("containers").container("android1").setDefault(), serviceLoader);

        Container android1 = registry.getContainer("android1");
        assertNotNull(android1);
        assertEquals("android1", android1.getName());
    }

    @Test
    public void testCreateAndroidContainersInGroup() {

        @SuppressWarnings("rawtypes")
        Collection<DeployableContainer> containers = new ArrayList<DeployableContainer>();
        containers.add(deployableContainer);
        containers.add(deployableJBossContainer);

        Mockito.when(serviceLoader.all(DeployableContainer.class)).thenReturn(containers);

        ContainerRegistry registry = new MultipleLocalContainersRegistry(injector.get());

        registry.create(
                Descriptors
                        .create(ArquillianDescriptor.class)
                        .group("containers")
                        .container("android1")
                        .setDefault()
                        .property("adapterImplClass",
                                "org.arquillian.droidium.container.AndroidDeployableContainer"),
                serviceLoader);

        registry.create(
                Descriptors
                        .create(ArquillianDescriptor.class)
                        .group("containers")
                        .container("jbossas")
                        .property("adapterImplClass", "org.jboss.as.arquillian.container.managed.ManagedDeployableContainer"),
                serviceLoader);

        Container android1 = registry.getContainer("android1");
        Container jboss = registry.getContainer("jbossas");
        Container android1b = registry.getContainer(TargetDescription.DEFAULT);

        assertNotNull(android1);
        assertNotNull(jboss);
        assertNotNull(android1b);

        assertEquals("android1", android1.getName());
        assertEquals("jbossas", jboss.getName());
        assertEquals("android1", android1b.getName());
    }

    @Test
    public void testCreateAndroidContainersWithoutGroup() {

        @SuppressWarnings("rawtypes")
        Collection<DeployableContainer> containers = new ArrayList<DeployableContainer>();
        containers.add(deployableContainer);
        containers.add(deployableJBossContainer);

        Mockito.when(serviceLoader.all(DeployableContainer.class)).thenReturn(containers);

        ContainerRegistry registry = new MultipleLocalContainersRegistry(injector.get());

        registry.create(
                Descriptors
                        .create(ArquillianDescriptor.class)
                        .container("android1")
                        .setDefault()
                        .property("adapterImplClass",
                                "org.arquillian.droidium.container.AndroidDeployableContainer"),
                serviceLoader);

        registry.create(
                Descriptors.create(ArquillianDescriptor.class)
                        .container("jbossas")
                        .property("adapterImplClass", "org.jboss.as.arquillian.container.managed.ManagedDeployableContainer"),
                serviceLoader);

        Container android1 = registry.getContainer("android1");
        Container jboss = registry.getContainer("jbossas");
        Container android1b = registry.getContainer(TargetDescription.DEFAULT);

        assertNotNull(android1);
        assertNotNull(jboss);
        assertNotNull(android1b);

        assertEquals("android1", android1.getName());
        assertEquals("jbossas", jboss.getName());
        assertEquals("android1", android1b.getName());
    }
}
