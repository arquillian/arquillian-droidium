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
package org.arquillian.droidium.container.manual.configuration;

import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidContainerConfigurationException;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.execution.impl.DefaultExecutionServiceFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * Requires having installed full android-10 and android-19 platforms in your Android SDK.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidSDKTestCase {

    private static final String LOWEST_TARGET = "android-10";

    private static final String LATEST_TARGET = "android-19";

    private static final String GOOGLE_LATEST_TARGET_ARMEABI_V7A = "Google Inc.:Google APIs:19";

    private static final String GOOGLE_LATEST_TARGET_X86 = "Google Inc.:Google APIs x86:19";

    private static final String ANDROID_WEAR_ABI = "android-wear/armeabi-v7a";

    private DroidiumPlatformConfiguration platformConfiguration = new DroidiumPlatformConfiguration();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void initializateExecutionService() {
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory());
    }

    @Test
    public void testAbiNullTargetNull() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi(null);
        configuration.setTarget(null);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testAbiNullTargetSet() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi(null);
        configuration.setTarget(null);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testAbi_x86_TargetSet() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi("x86");
        configuration.setTarget(LATEST_TARGET);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testAbiSetTargetNull() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi("x86");

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testAbiSetTargetNumber() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi("x86");
        configuration.setTarget("19");

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testAbiSetTargetFullAndroid() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi("x86");
        configuration.setTarget("android-19");

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testAbiNullTargetFullGoogleAPI_ARM() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setTarget(GOOGLE_LATEST_TARGET_ARMEABI_V7A);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/armeabi-v7a", sdk.getConfiguration().getAbi());
        Assert.assertEquals(GOOGLE_LATEST_TARGET_ARMEABI_V7A, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testAbiNullTargetFullGoogleAPI_X86() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setTarget(GOOGLE_LATEST_TARGET_X86);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(GOOGLE_LATEST_TARGET_X86, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testAbiSetTargetFullGoogleAPI_X86() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi("x86");
        configuration.setTarget(GOOGLE_LATEST_TARGET_X86);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(GOOGLE_LATEST_TARGET_X86, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testAndroidWearTargetNotSet() {
        // android-19 will be picked as the latest one
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi(ANDROID_WEAR_ABI);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals(ANDROID_WEAR_ABI, sdk.getConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testLongAbiFormLatestTarget() {
        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setTarget(LATEST_TARGET);
        configuration.setAbi("default/x86");

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getConfiguration().getTarget());
    }

    // negative tests

    @Test
    public void testNonExistingAbi() {
        // there is not mips for android-10, it guesses x86

        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi("mips");
        configuration.setTarget(LOWEST_TARGET);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getConfiguration().getAbi());
        Assert.assertEquals(LOWEST_TARGET, sdk.getConfiguration().getTarget());
    }

    @Test
    public void testNonExistingTarget() {
        expectedException.expect(AndroidContainerConfigurationException.class);
        expectedException.expectMessage("There is not any target with target name 'android-100'");

        AndroidContainerConfiguration configuration = new AndroidContainerConfiguration();

        configuration.setAbi("default/x86");
        configuration.setTarget("android-100");

        new AndroidSDK(platformConfiguration).setupWith(configuration);
    }

}
