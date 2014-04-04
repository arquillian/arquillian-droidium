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
package org.arquillian.droidium.native_.exception;

/**
 * Thrown in case there are found two WebDriver instances for a simple class name. FQDN of classes can differ but their
 * respective simple name does not have to so we do not know what instance of WebDriver user wants to use.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class NotUniqueWebDriverInstanceException extends RuntimeException {

    private static final long serialVersionUID = 5366362067032226785L;

    public NotUniqueWebDriverInstanceException() {
        super();
    }

    /**
     * @param message
     */
    public NotUniqueWebDriverInstanceException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public NotUniqueWebDriverInstanceException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public NotUniqueWebDriverInstanceException(String message, Throwable cause) {
        super(message, cause);
    }

}
