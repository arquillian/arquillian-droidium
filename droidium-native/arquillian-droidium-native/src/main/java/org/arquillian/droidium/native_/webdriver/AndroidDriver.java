/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.native_.webdriver;

import io.selendroid.client.SelendroidDriver;

import java.net.URL;

import org.jboss.arquillian.core.spi.Validate;
import org.openqa.selenium.Capabilities;

/**
 * Wrapper around {@link SelendroidDriver} to provide easy way how to control activities.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDriver extends SelendroidDriver {

    public AndroidDriver(URL url, Capabilities caps) throws Exception {
        super(url, caps);
    }

    public AndroidDriver(Capabilities caps) throws Exception {
        super(caps);
    }

    /**
     * Starts Android activity
     *
     * @param activity activity you want to start
     * @throws IllegalArgumentException if {@code activity} is a null object or an empty string
     */
    public void startActivity(String activity) {
        Validate.notNullOrEmpty(activity, "Activity you want to start can not be a null object nor an empty string!");
        get("and-activity://" + activity);
    }

    /**
     * Closes currently open activity
     */
    public void stopActivity() {
        close();
    }
}
