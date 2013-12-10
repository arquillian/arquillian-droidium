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
package org.arquillian.droidium.container.execution;

import java.text.MessageFormat;

import org.arquillian.droidium.container.api.AndroidExecutionException;

/**
 * Exception thrown in case that process execution failed
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class ProcessExecutionException extends AndroidExecutionException {

    private static final long serialVersionUID = 1178535932055786525L;

    public ProcessExecutionException() {
        super();
    }

    public ProcessExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessExecutionException(String message) {
        super(message);
    }

    public ProcessExecutionException(String messageFormat, Object... parameters) {
        super(MessageFormat.format(messageFormat, parameters));
    }

    public ProcessExecutionException(Throwable cause, String messageFormat, Object... parameters) {
        super(MessageFormat.format(messageFormat, parameters), cause);
    }

}
