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
package org.arquillian.droidium.container.automatic;

import java.io.File;

import org.arquillian.droidium.container.api.FileType;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.task.CreateKeyStoreTask;
import org.arquillian.droidium.container.utils.AndroidIdentifierGenerator;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.execution.impl.DefaultExecutionServiceFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests the creation of keystore which is needed for package signing.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class KeyStoreCreatorTestCase {

    private AndroidContainerConfiguration configuration;

    private DroidiumPlatformConfiguration platformConfiguration;

    private AndroidSDK androidSDK;

    private File keyStoreToCreate;

    @BeforeClass
    public static void initializateExecutionService() {
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory());
    }

    @Before
    public void before() {
        configuration = new AndroidContainerConfiguration();
        configuration.validate();

        platformConfiguration = new DroidiumPlatformConfiguration();
        platformConfiguration.validate();

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        keyStoreToCreate = new File(platformConfiguration.getTmpDir(), new AndroidIdentifierGenerator().getIdentifier(FileType.FILE));
    }

    @After
    public void tearDown() {
        if (keyStoreToCreate.exists()) {
            boolean deleted = keyStoreToCreate.delete();
            if (!deleted) {
                throw new RuntimeException("Unable to delete files after test!");
            }
        }
    }

    @Test
    public void createKeyStoreTest() {
        Tasks.chain(keyStoreToCreate, CreateKeyStoreTask.class).sdk(androidSDK).execute().await();
        Assert.assertTrue(keyStoreToCreate.exists());
    }
}
