/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

/**
 * Tests parsing of Android Target
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class TargetRegexTest {

    @Test
    public void androidLevelPattern() {
        Matcher m = Target.ANDROID_LEVEL_PATTERN.matcher("16");
        Assert.assertThat(m.matches(), is(true));

        int apiLevel = Integer.parseInt(m.group());
        Assert.assertThat(apiLevel, is(16));
    }

    @Test
    public void androidPattern() {
        Matcher m = Target.ANDROID_PATTERN.matcher("android-16");
        Assert.assertThat(m.matches(), is(true));

        int apiLevel = Integer.parseInt(m.group(1));
        Assert.assertThat(apiLevel, is(16));
    }

    @Test
    public void googleAndroidPattern() {
        Matcher m = Target.GOOGLE_ADDON_PATTERN.matcher("Google Inc.:Google APIs:16");
        Assert.assertThat(m.matches(), is(true));

        String type = m.group(1);
        Assert.assertThat(type, is("Google APIs"));

        int apiLevel = Integer.parseInt(m.group(2));
        Assert.assertThat(apiLevel, is(16));
    }

    @Test
    public void googleAndroidPatternX86() {
        Matcher m = Target.GOOGLE_ADDON_PATTERN.matcher("Google Inc.:Google APIs x86:19");
        Assert.assertThat(m.matches(), is(true));

        String type = m.group(1);
        Assert.assertThat(type, is("Google APIs x86"));

        int apiLevel = Integer.parseInt(m.group(2));
        Assert.assertThat(apiLevel, is(19));
    }

}
