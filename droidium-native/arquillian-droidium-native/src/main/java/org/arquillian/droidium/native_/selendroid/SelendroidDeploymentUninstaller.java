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
package org.arquillian.droidium.native_.selendroid;

import org.arquillian.droidium.native_.spi.event.AfterSelendroidDeploymentUndeployed;
import org.arquillian.droidium.native_.spi.event.BeforeSelendroidDeploymentUndeployed;
import org.arquillian.droidium.native_.spi.event.SelendroidUndeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Uninstalls Selendroid server from Android device.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link SelendroidUndeploy}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link BeforeSelendroidDeploymentUndeployed}</li>
 * <li>{@link AfterSelendroidDeploymentUndeployed}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidDeploymentUninstaller {

    @Inject
    private Instance<SelendroidServerManager> selendroidServerManager;

    @Inject
    private Event<BeforeSelendroidDeploymentUndeployed> beforeSelendroidUndeployed;

    @Inject
    private Event<AfterSelendroidDeploymentUndeployed> afterSelendroidUndeployed;

    public void onSelendroidUndeploy(@Observes SelendroidUndeploy event) {
        beforeSelendroidUndeployed.fire(new BeforeSelendroidDeploymentUndeployed(event.getDeployment().getResigned()));

        selendroidServerManager.get().uninstall(event.getDeployment());

        afterSelendroidUndeployed.fire(new AfterSelendroidDeploymentUndeployed(event.getDeployment().getResigned()));
    }
}
