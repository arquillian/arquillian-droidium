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
import org.arquillian.droidium.showcase.hybrid.test01.utils.WaitingConditions;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Android Droidium hybrid testing with {@code WebDriver} - proof of concept.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SelendroidHybridTestAppTestCase {

    /**
     * @return deployment for Android device, the whole test application from Selendroid test is deployed without any change. We
     *         can deploy APK archive in spite of Shrinkwrap's disability to deal with such format since APK is internally just
     *         ZIP file anyway.
     */
    @Deployment(name = "android")
    @Instrumentable
    @TargetsContainer("android")
    public static Archive<?> createDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("selendroid-test-app-0.5.1.apk"));
    }

    private static final String USER_NAME = "john";

    private static final String USER_EMAIL = "john@doe.com";

    private static final String USER_PASSWORD = "p4ssw0rd";

    private static final String USER_REAL_NAME = "John Doe";

    private static final String USER_REAL_OLD = "Mr. Burns";

    private static final String USER_PRGRAMMING_LANGUAGE = "Scala";

    /**
     * Simple test which tries to register some user and verifies him - native view.
     */
    @Test
    @InSequence(1)
    @OperateOnDeployment("android")
    public void nativeViewTest(@ArquillianResource AndroidDevice android, @Drone WebDriver driver) {

        android.getActivityManagerProvider().getActivityManager().startActivity("io.selendroid.testapp.HomeScreenActivity");

        // show here just for completeness, native mode is default
        driver.switchTo().window("NATIVE_APP");

        registerUser(driver);
        verifyUser(driver);
    }

    /**
     * Simple test in web view mode
     */
    @Test
    @InSequence(2)
    @OperateOnDeployment("android")
    public void webViewTest(@ArquillianResource AndroidDevice android, @Drone WebDriver driver) {

        android.getActivityManagerProvider().getActivityManager().startActivity("io.selendroid.testapp.HomeScreenActivity");

        WebElement button = driver.findElement(By.id("buttonStartWebview"));
        button.click();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Go to home screen")));

        //
        // Switching into WEBVIEW - the whole point of this project
        //
        driver.switchTo().window("WEBVIEW");

        WebElement inputField = driver.findElement(By.id("name_input"));
        Assert.assertNotNull(inputField);
        inputField.clear();
        inputField.sendKeys("John Doe");

        WebElement car = driver.findElement(By.name("car"));
        Select preferedCar = new Select(car);
        preferedCar.selectByValue("audi");
        inputField.submit();

        WaitingConditions.pageTitleToBe(driver, "Hello: John Doe");
    }

    /**
     * Registers a user
     *
     * @param driver
     */
    private void registerUser(WebDriver driver) {
        // Go to user registration
        driver.findElement(By.id("startUserRegistration")).click();

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

        // register
        driver.findElement(By.id("btnRegisterUser")).click();
    }

    /**
     * Verifies that user is registered
     *
     * @param driver
     */
    private void verifyUser(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 5);
        WebElement inputUserName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("label_username_data")));

        Assert.assertEquals(inputUserName.getText(), USER_NAME);
        Assert.assertEquals(driver.findElement(By.id("label_email_data")).getText(), USER_EMAIL);
        Assert.assertEquals(driver.findElement(By.id("label_password_data")).getText(), USER_PASSWORD);
        Assert.assertEquals(driver.findElement(By.id("label_name_data")).getText(), USER_REAL_NAME);
        Assert.assertEquals(driver.findElement(By.id("label_preferedProgrammingLanguage_data")).getText(),
            USER_PRGRAMMING_LANGUAGE);
        Assert.assertEquals(driver.findElement(By.id("label_acceptAdds_data")).getText(), "true");

        driver.findElement(By.id("buttonRegisterUser")).click();
    }
}
