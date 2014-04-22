/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.container.impl;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceMetadata;
import org.arquillian.droidium.container.api.AndroidDeviceRegister;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class AndroidDeviceRegisterTestCase {

    private AndroidDeviceRegister androidDeviceRegister;

    private AndroidDeviceImpl androidDevice1;

    private AndroidDevice androidDevice2;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        androidDeviceRegister = new AndroidDeviceRegisterImpl();
        androidDevice1 = new AndroidDeviceImpl();
        androidDevice2 = new AndroidDeviceImpl();
    }

    @Test
    public void getSingleDeviceWhenNoneWasPutTest() {
        expectedException.expect(IllegalStateException.class);
        androidDeviceRegister.getSingle();
    }

    @Test
    public void getSingleDeviceWhenTwoWasPutTest() {
        expectedException.expect(IllegalStateException.class);

        androidDeviceRegister.put(androidDevice1, new AndroidDeviceMetadata());
        androidDeviceRegister.put(androidDevice2, new AndroidDeviceMetadata());

        androidDeviceRegister.getSingle();
    }

    @Test
    public void getMetadataTest() {
        androidDeviceRegister.put(androidDevice1, new AndroidDeviceMetadata());
        androidDeviceRegister.put(androidDevice2, new AndroidDeviceMetadata());

        AndroidDeviceMetadata metadata1 = androidDeviceRegister.getMetadata(androidDevice1);
        AndroidDeviceMetadata metadata2 = androidDeviceRegister.getMetadata(androidDevice2);

        Assert.assertNotNull(metadata1);
        Assert.assertNotNull(metadata2);
        Assert.assertNotSame(metadata1, metadata2);
    }

    @Test
    public void containsTest() {
        androidDeviceRegister.put(androidDevice1, new AndroidDeviceMetadata());

        Assert.assertTrue(androidDeviceRegister.contains(androidDevice1));
        Assert.assertFalse(androidDeviceRegister.contains(androidDevice2));
    }

    @Test
    public void removeTest() {
        androidDeviceRegister.put(androidDevice1, new AndroidDeviceMetadata());
        androidDeviceRegister.put(androidDevice2, new AndroidDeviceMetadata());

        Assert.assertEquals(2, androidDeviceRegister.size());

        androidDeviceRegister.remove(androidDevice1);

        Assert.assertEquals(1, androidDeviceRegister.size());

        androidDeviceRegister.remove(androidDevice2);

        Assert.assertEquals(0, androidDeviceRegister.size());
    }

    @Test
    public void removeByContainerQualifier() {

        AndroidDeviceMetadata metadata1 = new AndroidDeviceMetadata();
        metadata1.setContainerQualifier("container1");

        AndroidDeviceMetadata metadata2 = new AndroidDeviceMetadata();
        metadata2.setContainerQualifier("container2");

        androidDeviceRegister.put(androidDevice1, metadata1);
        androidDeviceRegister.put(androidDevice2, metadata2);

        Assert.assertEquals(2, androidDeviceRegister.size());

        androidDeviceRegister.removeByContainerQualifier("container1");

        Assert.assertEquals(1, androidDeviceRegister.size());

        androidDeviceRegister.removeByContainerQualifier("nonexistent");
        Assert.assertEquals(1, androidDeviceRegister.size());

        androidDeviceRegister.removeByContainerQualifier("container2");

        Assert.assertEquals(0, androidDeviceRegister.size());
    }

    @Test
    public void addDeploymentForDeviceTest() {
        AndroidDeviceMetadata metadata1 = new AndroidDeviceMetadata();
        metadata1.setContainerQualifier("container1");

        androidDeviceRegister.put(androidDevice1, metadata1);
        androidDeviceRegister.addDeploymentForDevice(androidDevice1, "someDeploymentName");

        AndroidDevice device = androidDeviceRegister.getByDeploymentName("someDeploymentName");

        Assert.assertNotNull(device);
        Assert.assertEquals(device, androidDevice1);

        AndroidDeviceMetadata metaData = androidDeviceRegister.getMetadata(device);
        Assert.assertEquals(metaData, metadata1);

        Assert.assertTrue(metaData.getDeploymentNames().contains("someDeploymentName"));
    }

    @Test
    public void getByContainerQualifier() {
        AndroidDeviceMetadata metadata1 = new AndroidDeviceMetadata();
        metadata1.setContainerQualifier("container1");

        androidDeviceRegister.put(androidDevice1, metadata1);

        AndroidDevice device = androidDeviceRegister.getByContainerQualifier("container1");

        Assert.assertNotNull(device);
        Assert.assertEquals(device, androidDevice1);

        AndroidDevice device2 = androidDeviceRegister.getByContainerQualifier("container2");

        Assert.assertNull(device2);
    }

    @Test
    public void getByDeploymentName() {
        AndroidDeviceMetadata metadata1 = new AndroidDeviceMetadata();
        metadata1.setContainerQualifier("container1");

        androidDeviceRegister.put(androidDevice1, metadata1);

        androidDeviceRegister.addDeploymentForDevice(androidDevice1, "someDeploymentName");

        AndroidDevice device = androidDeviceRegister.getByDeploymentName("someDeploymentName");

        Assert.assertNotNull(device);
        Assert.assertEquals(device, androidDevice1);
    }
}
