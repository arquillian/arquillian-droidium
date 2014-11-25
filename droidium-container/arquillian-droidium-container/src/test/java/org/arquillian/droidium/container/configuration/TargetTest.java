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
import java.util.Collections;
import java.util.List;

import org.arquillian.droidium.container.configuration.target.ABI;
import org.arquillian.droidium.container.configuration.target.TAG;
import org.arquillian.droidium.container.configuration.target.TagAbiPair;
import org.arquillian.droidium.container.configuration.target.Target;
import org.arquillian.droidium.container.configuration.target.TargetRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class TargetTest {

    private TargetRegistry targetRegistry;

    @Before
    public void setup() {
        targetRegistry = new TargetRegistry();
    }

    @Test
    public void testTargetTagNullAbiNull() {
        Target target = new Target();
        target.addTagAbi((String) null, (String) null);

        targetRegistry.addTarget(target);

        Assert.assertEquals(1, targetRegistry.getTargets().size());
        Assert.assertEquals(0, targetRegistry.getByABI(ABI.ARMEABI).size());
    }

    @Test
    public void testTargetTagNullAbiNotNull() {
        Target target = new Target();
        target.addTagAbi(null, ABI.ARMEABI);

        targetRegistry.addTarget(target);

        Assert.assertEquals(1, targetRegistry.getTargets().size());
        Assert.assertEquals(0, targetRegistry.getByTag(TAG.DEFAULT).size());
        Assert.assertEquals(0, targetRegistry.getByABI(ABI.ARMEABI).size());
    }

    @Test
    public void testTargetTagNotNullAbiNull() {
        Target target = new Target();
        target.addTagAbi(TAG.ANDROID_TV, null);

        targetRegistry.addTarget(target);

        Assert.assertEquals(1, targetRegistry.getTargets().size());
        Assert.assertEquals(0, targetRegistry.getByTag(TAG.ANDROID_TV).size());
    }

    @Test
    public void testTargetTagNotNullAbiNotNull() {
        Target target = new Target();
        target.addTagAbi(TAG.DEFAULT, ABI.ARMEABI);

        targetRegistry.addTarget(target);

        Assert.assertEquals(1, targetRegistry.getTargets().size());
        Assert.assertEquals(1, targetRegistry.getByTag(TAG.DEFAULT).size());
        Assert.assertEquals(1, targetRegistry.getByABI(ABI.ARMEABI).size());
        Assert.assertEquals(1, targetRegistry.getByPair(TAG.DEFAULT, ABI.ARMEABI).size());
    }

    @Test
    public void testTargetTagAbiPairsFindByTag() {
        Target target = new Target();
        target.addTagAbi(TAG.DEFAULT, ABI.ARMEABI);
        target.addTagAbi(TAG.DEFAULT, ABI.ARMEABI_V7A);

        Assert.assertEquals(2, target.getTagAbisForTag(TAG.DEFAULT).size());
        Assert.assertEquals(1, target.getTagAbisForAbi(ABI.ARMEABI).size());
        Assert.assertEquals(0, target.getTagAbisForAbi(ABI.X86).size());
    }

    @Test
    public void tagAbiPairComparisionWhenEqual() {
        TagAbiPair tagAbiPair1 = new TagAbiPair(TAG.DEFAULT, ABI.ARMEABI);
        TagAbiPair tagAbiPair2 = new TagAbiPair(TAG.DEFAULT, ABI.ARMEABI);

        Assert.assertEquals(0, tagAbiPair1.compareTo(tagAbiPair2));
    }

    @Test
    public void tagAbiPairComparision() {
        List<TagAbiPair> tagAbiPairs = new ArrayList<TagAbiPair>();

        for (TAG tag : TAG.values()) {
            for (ABI abi : ABI.values()) {
                tagAbiPairs.add(new TagAbiPair(tag, abi));
            }
        }

        List<TagAbiPair> toSort = new ArrayList<TagAbiPair>(tagAbiPairs);

        Collections.sort(toSort);

        for (int i = 0; i < tagAbiPairs.size(); i++) {
            Assert.assertEquals(tagAbiPairs.get(i), toSort.get(i));
        }
    }
}
