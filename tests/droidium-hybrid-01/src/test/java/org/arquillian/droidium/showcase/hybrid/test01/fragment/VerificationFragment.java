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
import org.jboss.arquillian.graphene.Graphene;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Fragment which verifies a user.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class VerificationFragment {

    @FindBy(id = "label_name_data")
    private WebElement name;

    @FindBy(id = "label_username_data")
    private WebElement username;

    @FindBy(id = "label_password_data")
    private WebElement password;

    @FindBy(id = "label_email_data")
    private WebElement email;

    @FindBy(id = "label_preferedProgrammingLanguage_data")
    private WebElement language;

    @FindBy(id = "label_acceptAdds_data")
    private WebElement acceptAdds;

    @FindBy(id = "buttonRegisterUser")
    private WebElement registerButton;

    public void verifyUser(String username, String email, String password, String name, Language language, boolean acceptAdds) {
        Assert.assertEquals(this.username.getText(), username);
        Assert.assertEquals(this.email.getText(), email);
        Assert.assertEquals(this.password.getText(), password);
        Assert.assertEquals(this.name.getText(), name);
        Assert.assertEquals(this.language.getText(), language.toString());
        Assert.assertEquals(new Boolean(acceptAdds), Boolean.parseBoolean(this.acceptAdds.getText()));
    }

    public void registerUser() {
        registerButton.click();
        Graphene.waitGui().until().element(registerButton).is().not().visible();
    }

}
