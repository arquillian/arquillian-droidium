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
package org.arquillian.droidium.container.api;

import java.text.MessageFormat;

/**
 * A generic error during execution on the Android device
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class AndroidExecutionException extends RuntimeException {

    private static final long serialVersionUID = 5649861186106271426L;

    public AndroidExecutionException(Throwable cause) {
        super(cause);
    }

    public AndroidExecutionException() {
    }

    public AndroidExecutionException(String msg) {
        super(msg);
    }

    public AndroidExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AndroidExecutionException(String msgFormat, Object... arguments) {
        super(MessageFormat.format(msgFormat, arguments));
    }

    public AndroidExecutionException(Throwable cause, String msgFormat, Object... arguments) {
        super(MessageFormat.format(msgFormat, arguments), cause);
    }

}
