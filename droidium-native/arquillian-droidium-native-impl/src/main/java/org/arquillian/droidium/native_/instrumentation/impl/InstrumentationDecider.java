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

import org.arquillian.droidium.native_.api.Instrumentable;
import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.arquillian.droidium.native_.spi.event.PerformInstrumentation;
import org.arquillian.droidium.native_.spi.event.RemoveInstrumentation;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Controls whether package to be deployed specified in {@link Deployment} method will be instrumented by Selendroid server or
 * not. All other deployments act as prerequisites for instrumented package in order to satisfy its dependencies.
 * Deployments to be instrumented are annotated by {@link Instrumentable}.
 *
 * <br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AfterDeploy}</li>
 * <li>{@link AfterUnDeploy}</li>
 * </ul>
 *
 * Fires: <br>
 * <br>
 * <ul>
 * <li>{@link PerformInstrumentation}</li>
 * <li>{@link RemoveInstrumentation}</li>
 * </ul>
 *
 * Usage Example:
 *
 * <pre>
 * <code>
 * &#64;Deployment(name = "androidApp1")
 * &#64;Instrumentable // this deployment will be instrumented
 * public static JavaArchive createDeployment1() {
 *      return ShrinkWrap.createFromZipFile(...);
 * }
 *
 * &#64;Deployment(name = "androidApp2") // this deployment will not be instrumented
 * public static JavaArchive createDeployment2() {
 *      return return ShrinkWrap.createFromZipFile(...);
 * }
 * </code>
 * </pre>
 *
 * @see Instrumentable
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class InstrumentationDecider {

    @Inject
    private Instance<Map<String, InstrumentationConfiguration>> instrumentation;

    @Inject
    private Event<PerformInstrumentation> performInstrumentationEvent;

    @Inject
    private Event<RemoveInstrumentation> removeInstrumentationEvent;

    /**
     * Decides if there is a need to perform the instrumentation on just installed Android application. Some Android
     * applications can act as resources or services to applications which are actually instrumented so they will not be
     * instrumented themselves.<br>
     * <br>
     * Observes:
     * <ul>
     * <li>{@link AfterDeploy}</li>
     * </ul>
     * Fires: <br>
     * <ul>
     * <li>{@link PerformInstrumentation}</li>
     * </ul>
     *
     * @param event
     */
    public void decidePerformingInstrumentation(@Observes AfterDeploy event) {
        String deploymentName = event.getDeployment().getName();
        if (instrumentation.get().containsKey(deploymentName)) {
            Archive<?> archive = event.getDeployment().getArchive();
            InstrumentationConfiguration configuration = instrumentation.get().get(deploymentName);
            performInstrumentationEvent.fire(new PerformInstrumentation(archive, configuration));
        }
    }

    /**
     * For just undeployed archive, decides if that archive was instrumented and cancel the instrumentation afterwards.<br>
     * <br>
     * Observes:
     * <ul>
     * <li>{@link AfterUnDeploy}</li>
     * </ul>
     * Fires: <br>
     * <ul>
     * <li>{@link RemoveInstrumentation}</li>
     * </ul>
     *
     * @param event
     */
    public void decideRemovingInstrumentation(@Observes AfterUnDeploy event) {
        String deploymentName = event.getDeployment().getName();
        if (instrumentation.get().containsKey(deploymentName)) {
            Archive<?> archive = event.getDeployment().getArchive();
            InstrumentationConfiguration configuration = instrumentation.get().get(deploymentName);
            removeInstrumentationEvent.fire(new RemoveInstrumentation(archive, configuration));
        }
    }
}
