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
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * Requires having installed full android-10 and android-19 (with Google API) platforms in your Android SDK.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidSDKTestCase {

    private static final String LOWEST_TARGET = "android-10";

    private static final String LATEST_TARGET = "android-19";

    private static final String LATEST_TARGET_GOOGLE = "Google Inc.:Google APIs (x86 System Image):19";

    private DroidiumPlatformConfiguration platformConfiguration;

    private AndroidContainerConfiguration configuration;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        configuration = new AndroidContainerConfiguration();
        platformConfiguration = new DroidiumPlatformConfiguration();
        platformConfiguration.validate();
    }

    @Test
    public void testLatestTargetAndroidAPI() {
        configuration.setAbi(null);
        configuration.setTarget(LATEST_TARGET_GOOGLE);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET_GOOGLE, sdk.getAndroidContainerConfiguration().getTarget());
    }

    @Test
    public void testLatestTargetAndroidAPI_2() {
        configuration.setAbi("default/x86");
        configuration.setTarget(LATEST_TARGET_GOOGLE);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET_GOOGLE, sdk.getAndroidContainerConfiguration().getTarget());
    }

    @Test
    public void testAbiNullTargetNull() {
        configuration.setAbi(null);
        configuration.setTarget(null);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getAndroidContainerConfiguration().getTarget());
    }

    @Test
    public void testAbiNullTargetSet() {
        configuration.setAbi(null);
        configuration.setTarget(LATEST_TARGET);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getAndroidContainerConfiguration().getTarget());
    }

    @Test
    public void testAbiSetTargetNull() {
        configuration.setAbi("x86");

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getAndroidContainerConfiguration().getTarget());
    }

    @Test
    public void testAbi_x86_TargetSet() {
        configuration.setAbi("x86");
        configuration.setTarget(LATEST_TARGET);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getAndroidContainerConfiguration().getTarget());
    }

    @Test
    public void testAbiSetTargetNumber() {
        configuration.setAbi("x86");
        configuration.setTarget("19");

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getAndroidContainerConfiguration().getTarget());
    }

    @Test
    public void testAbiSetTargetFullAndroid() {
        configuration.setAbi("x86");
        configuration.setTarget(LATEST_TARGET);

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/x86", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LATEST_TARGET, sdk.getAndroidContainerConfiguration().getTarget());
    }

    @Test
    public void testAbiSetTargetSetLowestPlatformFullTarget() {
        configuration.setAbi("default/armeabi");
        configuration.setTarget("android-10");

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/armeabi", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LOWEST_TARGET, sdk.getAndroidContainerConfiguration().getTarget());
    }

    @Test
    public void testAbiSetTargetSetLowestPlatformShortTarget() {
        configuration.setAbi("default/armeabi");
        configuration.setTarget("10");

        AndroidSDK sdk = new AndroidSDK(platformConfiguration);
        sdk.setupWith(configuration);

        Assert.assertEquals("default/armeabi", sdk.getAndroidContainerConfiguration().getAbi());
        Assert.assertEquals(LOWEST_TARGET, sdk.getAndroidContainerConfiguration().getTarget());
    }

    // google tests

    @Test
    public void testGoogleTarget() {
        configuration.setAbi("default/x86");
        configuration.setTarget(LATEST_TARGET_GOOGLE);

        new AndroidSDK(platformConfiguration).setupWith(configuration);
    }

}
