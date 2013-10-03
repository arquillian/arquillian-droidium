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
package org.arquillian.droidium.container.impl;

import java.util.logging.Logger;

import org.arquillian.droidium.container.api.ActivityManager;
import org.arquillian.droidium.container.api.ActivityManagerProvider;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DefaultActivityManagerProvider implements ActivityManagerProvider {

    private static final Logger logger = Logger.getLogger(DefaultActivityManagerProvider.class.getName());

    private ActivityManager activityManager;

    @Override
    public ActivityManager getActivityManager() {
        if (activityManager == null) {
            logger.info("You are using default implementation of activity manager provider and you have not set any "
                + "activity manager for it so far. Please implement your own or put Droidium native plugin on the classpath.");
        }
        return this.activityManager;
    }

    @Override
    public void setActivityManager(ActivityManager activityManager) {
        this.activityManager = activityManager;
    }

}
