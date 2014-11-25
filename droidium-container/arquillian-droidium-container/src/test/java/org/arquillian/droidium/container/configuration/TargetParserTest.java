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
import org.arquillian.droidium.container.configuration.target.TagAbiPair;
import org.arquillian.droidium.container.configuration.target.Target;
import org.arquillian.droidium.container.configuration.target.TargetParser;
import org.arquillian.droidium.container.configuration.target.TargetRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class TargetParserTest {

    @Test
    public void parseTargets() {
        TargetParser targetParser = new TargetParser();

        List<Target> targets = targetParser.parseAndroidListTargetOutput(getOutputLines());

        Assert.assertEquals(4, targets.size());

        Target target1 = targets.get(0);
        Target target2 = targets.get(1);
        Target target4 = targets.get(3);

        Assert.assertEquals(1, target1.getId());
        Assert.assertEquals("android-3", target1.getIdLabel());
        Assert.assertEquals("Android 1.5", target1.getName());
        Assert.assertEquals(TARGET_TYPE.PLATFORM, target1.getTargetType());
        Assert.assertEquals(3, target1.getApiLevel());
        Assert.assertEquals(4, target1.getRevision());
        Assert.assertEquals(5, target1.getSkins().size());
        Assert.assertEquals(1, target1.getAbis().size());
        Assert.assertEquals(1, target1.getTagAbisForAbi(ABI.ARMEABI).size());
        Assert.assertEquals(1, target1.getTagAbisForTag(TAG.DEFAULT).size());
        Assert.assertTrue(target1.hasTagAbi(new TagAbiPair(TAG.DEFAULT, ABI.ARMEABI)));
        Assert.assertTrue(target1.hasTagAbi(TAG.DEFAULT, ABI.ARMEABI));
        Assert.assertEquals("HVGA", target1.getDefaultSkin());
        Assert.assertTrue(target1.isPlatorm());

        Assert.assertEquals(2, target2.getId());
        Assert.assertEquals("android-4", target2.getIdLabel());
        Assert.assertEquals("Android 1.6", target2.getName());
        Assert.assertEquals(TARGET_TYPE.PLATFORM, target2.getTargetType());
        Assert.assertEquals(4, target2.getApiLevel());
        Assert.assertEquals(3, target2.getRevision());
        Assert.assertEquals(4, target2.getSkins().size());
        Assert.assertEquals(1, target2.getAbis().size());
        Assert.assertEquals(1, target2.getTagAbisForAbi(ABI.ARMEABI).size());
        Assert.assertEquals(1, target2.getTagAbisForTag(TAG.DEFAULT).size());
        Assert.assertTrue(target2.hasTagAbi(new TagAbiPair(TAG.DEFAULT, ABI.ARMEABI)));
        Assert.assertTrue(target2.hasTagAbi(TAG.DEFAULT, ABI.ARMEABI));
        Assert.assertEquals("WVGA800", target2.getDefaultSkin());
        Assert.assertTrue(target2.isPlatorm());

        Assert.assertEquals(33, target4.getId());
        Assert.assertEquals("Google Inc.:Glass Development Kit Preview:19", target4.getIdLabel());
        Assert.assertEquals("Glass Development Kit Preview", target4.getName());
        Assert.assertEquals(TARGET_TYPE.ADD_ON, target4.getTargetType());
        Assert.assertEquals(11, target4.getRevision());
        Assert.assertEquals(6, target4.getSkins().size());
        Assert.assertEquals(0, target4.getAbis().size());
        Assert.assertEquals(0, target4.getTagAbisForAbi(ABI.ARMEABI).size());
        Assert.assertEquals(0, target4.getTagAbisForTag(TAG.DEFAULT).size());
        Assert.assertFalse(target4.hasTagAbi(new TagAbiPair(TAG.DEFAULT, ABI.ARMEABI)));
        Assert.assertFalse(target4.hasTagAbi(TAG.DEFAULT, ABI.ARMEABI));
        Assert.assertEquals("WVGA800", target4.getDefaultSkin());
        Assert.assertTrue(target4.isAddOn());
    }

    @Test
    public void parseEmptyLines() {
        TargetParser targetParser = new TargetParser();

        List<Target> targets = targetParser.parseAndroidListTargetOutput(new ArrayList<String>());

        Assert.assertTrue(targets.isEmpty());
    }

    @Test
    public void targetRegistryTest() {
        TargetRegistry targetRegistry = new TargetRegistry();
        TargetParser targetParser = new TargetParser();

        List<Target> targets = targetParser.parseAndroidListTargetOutput(getOutputLines());

        targetRegistry.addTargets(targets);

        Assert.assertEquals(4, targetRegistry.getTargets().size());
        Assert.assertEquals(2, targetRegistry.getByPair(TAG.DEFAULT, ABI.ARMEABI).size());
        Assert.assertEquals(1, targetRegistry.getByPair(TAG.DEFAULT, ABI.ARMEABI_V7A).size());
        Assert.assertEquals(2, targetRegistry.getByABI(ABI.ARMEABI).size());
        Assert.assertEquals(1, targetRegistry.getByABI(ABI.X86).size());
        Assert.assertEquals(3, targetRegistry.getByTag(TAG.DEFAULT).size());
        Assert.assertEquals(1, targetRegistry.getTargetsWithNoAbis().size());
        Assert.assertEquals(33, targetRegistry.getHighest(TARGET_TYPE.ADD_ON).getId());
        Assert.assertEquals(14, targetRegistry.getHighest(TARGET_TYPE.PLATFORM).getId());
        Assert.assertEquals(1, targetRegistry.getLowest(TARGET_TYPE.PLATFORM).getId());

    }

    private List<String> getOutputLines() {

        List<String> output = new ArrayList<String>();

        output.add("Available Android targets:");
        output.add("----------");
        output.add("id: 1 or \"android-3\"");
        output.add("     Name: Android 1.5");
        output.add("     Type: Platform");
        output.add("     API level: 3");
        output.add("     Revision: 4");
        output.add("     Skins: HVGA (default), HVGA-L, HVGA-P, QVGA-L, QVGA-P");
        output.add(" Tag/ABIs : default/armeabi");
        output.add("----------");
        output.add("id: 2 or \"android-4\"");
        output.add("     Name: Android 1.6");
        output.add("     Type: Platform");
        output.add("     API level: 4");
        output.add("     Revision: 3");
        output.add("     Skins: HVGA, QVGA, WVGA800 (default), WVGA854");
        output.add(" Tag/ABIs : default/armeabi");
        output.add("----------");
        output.add("id: 14 or \"android-19\"");
        output.add("     Name: Android 4.4.2");
        output.add("     Type: Platform");
        output.add("     API level: 19");
        output.add("     Revision: 4");
        output.add("     Skins: HVGA, QVGA, WQVGA400, WQVGA432, WSVGA, WVGA800 (default)");
        output.add(" Tag/ABIs : android-wear/armeabi-v7a, default/armeabi-v7a, default/x86");
        output.add("----------");
        output.add("id: 33 or \"Google Inc.:Glass Development Kit Preview:19\"");
        output.add("     Name: Glass Development Kit Preview");
        output.add("     Type: Add-On");
        output.add("     Vendor: Google Inc.");
        output.add("     Revision: 11");
        output.add("     Description: Preview of the Glass Development Kit");
        output.add("     Based on Android 4.4.2 (API level 19)");
        output.add("     Libraries:");
        output.add("      * com.google.android.glass (gdk.jar)");
        output.add("          APIs for Glass Development Kit Preview");
        output.add("      Skins: HVGA, QVGA, WQVGA400, WQVGA432, WSVGA, WVGA800 (default)");
        output.add(" Tag/ABIs : no ABIs.");

        return output;
    }
}
