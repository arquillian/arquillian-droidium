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

package org.arquillian.droidium.container.automatic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.arquillian.droidium.container.api.FileType;
import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.AndroidSDCardManagerImpl;
import org.arquillian.droidium.container.spi.event.AndroidSDCardCreate;
import org.arquillian.droidium.container.spi.event.AndroidSDCardCreated;
import org.arquillian.droidium.container.spi.event.AndroidSDCardDelete;
import org.arquillian.droidium.container.spi.event.AndroidSDCardDeleted;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test creation and deletion of SD card with various configuration scenarios.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AndroidSDCardManagerTestCase extends AbstractContainerTestTestBase {

    private static AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

    private static DroidiumPlatformConfiguration platformConfiguration = new DroidiumPlatformConfiguration();

    private AndroidSDK androidSDK;

    private static final String SD_CARD = "340df030-8994-11e2-9e96-0800200c9a66.img";

    private static final String SD_CARD_LABEL = "ba817e70-8994-11e2-9e96-0800200c9a66";

    private static String SD_PATH;

    private static final String SD_SIZE = "128M";

    @Mock
    private IdentifierGenerator<FileType> idGenerator;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(AndroidSDCardManagerImpl.class);
    }

    @BeforeClass
    public static void initializateExecutionService() {

        configuration.validate();
        platformConfiguration.validate();

        SD_PATH = new File(platformConfiguration.getTmpDir(), SD_CARD).getAbsolutePath();
    }

    @Before
    public void setup() {
        getManager().getContext(ContainerContext.class).activate("doesnotmatter");

        Mockito.when(idGenerator.getIdentifier(FileType.SD_CARD)).thenReturn(SD_CARD);
        Mockito.when(idGenerator.getIdentifier(FileType.SD_CARD_LABEL)).thenReturn(SD_CARD_LABEL);
        bind(ContainerScoped.class, IdentifierGenerator.class, idGenerator);

    }

    @After
    public void deleteFiles() {
        File f = new File(SD_PATH);
        if (f.exists()) {
            boolean deleted = f.delete();
            if (!deleted) {
                throw new RuntimeException("Unable to delete files after test!");
            }
        }
    }

    @AfterClass
    public static void cleanUp() {
        File f = new File(SD_PATH);
        if (f.exists()) {
            boolean deleted = f.delete();
            if (!deleted) {
                throw new RuntimeException("Unable to delete files after test!");
            }
        }
    }

    @Test
    public void testGenerateTrueSDCardNull() {
        setupSDCardConfiguration(configuration, null, SD_SIZE, SD_CARD_LABEL, true);

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ApplicationScoped.class, DroidiumPlatformConfiguration.class, platformConfiguration);
        bind(ApplicationScoped.class, AndroidSDK.class, androidSDK);

        fire(new AndroidSDCardCreate());

        assertEventFired(AndroidSDCardCreate.class, 1);
        assertEventFired(AndroidSDCardCreated.class, 1);

        assertTrue(new File(SD_PATH).exists());
    }

    @Test
    public void testGenerateTrueSDCardNotNull() {
        setupSDCardConfiguration(configuration, SD_PATH, SD_SIZE, SD_CARD_LABEL, true);

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ApplicationScoped.class, DroidiumPlatformConfiguration.class, platformConfiguration);
        bind(ApplicationScoped.class, AndroidSDK.class, androidSDK);

        fire(new AndroidSDCardCreate());

        assertEventFired(AndroidSDCardCreate.class, 1);
        assertEventFired(AndroidSDCardCreated.class, 1);

        assertTrue(new File(SD_PATH).exists());
    }

    @Test
    public void testGenerateTrueSDCardNotNullSDCardExists() throws IOException {

        assertTrue("file of SD_PATH already exists!", new File(SD_PATH).createNewFile());

        setupSDCardConfiguration(configuration, SD_PATH, SD_SIZE, SD_CARD_LABEL, true);

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ApplicationScoped.class, DroidiumPlatformConfiguration.class, platformConfiguration);
        bind(ApplicationScoped.class, AndroidSDK.class, androidSDK);

        fire(new AndroidSDCardCreate());

        assertEventFired(AndroidSDCardCreate.class, 1);
        assertEventFired(AndroidSDCardCreated.class, 0);

        assertTrue(configuration.getGenerateSDCard() == false);

        assertTrue(new File(SD_PATH).exists());
    }

    @Test
    public void testGenerateTrueSDCardNotNullSDCardDoesNotExist() {

        assertFalse("file of SD_PATH already exists!", new File(SD_PATH).exists());

        setupSDCardConfiguration(configuration, SD_PATH, SD_SIZE, SD_CARD_LABEL, true);

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ApplicationScoped.class, DroidiumPlatformConfiguration.class, platformConfiguration);
        bind(ApplicationScoped.class, AndroidSDK.class, androidSDK);

        fire(new AndroidSDCardCreate());

        assertEventFired(AndroidSDCardCreate.class, 1);
        assertEventFired(AndroidSDCardCreated.class, 1);

        assertTrue(new File(SD_PATH).exists());
    }

    @Test
    public void testGenerateFalseSDCardNull() {

        setupSDCardConfiguration(configuration, null, SD_SIZE, SD_CARD_LABEL, false);

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ApplicationScoped.class, DroidiumPlatformConfiguration.class, platformConfiguration);
        bind(ApplicationScoped.class, AndroidSDK.class, androidSDK);

        fire(new AndroidSDCardCreate());

        assertEventFired(AndroidSDCardCreate.class, 1);
        assertEventFired(AndroidSDCardCreated.class, 0);
    }

    @Test
    public void testGenerateFalseSDCardNotNullSDCardExists() throws IOException {

        assertTrue("file of SD_PATH already exists!", new File(SD_PATH).createNewFile());

        setupSDCardConfiguration(configuration, SD_PATH, SD_SIZE, SD_CARD_LABEL, false);

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ApplicationScoped.class, DroidiumPlatformConfiguration.class, platformConfiguration);
        bind(ApplicationScoped.class, AndroidSDK.class, androidSDK);

        fire(new AndroidSDCardCreate());

        assertEventFired(AndroidSDCardCreate.class, 1);
        assertEventFired(AndroidSDCardCreated.class, 0);
    }

    @Test
    public void testGenerateFalseSDCardNotNullSDCardDoesNotExist() {

        assertTrue("file of SD_PATH already exists!", !new File(SD_PATH).exists());

        setupSDCardConfiguration(configuration, SD_PATH, SD_SIZE, SD_CARD_LABEL, false);

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ApplicationScoped.class, DroidiumPlatformConfiguration.class, platformConfiguration);
        bind(ApplicationScoped.class, AndroidSDK.class, androidSDK);

        fire(new AndroidSDCardCreate());

        assertEventFired(AndroidSDCardCreate.class, 1);
        assertEventFired(AndroidSDCardCreated.class, 0);
    }

    @Test
    public void testDeleteSDCard() throws IOException {
        assertTrue("file of SD_PATH already exists!", new File(SD_PATH).createNewFile());

        setupSDCardConfiguration(configuration, SD_PATH, SD_SIZE, SD_CARD_LABEL, true);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);

        fire(new AndroidSDCardDelete());

        assertEventFired(AndroidSDCardDelete.class, 1);
        assertEventFired(AndroidSDCardDeleted.class, 1);

        assertTrue("File supposed to be deleted was not!", !new File(SD_PATH).exists());
    }

    private AndroidContainerConfiguration setupSDCardConfiguration(AndroidContainerConfiguration config,
        String sdFileName, String sdSize, String sdLabel, boolean generated) {
        config.setSdCard(sdFileName);
        config.setSdCardLabel(sdLabel);
        config.setSdSize(sdSize);
        config.setGenerateSDCard(generated);
        return config;
    }
}
