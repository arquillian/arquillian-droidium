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

import io.selendroid.SelendroidCapabilities;

import java.util.Map;

import org.jboss.arquillian.drone.webdriver.spi.BrowserCapabilities;

/**
 * Capabilities for {@link AndroidDriver}.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidBrowserCapabilities implements BrowserCapabilities {

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public String getImplementationClassName() {
        return "org.arquillian.droidium.native_.webdriver.AndroidDriver";
    }

    @Override
    public Map<String, ?> getRawCapabilities() {
        return new SelendroidCapabilities().getRawCapabilities();
    }

    @Override
    public String getReadableName() {
        return "android";
    }

}