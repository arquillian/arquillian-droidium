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
package org.arquillian.droidium.native_.spi.location;

import org.jboss.arquillian.graphene.spi.location.Scheme;

/**
 * Droidium scheme to put on Location annotation. It automatically opens Android activity given as a value to that annotation.
 * Activity is the same thing as the abstraction of page objects from Graphene.
 *
 * Usage:
 *
 * <pre>
 * &#064;Location(scheme = DroidiumScheme.class, value = &quot;this.is.my.activity&quot;)
 * public class MobileActivity
 * </pre>
 *
 * and use it like this
 *
 * <pre>
 * &#064;Test
 * public void mobileTest(&#064;InitialPage MobileActivity activity)
 * </pre>
 *
 * that activity will be automatically started for you.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumScheme extends Scheme {

    private static final String scheme = "and-activity://";

    @Override
    public String toString() {
        return scheme;
    }
}
