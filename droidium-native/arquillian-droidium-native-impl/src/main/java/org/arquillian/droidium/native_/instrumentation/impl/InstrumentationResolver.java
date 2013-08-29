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
package org.arquillian.droidium.native_.instrumentation.impl;

import java.util.Map;

import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * Observes:
 * <ul>
 * <li>{@link BeforeClass}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * @see InstrumentationMapper
 */
public class InstrumentationResolver {

    @Inject
    @ClassScoped
    private InstanceProducer<Map<String, InstrumentationConfiguration>> instrumentation;

    /**
     * Maps instrumentation configuration to deployment names from deployment methods.<br>
     *
     * @param context
     */
    public void resolveInstrumentation(@Observes EventContext<GenerateDeployment> context) {
        Map<String, InstrumentationConfiguration> resolvedInstrumentation = InstrumentationMapper.map(context.getEvent().getTestClass());
        instrumentation.set(resolvedInstrumentation);
        context.proceed();
    }

}
