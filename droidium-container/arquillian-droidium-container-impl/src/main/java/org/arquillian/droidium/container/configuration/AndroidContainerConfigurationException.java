/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import org.jboss.arquillian.container.spi.ConfigurationException;

/**
 * Exception thrown when configuration of Android container went wrong.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class AndroidContainerConfigurationException extends ConfigurationException {

    private static final long serialVersionUID = -2363543684908024418L;

    /**
     * @param message message to be printed out
     */
    public AndroidContainerConfigurationException(String message) {
        super(message);
    }

    /**
     * @param cause cause of the exception
     */
    public AndroidContainerConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message message to be printed out
     * @param cause cause of the exception
     */
    public AndroidContainerConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
