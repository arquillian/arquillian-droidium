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
package org.arquillian.droidium.native_.deployment;

import java.util.List;

import org.arquillian.droidium.container.impl.AndroidApplicationHelper;
import org.arquillian.droidium.container.spi.AndroidDeployment;
import org.arquillian.droidium.container.spi.event.AfterAndroidDeploymentDeployed;
import org.arquillian.droidium.native_.api.Instrumentable;
import org.arquillian.droidium.native_.spi.event.AfterAndroidDeploymentScanned;
import org.arquillian.droidium.native_.spi.event.BeforeAndroidDeploymentScanned;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Scans installed Android packages to find all activities which can be instrumented.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AfterAndroidDeploymentDeployed}</li>
 * </ul>
 * Fires:<br>
 * <ul>
 * <li>{@link BeforeAndroidDeploymentScanned}</li>
 * <li>{@link AfterAndroidDeploymentScanned}</li>
 * </ul>
 *
 * Both events are fired only in case we are really going to perfrom the scan. Scan is performed only in case there is
 * {@link Instrumentable} annotation put on {@link Deployment} method.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DeploymentActivitiesScanner {

    @Inject
    private Instance<DeploymentActivitiesMapper> deploymentActivitiesMapper;

    @Inject
    private Instance<DeploymentInstrumentationMapper> deploymentInstrumentationMapper;

    @Inject
    private Instance<AndroidApplicationHelper> androidApplicationHelper;

    @Inject
    private Event<BeforeAndroidDeploymentScanned> beforeAndroidDeploymentScanned;

    @Inject
    private Event<AfterAndroidDeploymentScanned> afterAndroidDeploymentScanned;

    public void onAndroidDeploymentDeployed(@Observes AfterAndroidDeploymentDeployed event) {

        final AndroidDeployment deployment = event.getDeployment();

        if (deploymentInstrumentationMapper.get().getInstumentationConfiguration(deployment.getDeploymentName()) != null) {

            beforeAndroidDeploymentScanned.fire(new BeforeAndroidDeploymentScanned(deployment));

            List<String> activities = androidApplicationHelper.get().getActivities(deployment.getResignedApk());

            this.deploymentActivitiesMapper.get().put(deployment.getDeploymentName(), activities);

            afterAndroidDeploymentScanned.fire(new AfterAndroidDeploymentScanned(deployment));
        }

    }
}
