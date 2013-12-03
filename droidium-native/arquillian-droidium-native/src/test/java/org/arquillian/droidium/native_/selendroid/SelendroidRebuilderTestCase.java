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
package org.arquillian.droidium.native_.selendroid;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class SelendroidRebuilderTestCase {

    @Test
    public void testReplacer() {
        List<String> lines = new ArrayList<String>();
        lines.add("xyz package=io.selendroid blablabalb");
        lines.add("android:targetPackage=\"io.selendroid.testapp\" />");
        lines.add("android:icon=\"@drawable/selenium_icon\"");

        List<String> filtered1 = SelendroidRebuilder.Replacer.replace(lines, "package=io.selendroid", "package=io.selendroid_1");
        List<String> filtered2 = SelendroidRebuilder.Replacer.replace(filtered1, "io.selendroid.testapp", "my.test.app");
        List<String> filtered3 = SelendroidRebuilder.Replacer.replace(filtered2, "android:icon=\"@drawable/selenium_icon\"", "");

        Assert.assertTrue(contains(filtered3, "xyz package=io.selendroid_1 blablabalb"));
        Assert.assertTrue(contains(filtered3, "android:targetPackage=\"my.test.app\" />"));
        Assert.assertFalse(contains(filtered3, "icon"));
    }

    private boolean contains(List<String> list, String toFind) {
        for (String line : list) {
            if (line.contains(toFind)) {
                return true;
            }
        }
        return false;
    }
}
