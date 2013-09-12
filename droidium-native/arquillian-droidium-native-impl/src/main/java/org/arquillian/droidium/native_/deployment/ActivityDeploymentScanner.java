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

import org.arquillian.droidium.native_.android.AndroidApplicationHelper;
import org.arquillian.droidium.native_.instrumentation.DeploymentInstrumentationMapper;
import org.arquillian.droidium.native_.spi.event.AfterAndroidDeploymentDeployed;
import org.arquillian.droidium.native_.spi.event.AfterAndroidDeploymentScanned;
import org.arquillian.droidium.native_.spi.event.BeforeAndroidDeploymentScanned;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
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
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ActivityDeploymentScanner {

    @Inject
    private Instance<ActivityDeploymentMapper> activityDeploymentMapper;

    @Inject
    private Instance<DeploymentInstrumentationMapper> instrumentationMapper;

    @Inject
    private Instance<AndroidApplicationHelper> androidApplicationHelper;

    @Inject
    private Event<BeforeAndroidDeploymentScanned> beforeAndroidDeploymentScanned;

    @Inject
    private Event<AfterAndroidDeploymentScanned> afterAndroidDeploymentScanned;

    public void onAndroidDeploymentDeployed(@Observes AfterAndroidDeploymentDeployed event, DeploymentDescription description) {

        beforeAndroidDeploymentScanned.fire(new BeforeAndroidDeploymentScanned(event.getDeployed()));

        if (instrumentationMapper.get().getDeploymentName(description.getName()) != null) {
            List<String> activities = androidApplicationHelper.get().getActivities(event.getDeployed());

            ActivityDeploymentMapper activityDeploymentMapper = this.activityDeploymentMapper.get();

            for (String activity : activities) {
                activityDeploymentMapper.put(activity, description.getName());
            }

            afterAndroidDeploymentScanned.fire(new AfterAndroidDeploymentScanned(event.getDeployed()));
        }

    }
}
