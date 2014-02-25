/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
package org.arquillian.droidium.multiplecontainers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.api.GroupDef;
import org.jboss.arquillian.config.descriptor.impl.ContainerDefImpl;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * Registers all container adapters.
 *
 * @author Dominik Pospisil <dpospisi@redhat.com>
 * @author Stefan Miklosovic <smikloso@redhat.com>
 */
public class MultipleContainerRegistryCreator {

    static final String ARQUILLIAN_LAUNCH_PROPERTY = "arquillian.launch";
    static final String ARQUILLIAN_LAUNCH_DEFAULT = "arquillian.launch";

    private Logger logger = Logger.getLogger(MultipleContainerRegistryCreator.class.getName());

    @Inject
    @ApplicationScoped
    private InstanceProducer<ContainerRegistry> registry;

    @Inject
    private Instance<Injector> injector;

    @Inject
    private Instance<ServiceLoader> loader;

    public void createRegistry(@Observes ArquillianDescriptor event) {
        MultipleLocalContainersRegistry reg = new MultipleLocalContainersRegistry(injector.get());
        ServiceLoader serviceLoader = loader.get();

        Collection<DeployableContainer> containers = serviceLoader.all(DeployableContainer.class);

        if (containers.size() == 0) {
            throw new ContainerAdapterNotFoundException("There are not any container adapters on the classpath");
        }

        validateConfiguration(event);

        String activeConfiguration = getActivatedConfiguration();

        for (ContainerDef container : event.getContainers()) {
            if ((activeConfiguration != null && activeConfiguration.equals(container.getContainerName()))
                || (activeConfiguration == null && container.isDefault())) {
                if (isCreatingContainer(container, containers)) {
                    reg.create(container, serviceLoader);
                }
            }
        }

        for (GroupDef group : event.getGroups()) {
            if ((activeConfiguration != null && activeConfiguration.equals(group.getGroupName()))
                || (activeConfiguration == null && group.isGroupDefault())) {
                for (ContainerDef container : group.getGroupContainers()) {
                    if (isCreatingContainer(container, containers)) {
                        reg.create(container, serviceLoader);
                    }
                }
            }
        }

        if (activeConfiguration == null && reg.getContainers().size() == 0) {
            DeployableContainer<?> deployableContainer = null;
            try {
                // 'check' if there are any DeployableContainers on CP
                deployableContainer = serviceLoader.onlyOne(DeployableContainer.class);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Could not add a default container to registry because multiple "
                    + DeployableContainer.class.getName() + " found on classpath.", e);
            } catch (Exception e) {
                throw new IllegalStateException("Could not create the default container instance.", e);
            }
            if (deployableContainer != null) {
                reg.create(new ContainerDefImpl("arquillian.xml").setContainerName("default"), serviceLoader);
            }
        } else if (activeConfiguration != null && reg.getContainers().size() == 0) {
            throw new IllegalArgumentException("No container or group found that matches given qualifier: "
                + activeConfiguration);
        }

        // export
        registry.set(reg);
    }

    @SuppressWarnings("rawtypes")
    private boolean isCreatingContainer(ContainerDef containerDef, Collection<DeployableContainer> containers) {

        if (ContainerGuesser.guessDeployableContainer(containerDef, containers) != null) {
            return true;
        }

        // double check if it is Android / Droidium container
        if (ContainerGuesser.isDroidiumContainer(containerDef, containers)) {
            return true;
        }

        // if we are not able to guess it, we look if it has adapterImplClass property and we try to load that class
        // and we check that the class is an instance of DeployableContainer
        if (ContainerGuesser.hasAdapterImplClassProperty(containerDef)) {
            if (SecurityActions.isClassPresent(ContainerGuesser.getAdapterImplClassValue(containerDef))) {
            	return DeployableContainer.class.isAssignableFrom(
            			SecurityActions.loadClass(ContainerGuesser.getAdapterImplClassValue(containerDef))); 
            }
        }

        logger.log(Level.WARNING, "Unable to guess container type of name {0} or not such container adapter was found on the "
            + "class path. It is expected that you pass {1} property with class name which implements DeployableContainer "
            + "interface for given container definition. It is not necessary to specify adapterImplClass property in case "
            + "your container qualifier name in arquillian.xml, after lowercasing, contains or is equal to word like: {2}. "
            + "It is expected that when you name your container like that, you put Arquillian container adapter for that "
            + "container on classpath in order to start it afterwards.",
            new Object[] {
                    containerDef.getContainerName(),
                    ContainerGuesser.ADAPTER_IMPL_CONFIG_STRING,
                    ContainerType.getAll()
                });

        return false;
    }

    /**
     * Validate that the Configuration given is sane
     *
     * @param desc The read Descriptor
     */
    private void validateConfiguration(ArquillianDescriptor desc) {
        Object defaultConfig = null;

        // verify only one container is marked as default
        for (ContainerDef container : desc.getContainers()) {
            if (container.isDefault()) {
                if (defaultConfig != null) {
                    throw new IllegalStateException("Multiple Containers defined as default, only one is allowed:\n"
                        + defaultConfig + ":" + container);
                }
                defaultConfig = container;
            }
        }

        boolean containerMarkedAsDefault = defaultConfig != null;

        // verify only one container or group is marked as default
        for (GroupDef group : desc.getGroups()) {
            if (group.isGroupDefault()) {
                if (defaultConfig != null) {
                    if (containerMarkedAsDefault) {
                        throw new IllegalStateException(
                            "Multiple Containers/Groups defined as default, only one is allowed:\n" + defaultConfig
                                + ":" + group);
                    }
                    throw new IllegalStateException("Multiple Groups defined as default, only one is allowed:\n"
                        + defaultConfig + ":" + group);
                }
                defaultConfig = group;
            }

            ContainerDef defaultInGroup = null;
            // verify only one container in group is marked as default
            for (ContainerDef container : group.getGroupContainers()) {
                if (container.isDefault()) {
                    if (defaultInGroup != null) {
                        throw new IllegalStateException(
                            "Multiple Containers within Group defined as default, only one is allowed:\n" + group);
                    }
                    defaultInGroup = container;
                }
            }
        }
    }

    private String getActivatedConfiguration() {
        if (exists(System.getProperty(ARQUILLIAN_LAUNCH_PROPERTY))) {
            return System.getProperty(ARQUILLIAN_LAUNCH_PROPERTY);
        }

        InputStream arquillianLaunchStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(ARQUILLIAN_LAUNCH_DEFAULT);
        if (arquillianLaunchStream != null) {
            try {
                return readActivatedValue(new BufferedReader(new InputStreamReader(arquillianLaunchStream)));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not read resource " + ARQUILLIAN_LAUNCH_DEFAULT, e);
            }
        }
        return null;
    }

    private String readActivatedValue(BufferedReader reader) throws Exception {
        try {
            String value;
            while ((value = reader.readLine()) != null) {
                if (value.startsWith("#")) {
                    continue;
                }
                return value;
            }
        } finally {
            reader.close();
        }
        return null;
    }

    private boolean exists(String value) {
        if (value == null || value.trim().length() == 0) {
            return false;
        }
        return true;
    }

}
