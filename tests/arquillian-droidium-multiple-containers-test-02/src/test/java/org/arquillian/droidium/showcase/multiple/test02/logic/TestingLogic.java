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
package org.arquillian.droidium.showcase.multiple.test02.logic;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Provides central logic of testing. It is device agnostic since WebDriver instance is put into
 * {@code TestingLogic#execute(WebDriver)} from outside (from test itself).
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
public class TestingLogic {

    public void execute(WebDriver driver) {
        // Go to user registration
        driver.findElement(By.id("startUserRegistration")).click();

        // enter nick
        WebElement userName = driver.findElement(By.id("inputUserName"));
        userName.sendKeys(TestingBits.USER_NAME);
        Assert.assertEquals(userName.getText(), TestingBits.USER_NAME);

        // enter e-mail
        WebElement inputEmail = driver.findElement(By.id("inputEmail"));
        inputEmail.sendKeys(TestingBits.USER_EMAIL);
        Assert.assertEquals(inputEmail.getText(), TestingBits.USER_EMAIL);

        // enter password
        WebElement inputPassword = driver.findElement(By.id("inputPassword"));
        inputPassword.sendKeys(TestingBits.USER_PASSWORD);
        Assert.assertEquals(inputPassword.getText(), TestingBits.USER_PASSWORD);

        // check value in name field, clear it and write new one
        WebElement inputName = driver.findElement(By.id("inputName"));
        Assert.assertEquals(inputName.getText(), TestingBits.USER_REAL_OLD);
        inputName.clear();
        inputName.sendKeys(TestingBits.USER_REAL_NAME);
        Assert.assertEquals(inputName.getText(), TestingBits.USER_REAL_NAME);

        // enter favorite language
        driver.findElement(By.id("input_preferedProgrammingLanguage")).click();
        driver.findElement(By.linkText(TestingBits.USER_PRGRAMMING_LANGUAGE)).click();

        // accept adds checkbox
        WebElement acceptAddsCheckbox = driver.findElement(By.id("input_adds"));
        Assert.assertEquals(acceptAddsCheckbox.isSelected(), false);
        acceptAddsCheckbox.click();

        // register
        driver.findElement(By.id("btnRegisterUser")).click();
    }
}
