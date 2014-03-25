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
package org.arquillian.droidium.showcase.hybrid.test01;

import java.io.File;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.native_.api.Instrumentable;
import org.arquillian.droidium.showcase.hybrid.test01.fragment.HomeScreenFragment;
import org.arquillian.droidium.showcase.hybrid.test01.fragment.RegistrationFragment;
import org.arquillian.droidium.showcase.hybrid.test01.fragment.VerificationFragment;
import org.arquillian.droidium.showcase.hybrid.test01.utils.Language;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

/**
 * Android Droidium hybrid testing with {@code WebDriver} - proof of concept.
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SelendroidHybridTestAppTestCase {

    @ArquillianResource
    private AndroidDevice android;

    @Drone
    private WebDriver driver;

    @Deployment
    @Instrumentable
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class,
            new File("selendroid-test-app-" + System.getProperty("selendroid.version", "0.9.0") + ".apk"));
    }

    private static final String USER_NAME = "john";

    private static final String USER_EMAIL = "john@doe.com";

    private static final String USER_PASSWORD = "p4ssw0rd";

    private static final String USER_REAL_NAME = "John Doe";

    private static final Language LANGUAGE = Language.SCALA;

    private static final boolean ACCEPT_ADDS = true;

    private static final String CAR = "audi";

    @FindBy(id = "content")
    private WebElement content;

    @FindBy(id = "content")
    private HomeScreenFragment homeFragment;

    @FindBy(id = "content")
    private RegistrationFragment registrationFragment;

    @FindBy(id = "content")
    private VerificationFragment verificationFragment;

    @Test
    @InSequence(1)
    public void nativeViewTest() {

        android.getActivityManagerProvider().getActivityManager().startActivity("io.selendroid.testapp.HomeScreenActivity");

        driver.switchTo().window("NATIVE_APP");

        homeFragment.startUserRegistration();
        registrationFragment.registerUser(USER_NAME, USER_EMAIL, USER_PASSWORD, USER_REAL_NAME, LANGUAGE, ACCEPT_ADDS);
        verificationFragment.verifyUser(USER_NAME, USER_EMAIL, USER_PASSWORD, USER_REAL_NAME, LANGUAGE, ACCEPT_ADDS);
        verificationFragment.registerUser();
    }

    @Test
    @InSequence(2)
    @Ignore
    public void webViewTest() {
        homeFragment.startWebView();

        driver.switchTo().window("WEBVIEW");

        WebElement inputField = driver.findElement(By.id("name_input"));
        Assert.assertNotNull(inputField);
        inputField.clear();
        inputField.sendKeys(USER_REAL_NAME);

        WebElement car = driver.findElement(By.name("car"));
        Select preferedCar = new Select(car);
        preferedCar.selectByValue(CAR);
        inputField.submit();
    }
}
