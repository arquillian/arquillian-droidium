/*
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
package org.arquillian.droidium.container.configuration;

import java.util.ArrayList;
import java.util.List;

import org.arquillian.droidium.container.configuration.target.ABI;
import org.arquillian.droidium.container.configuration.target.TAG;
import org.arquillian.droidium.container.configuration.target.TARGET_TYPE;
import org.arquillian.droidium.container.configuration.target.Target;
import org.arquillian.droidium.container.configuration.target.TargetBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class TargetBuilderTest {

    @Test
    public void parseAddonTarget() {

        List<String> targetLines = getTargetLinesForAddon();

        Target target = TargetBuilder.build(targetLines);

        Assert.assertEquals(17, target.getId());
        Assert.assertEquals("Google Inc.:Google APIs:3", target.getIdLabel());
        Assert.assertEquals("Google APIs", target.getName());
        Assert.assertEquals(TARGET_TYPE.ADD_ON, target.getTargetType());
        Assert.assertEquals("Google Inc.", target.getVendor());
        Assert.assertEquals(3, target.getRevision());
        Assert.assertEquals("Android + Google APIs", target.getDescription());
        Assert.assertEquals(5, target.getSkins().size());
        Assert.assertEquals("HVGA", target.getDefaultSkin());

        Assert.assertEquals(1, target.getTagAbisForAbi(ABI.ARMEABI).size());
        Assert.assertEquals(1, target.getTagAbisForAbi(ABI.X86).size());
        Assert.assertEquals(1, target.getTagAbisForAbi(ABI.X86_64).size());
        Assert.assertEquals(1, target.getTagAbisForTag(TAG.DEFAULT).size());

        Assert.assertTrue(target.hasTagAbi(TAG.GOOGLE_APIS, ABI.ARMEABI_V7A));

        Assert.assertTrue(target.hasAbi(ABI.ARMEABI));
        Assert.assertTrue(target.hasAbi(ABI.ARMEABI_V7A));
        Assert.assertTrue(target.hasAbi(ABI.X86));
        Assert.assertTrue(target.hasAbi(ABI.X86_64));

        Assert.assertTrue(target.hasTag(TAG.DEFAULT));
        Assert.assertTrue(target.hasTag(TAG.GOOGLE_APIS));

        Assert.assertEquals(4, target.numberOfTagAbis());
        Assert.assertEquals(4, target.getAbis().size());

        Assert.assertTrue(target.isAddOn());

        Assert.assertEquals(1, target.getDefaultAbis().size());
    }

    @Test
    public void parsePlatformTarget() {

        List<String> targetLines = getTargetLinesForPlatform();

        Target target = TargetBuilder.build(targetLines);

        Assert.assertEquals(9, target.getId());
        Assert.assertEquals("android-14", target.getIdLabel());
        Assert.assertEquals("Android 4.0", target.getName());
        Assert.assertEquals(14, target.getApiLevel());
        Assert.assertEquals(TARGET_TYPE.PLATFORM, target.getTargetType());
        Assert.assertTrue(target.isPlatorm());

        Assert.assertEquals(4, target.getRevision());

        Assert.assertEquals(9, target.getSkins().size());
        Assert.assertEquals("WVGA800", target.getDefaultSkin());

        Assert.assertEquals(1, target.getTagAbisForAbi(ABI.ARMEABI_V7A).size());
        Assert.assertEquals(0, target.getTagAbisForAbi(ABI.X86).size());
        Assert.assertEquals(0, target.getTagAbisForAbi(ABI.X86_64).size());
        Assert.assertEquals(1, target.getTagAbisForTag(TAG.DEFAULT).size());

        Assert.assertTrue(target.hasTagAbi(TAG.DEFAULT, ABI.ARMEABI_V7A));

        Assert.assertTrue(target.hasAbi(ABI.ARMEABI_V7A));
        Assert.assertFalse(target.hasAbi(ABI.X86_64));

        Assert.assertTrue(target.hasTag(TAG.DEFAULT));
        Assert.assertFalse(target.hasTag(TAG.GOOGLE_APIS));

        Assert.assertEquals(1, target.numberOfTagAbis());
        Assert.assertEquals(1, target.getAbis().size());

        Assert.assertTrue(target.isPlatorm());

        Assert.assertEquals(1, target.getDefaultAbis().size());
    }

    private List<String> getTargetLinesForPlatform() {
        List<String> targetLines = new ArrayList<String>();

        targetLines.add("id: 9 or \"android-14\"");
        targetLines.add("Name: Android 4.0");
        targetLines.add("Type: Platform");
        targetLines.add("API level: 14");
        targetLines.add("Revision: 4");
        targetLines.add("Skins: HVGA, QVGA, WQVGA400, WQVGA432, WSVGA, WVGA800 (default), WVGA854, WXGA720, WXGA800");
        targetLines.add("Tag/ABIs : default/armeabi-v7a");

        return targetLines;
    }

    private List<String> getTargetLinesForAddon() {
        List<String> targetLines = new ArrayList<String>();

        targetLines.add("id: 17 or \"Google Inc.:Google APIs:3\"");
        targetLines.add("Name: Google APIs");
        targetLines.add("Type: Add-On");
        targetLines.add("Vendor: Google Inc.");
        targetLines.add("Revision: 3");
        targetLines.add("Description: Android + Google APIs");
        targetLines.add("Based on Android 1.5 (API level 3)");
        targetLines.add("Libraries:");
        targetLines.add("* com.google.android.maps (maps.jar)");
        targetLines.add("API for Google Maps");
        targetLines.add("Skins: HVGA (default), HVGA-L, HVGA-P, QVGA-L, QVGA-P");
        targetLines.add("Tag/ABIs : default/armeabi, google_apis/armeabi-v7a, google_apis/x86, google_apis/x86_64");

        return targetLines;
    }

}
