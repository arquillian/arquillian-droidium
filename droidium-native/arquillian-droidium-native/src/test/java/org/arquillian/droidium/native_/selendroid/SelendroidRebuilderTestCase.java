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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.io.FileReader;
import org.arquillian.spacelift.task.io.FileWriter;
import org.arquillian.spacelift.task.text.StringReplacementTool;
import org.junit.After;
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
public class SelendroidRebuilderTestCase {

    private File tempFile;

    @Before
    public void before() throws IOException {
        tempFile = File.createTempFile("droidium-test-", null);
    }

    @After
    public void after() throws Exception {
        if (tempFile != null && tempFile.exists()) {
            if (!tempFile.delete()) {
                throw new IllegalStateException("Unable to delete file: " + tempFile.getCanonicalPath());
            }
        }
    }

    @Test
    public void testReplacer() throws IOException {
        String content = "xyz package=io.selendroid blablabalb\n" +
            "android:targetPackage=\"io.selendroid.testapp\" />\n" +
            "android:icon=\"@drawable/selenium_icon\"\n";

        File tempFile = File.createTempFile("droidium-test-", null);

        Map<File, String> map = new HashMap<File, String>();
        map.put(tempFile, content);

        Spacelift.task(map, FileWriter.class).execute().await();

        Spacelift.task(StringReplacementTool.class).in(tempFile)
            .replace("package=io.selendroid").with("package=io.selendroid_1")
            .replace("io.selendroid.testapp").with("my.test.app")
            .replace("android:icon=\"@drawable/selenium_icon\"").with("")
            .execute().await();

        String replaced = Spacelift.task(Arrays.asList(tempFile), FileReader.class).execute().await().entrySet().iterator().next().getValue();

        Assert.assertTrue(replaced.contains("xyz package=io.selendroid_1 blablabalb"));
        Assert.assertTrue(replaced.contains("android:targetPackage=\"my.test.app\" />"));
        Assert.assertFalse(replaced.contains("icon"));
    }

}
