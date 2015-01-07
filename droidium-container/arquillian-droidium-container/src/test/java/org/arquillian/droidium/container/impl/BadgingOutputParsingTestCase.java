package org.arquillian.droidium.container.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.util.Arrays;

import org.arquillian.droidium.container.impl.AndroidApplicationHelper.BadgingOutput;
import org.junit.Assert;
import org.junit.Test;

public class BadgingOutputParsingTestCase {

    private static final String SAMPLE_1 =
        "package: name='org.jboss.aerogear.todo' versionCode='1' versionName='1.0' platformBuildVersionName=''\n" +
            "sdkVersion:'10'\n" +
            "targetSdkVersion:'18'\n" +
            "uses-permission: name='android.permission.INTERNET'";

    @Test
    public void getSingleProperty() {
        BadgingOutput output = new BadgingOutput(Arrays.asList(SAMPLE_1.split("\n")));

        Assert.assertThat(output.getSingleProperty("package"), is(not("")));
        Assert.assertThat(output.getSingleProperty("sdkVersion"), is("'10'"));
    }

    @Test
    public void getSinglePropertySubProperty() {
        BadgingOutput output = new BadgingOutput(Arrays.asList(SAMPLE_1.split("\n")));

        Assert.assertThat(output.getSingleProperty("package", "name"), is("org.jboss.aerogear.todo"));
        Assert.assertThat(output.getSingleProperty("package", "versionName"), is("1.0"));
    }
}
