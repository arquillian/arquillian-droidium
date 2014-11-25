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
public class Target implements Comparable<Target> {

    private int id = 0;

    private String idLabel;

    private int apiLevel;

    private String name;

    private TARGET_TYPE targetType;

    private String vendor;

    private int revision;

    private String description;

    private List<String> skins = new ArrayList<String>();

    private List<TagAbiPair> tagAbiPairs = new ArrayList<TagAbiPair>();

    public Target() {

    }

    public Target(TAG tag, ABI abi) {
        addTagAbi(tag, abi);
    }

    public void setId(int id) {
        if (id > 0) {
            this.id = id;
        }
    }

    public int getId() {
        return id;
    }

    public void setIdLabel(String idLabel) {
        if (idLabel != null && !idLabel.isEmpty()) {
            this.idLabel = idLabel;
        }
    }

    public String getIdLabel() {
        return idLabel;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(int apiLevel) {
        if (apiLevel > 0) {
            this.apiLevel = apiLevel;
        }
    }

    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    public String getName() {
        return name;
    }

    public void setTargetType(TARGET_TYPE targetType) {
        if (targetType != null) {
            this.targetType = targetType;
        }
    }

    public TARGET_TYPE getTargetType() {
        return targetType;
    }

    public boolean isAddOn() {
        return TARGET_TYPE.ADD_ON.equals(targetType);
    }

    public boolean isPlatorm() {
        return TARGET_TYPE.PLATFORM.equals(targetType);
    }

    public void setVendor(String vendor) {
        if (vendor != null && !vendor.isEmpty()) {
            this.vendor = vendor;
        }
    }

    public String getVendor() {
        return vendor;
    }

    public void setRevision(int revision) {
        if (revision > 0) {
            this.revision = revision;
        }
    }

    public int getRevision() {
        return revision;
    }

    public void setDescription(String description) {
        if (description != null && !description.isEmpty()) {
            this.description = description;
        }
    }

    public String getDescription() {
        return description;
    }

    public void addSkin(String skin) {
        if (skin != null && !skin.isEmpty()) {
            if (!this.skins.contains(skin)) {
                this.skins.add(skin);
            }
        }
    }

    public void addSkins(List<String> skins) {
        if (skins != null) {
            for (String skin : skins) {
                addSkin(skin);
            }
        }
    }

    /**
     *
     * @return unmodifiable list of skins
     */
    public List<String> getSkins() {
        return Collections.unmodifiableList(skins);
    }

    public boolean hasSkin(String skin) {
        if (skin == null || skin.isEmpty()) {
            return false;
        }
        return skins.contains(skin);
    }

    /**
     *
     * @return unmodifiable list of abis
     */
    public List<TagAbiPair> getAbis() {
        return Collections.unmodifiableList(tagAbiPairs);
    }

    /**
     *
     * @return default skin as String or null if default skin is not found
     */
    public String getDefaultSkin() {
        for (String skin : skins) {
            if (skin.contains("(default)")) {
                return skin.split(" ")[0];
            }
        }

        return null;
    }

    public void addTagAbi(String tagAbi) {
        addTagAbi(TagAbiPair.construct(tagAbi));
    }

    public void addTagAbi(String tag, String abi) {
        addTagAbi(TAG.match(tag), ABI.match(abi));
    }

    public void addTagAbi(TAG tag, ABI abi) {
        if (tag == null || abi == null) {
            return;
        }
        addTagAbi(new TagAbiPair(tag, abi));
    }

    public void addTagAbi(TagAbiPair abi) {
        if (abi != null) {
            tagAbiPairs.add(abi);
        }
    }

    public void addTagAbis(List<TagAbiPair> abis) {
        if (abis != null) {
            for (TagAbiPair abi : abis) {
                addTagAbi(abi);
            }
        }
    }

    public boolean hasTagAbis() {
        return !tagAbiPairs.isEmpty();
    }

    public boolean hasAbi(ABI abi) {
        if (abi == null) {
            return false;
        }

        for (TagAbiPair tagAbiPair : tagAbiPairs) {
            if (tagAbiPair.getAbi().equals(abi)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasTag(TAG tag) {
        if (tag == null) {
            return false;
        }

        for (TagAbiPair tagAbiPair : tagAbiPairs) {
            if (tagAbiPair.getTag().equals(tag)) {
                return true;
            }
        }

        return false;
    }

    public List<TagAbiPair> getTagAbisForTag(TAG tag) {

        List<TagAbiPair> abis = new ArrayList<TagAbiPair>();

        if (tag != null) {
            for (TagAbiPair tagAbiPair : this.tagAbiPairs) {
                if (tagAbiPair.getTag().equals(tag)) {
                    abis.add(tagAbiPair);
                }
            }
        }

        return abis;
    }

    public List<TagAbiPair> getTagAbisForAbi(ABI abi) {
        List<TagAbiPair> abis = new ArrayList<TagAbiPair>();

        if (abi != null) {
            for (TagAbiPair tagAbiPair : this.tagAbiPairs) {
                if (tagAbiPair.getAbi().equals(abi)) {
                    abis.add(tagAbiPair);
                }
            }
        }

        return abis;
    }

    public boolean hasTagAbi(TAG tag, ABI abi) {
        return hasTagAbi(new TagAbiPair(tag, abi));
    }

    public boolean hasTagAbi(TagAbiPair tagAbiPair) {
        if (tagAbiPair == null) {
            return false;
        }

        for (TagAbiPair tap : tagAbiPairs) {
            if (tagAbiPair.equals(tap)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasTagAbi(String tag, String abi) {
        return hasTagAbi(TAG.match(tag), ABI.match(abi));
    }

    public boolean hasTagAbi(String tagAbi) {
        return hasTagAbi(TagAbiPair.construct(tagAbi));
    }

    public List<TagAbiPair> getDefaultAbis() {

        List<TagAbiPair> abis = new ArrayList<TagAbiPair>();

        for (TagAbiPair abi : this.tagAbiPairs) {
            if (abi.getTag().equals(TAG.DEFAULT)) {
                abis.add(abi);
            }
        }

        return abis;
    }

    public int numberOfTagAbis() {
        return tagAbiPairs.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Target [id = ").append(id)
            .append(", idLabel= " + idLabel)
            .append(", apiLevel=").append(apiLevel)
            .append(", name=").append(name)
            .append(", revision=").append(revision);

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + apiLevel;
        result = prime * result + id;
        result = prime * result + ((idLabel == null) ? 0 : idLabel.hashCode());
        result = prime * result + revision;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Target other = (Target) obj;
        if (apiLevel != other.apiLevel)
            return false;
        if (id != other.id)
            return false;
        if (idLabel == null) {
            if (other.idLabel != null)
                return false;
        } else if (!idLabel.equals(other.idLabel))
            return false;
        if (revision != other.revision)
            return false;
        return true;
    }

    @Override
    public int compareTo(Target o) {
        if (o == null) {
            return 1;
        }
        return getId() - o.getId();
    }

}
