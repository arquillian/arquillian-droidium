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
package org.arquillian.droidium.native_.location;

import org.arquillian.droidium.native_.spi.location.DroidiumScheme;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.graphene.spi.location.LocationDecider;
import org.jboss.arquillian.graphene.spi.location.Scheme;

/**
 * Location decider which decides "and-activity://" scheme for starting Android activities.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumLocationDecider implements LocationDecider {

    private static final Scheme scheme = new DroidiumScheme();

    @Override
    public Scheme canDecide() {
        return scheme;
    }

    @Override
    public String decide(String location) {
        Validate.notNull(location, "Location to decide can not be a null object.");

        return new StringBuilder().append(scheme.toString()).append(location).toString();
    }

}
