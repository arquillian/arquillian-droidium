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
package org.arquillian.droidium.native_.instrumentation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.native_.api.Instrumentable;
import org.arquillian.droidium.native_.event.InstrumentPackage;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Controls whether package to be deployed specified in {@link Deployment} method will be instrumented by Selendroid server or
 * not. This class provides a way how to decide if package will be just installed or whether it is instrumented by Selendroid
 * server. By doing this, it is possible to deploy multiple packages to target {@link AndroidDevice} but only one of them will
 * be instrumented. All other deployments act as prerequisites for instrumented package in order to satisfy its dependencies.
 *
 * <br>
 * Observes:
 * <ul>
 * <li>{@link BeforeClass}</li>
 * <li>{@link AfterDeploy}</li>
 * </ul>
 *
 * Fires: <br>
 * <br>
 * <ul>
 * <li>{@link InstrumentPackage}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumInstrumentationController {

    private static final Logger logger = Logger.getLogger(DroidiumInstrumentationController.class.getName());

    @Inject
    private Event<InstrumentPackage> instrumentEvent;

    private String instrumentedDeploymentName;

    /**
     * Resolves a name of {@code @Deployment} which is going to be instrumented. Instrumentation of an archive occurs if
     * {@code @Deployment} method is annotated with {@code @Instrumentable}. In case of multiple deployments, there has to be
     * one and only one {@code @Instrumentable} deployment. In case of one {@code @Deployment}, this deployment will be
     * instrumented regardless of the presence of {@code @Instrumentable} annotation.
     *
     * @param context
     */
    public void resolveInstrumentedDeploymentName(@Observes EventContext<BeforeClass> context) {

        Method[] deploymentMethods = context.getEvent().getTestClass().getMethods(Deployment.class);

        if (deploymentMethods.length == 0) {
            throw new IllegalStateException("There are not any methods annotated by " + Deployment.class.getName() + ".");
        }

        instrumentedDeploymentName = getInstrumentedDeploymentName(deploymentMethods);

        logger.info("Deployment going to be instrumented: " + instrumentedDeploymentName);

        context.proceed();
    }

    /**
     * Decides if instrumentation of just deployed archive is going to happen or not. If yes, {@code InstrumentPackage} event is
     * fired with underlying deployed archive. Deployed archive is instrumented if the name of {@code @Deployment} is the same
     * as resolved name in {@code #resolveInstrumentedDeploymentName(EventContext)}.
     *
     * @param event
     * @param deploymentScenario
     */
    public void decideInstrumentation(@Observes AfterDeploy event, DeploymentScenario deploymentScenario) {

        Archive<?> archive = null;

        for (org.jboss.arquillian.container.spi.client.deployment.Deployment deployment : deploymentScenario.deployments()) {
            if (deployment.getDescription().getName().equals(instrumentedDeploymentName)) {
                archive = deployment.getDescription().getArchive();
                break;
            }
        }

        if (archive != null) {
            instrumentEvent.fire(new InstrumentPackage(archive));
        }
    }

    private String getInstrumentedDeploymentName(Method[] methods) {
        if (methods.length == 1) {
            return methods[0].getAnnotation(Deployment.class).name();
        }

        List<Method> instrumentableMethods = getInstrumentableMethods(methods);

        if (instrumentableMethods.size() != 1) {
            throw new IllegalStateException("There is more then one @Deployment method but you either specified "
                + "no @Instrumentable method or you specified more then one. You can put @Instrumentable only on one "
                + "@Deployment method. There has to be one instrumented deployment at maximum.");
        }

        return instrumentableMethods.get(0).getAnnotation(Deployment.class).name();
    }

    private List<Method> getInstrumentableMethods(Method[] methods) {
        List<Method> instrumentableMethods = new ArrayList<Method>();

        for (Method method : methods) {
            if (method.getAnnotation(Instrumentable.class) != null) {
                instrumentableMethods.add(method);
            }
        }
        return instrumentableMethods;
    }

}
