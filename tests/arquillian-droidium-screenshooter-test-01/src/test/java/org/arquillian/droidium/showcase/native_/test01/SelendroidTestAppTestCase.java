/**
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
package org.arquillian.droidium.showcase.native_.test01;

import java.io.File;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.ScreenshotType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Android Droidium testing with WebDriver and Selendroid server- showing of taking screenshots.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SelendroidTestAppTestCase {

    /**
     * @return deployment for Android device, the whole test application from Selendroid test is deployed without any change. We
     *         can deploy APK archive in spite of Shrinkwrap's disability to deal with such format since APK is internally just
     *         ZIP file anyway.
     */
    @Deployment(name = "android")
    @TargetsContainer("android")
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("selendroid-test-app-0.4.2.apk"));
    }

    private static final String USER_NAME = "john";

    private static final String USER_EMAIL = "john@doe.com";

    private static final String USER_PASSWORD = "p4ssw0rd";

    private static final String USER_REAL_NAME = "John Doe";

    private static final String USER_REAL_OLD = "Mr. Burns";

    private static final String USER_PRGRAMMING_LANGUAGE = "Scala";

    /**
     * Simple test which tries to register some user.
     *
     * @param android Android device itself, it is not needed in tests as such since we interact only with {@code WebDriver}
     *        injection.
     * @param driver {@code WebDriver} injection which sends commands to Selendroid server installed on the Android device.
     */
    @Test
    @InSequence(1)
    @OperateOnDeployment("android")
    public void test01(@ArquillianResource AndroidDevice android, @Drone WebDriver driver) {

        // where to save taken screenshots, by default to target/
        android.setScreenshotTargetDir("target/screenshots-1");

        // take it!
        android.takeScreenshot();

        // Go to user registration
        driver.findElement(By.id("startUserRegistration")).click();

        // take it as GIF, by default, it is taken as PNG
        android.takeScreenshot(ScreenshotType.GIF);

        // enter nick
        WebElement userName = driver.findElement(By.id("inputUserName"));
        userName.sendKeys(USER_NAME);
        Assert.assertEquals(userName.getText(), USER_NAME);

        // enter e-mail
        WebElement inputEmail = driver.findElement(By.id("inputEmail"));
        inputEmail.sendKeys(USER_EMAIL);
        Assert.assertEquals(inputEmail.getText(), USER_EMAIL);

        // enter password
        WebElement inputPassword = driver.findElement(By.id("inputPassword"));
        inputPassword.sendKeys(USER_PASSWORD);
        Assert.assertEquals(inputPassword.getText(), USER_PASSWORD);

        // check value in name field, clear it and write new one
        WebElement inputName = driver.findElement(By.id("inputName"));
        Assert.assertEquals(inputName.getText(), USER_REAL_OLD);
        inputName.clear();
        inputName.sendKeys(USER_REAL_NAME);
        Assert.assertEquals(inputName.getText(), USER_REAL_NAME);

        // enter favorite language
        driver.findElement(By.id("input_preferedProgrammingLanguage")).click();
        driver.findElement(By.linkText(USER_PRGRAMMING_LANGUAGE)).click();

        // accept adds checkbox
        WebElement acceptAddsCheckbox = driver.findElement(By.id("input_adds"));
        Assert.assertEquals(acceptAddsCheckbox.isSelected(), false);

        acceptAddsCheckbox.click();

        android.setScreenshotTargetDir("target/screenshots-2");

        // you can name it, it will be PNG image by default
        android.takeScreenshot("myscreenshot1");

        // register
        driver.findElement(By.id("btnRegisterUser")).click();

        // or you can specify file format as well
        android.takeScreenshot("myscreenshot2", ScreenshotType.JPEG);

    }
}
