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
package org.arquillian.droidium.showcase.hybrid.test01.fragment;

import org.arquillian.droidium.showcase.hybrid.test01.utils.Language;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.android.AndroidDriver;
import org.openqa.selenium.support.FindBy;

/**
 * Abstraction of registration activity to register a user.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class RegistrationFragment {

    @Drone
    private AndroidDriver driver;

    @FindBy(id = "inputUserName")
    private WebElement inputUserName;

    @FindBy(id = "inputEmail")
    private WebElement inputEmail;

    @FindBy(id = "inputPassword")
    private WebElement inputPassword;

    @FindBy(id = "inputName")
    private WebElement inputName;

    @FindBy(id = "input_preferedProgrammingLanguage")
    private WebElement language;

    @FindBy(id = "input_adds")
    private WebElement adds;

    @FindBy(id = "btnRegisterUser")
    private WebElement registerButton;

    public void registerUser(String username, String email, String password, String name, Language language, boolean acceptAdds) {
        inputUserName.click();
        inputUserName.sendKeys(username);

        inputEmail.click();
        inputEmail.sendKeys(email);

        inputPassword.click();
        inputPassword.sendKeys(password);

        inputName.click();
        inputName.clear();
        inputName.sendKeys(name);

        this.language.click();
        WebElement lang = driver.findElement(By.linkText(language.toString()));
        lang.click();

        if (acceptAdds) {
            adds.click();
        }

        registerButton.click();

        Graphene.waitGui().until().element(registerButton).is().not().visible();
    }

}
