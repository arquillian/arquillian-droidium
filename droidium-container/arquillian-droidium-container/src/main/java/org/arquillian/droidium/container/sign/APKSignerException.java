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
package org.arquillian.droidium.container.sign;

/**
 * Thrown when there is a failure while signing an APK package.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class APKSignerException extends RuntimeException {

    private static final long serialVersionUID = 5135480339532884887L;

    public APKSignerException() {
        super();
    }

    /**
     * @param message
     */
    public APKSignerException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public APKSignerException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public APKSignerException(String message, Throwable cause) {
        super(message, cause);
    }

}
