/*
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
package org.arquillian.droidium.container.configuration.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class TargetRegistry {

    private final List<Target> targets = new ArrayList<Target>();

    public void addTarget(Target target) {
        if (target == null) {
            return;
        }

        targets.add(target);
    }

    public void addTargets(List<Target> targets) {
        for (Target target : targets) {
            addTarget(target);
        }
    }

    /**
     *
     * @return unmodifiable list of targets
     */
    public List<Target> getTargets() {
        return Collections.unmodifiableList(targets);
    }

    public void clear() {
        targets.clear();
    }

    public List<Target> getTargetsWithNoAbis() {
        List<Target> targets = new ArrayList<Target>();

        for (Target target : this.targets) {
            if (target.numberOfTagAbis() == 0) {
                targets.add(target);
            }
        }

        return targets;
    }

    public List<Target> getByTag(TAG tag) {
        List<Target> targets = new ArrayList<Target>();

        for (Target target : this.targets) {
            if (target.hasTag(tag)) {
                targets.add(target);
            }
        }

        return targets;
    }

    public List<Target> getByABI(ABI abi) {
        List<Target> targets = new ArrayList<Target>();

        for (Target target : this.targets) {
            if (target.hasAbi(abi)) {
                targets.add(target);
            }
        }

        return targets;
    }

    public List<Target> getByPair(TagAbiPair tagAbiPair) {
        List<Target> targets = new ArrayList<Target>();

        for (Target target : this.targets) {
            if (target.hasTagAbi(tagAbiPair)) {
                targets.add(target);
            }
        }

        return targets;
    }

    public List<Target> getByPair(TAG tag, ABI abi) {
        return getByPair(new TagAbiPair(tag, abi));
    }

    public Target getByIdLabel(String idLabel) {
        if (idLabel == null || idLabel.isEmpty()) {
            return null;
        }

        for (Target target : targets) {
            if (target.getIdLabel().equals(idLabel)) {
                return target;
            }
        }

        return null;
    }

    public List<Target> getByApiLevel(int apiLevel) {

        List<Target> foundTargets = new ArrayList<Target>();

        for (Target target : targets) {
            if (target.getApiLevel() == apiLevel) {
                foundTargets.add(target);
            }
        }

        return foundTargets;
    }

    public List<Target> getByApiLevel(String apiLevel) {
        return getByApiLevel(Integer.parseInt(apiLevel));
    }

    public List<Target> getAddOns() {
        List<Target> addons = new ArrayList<Target>();

        for (Target target : targets) {
            if (target.isAddOn()) {
                addons.add(target);
            }
        }

        return addons;
    }

    public List<Target> getPlatforms() {
        List<Target> platforms = new ArrayList<Target>();

        for (Target target : targets) {
            if (target.isPlatorm()) {
                platforms.add(target);
            }
        }

        return platforms;
    }

    public List<Target> getByTargetType(TARGET_TYPE targetType) {
        List<Target> targets = new ArrayList<Target>();

        for (Target target : this.targets) {
            if (target.getTargetType().equals(targetType)) {
                targets.add(target);
            }
        }

        return targets;
    }

    public Target getHighest(TARGET_TYPE targetType) {

        List<Target> targets = getByTargetType(targetType);

        Collections.sort(targets);

        if (targets.isEmpty()) {
            return null;
        }

        return targets.get(targets.size() - 1);
    }

    public Target getLowest(TARGET_TYPE targetType) {

        List<Target> targets = getByTargetType(targetType);

        Collections.sort(targets);

        if (targets.isEmpty()) {
            return null;
        }

        return targets.get(0);
    }

}
