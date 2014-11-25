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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the output of {@code android list target} for given target snippet.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class TargetBuilder {

    private static final Pattern PATTERN_ID_LABEL = Pattern.compile("id: ([0-9]+) or \"(.*)\"");
    // something like "Google Inc.:Google APIs (x86 System Image):19"
    private static final Pattern PATTERN_ID_LABEL_GOOGLE = Pattern.compile("(.*):(.*):([0-9]*)");
    private static final Pattern PATTERN_NAME = Pattern.compile("Name: (.*)");
    private static final Pattern PATTERN_TYPE = Pattern.compile("Type: (.*)");
    private static final Pattern PATTERN_VENDOR = Pattern.compile("Vendor: (.*)");
    private static final Pattern PATTERN_REVISION = Pattern.compile("Revision: (\\d+)");
    private static final Pattern PATTERN_API_LEVEL = Pattern.compile("API level: (\\d+)");
    private static final Pattern PATTERN_DESCRIPTION = Pattern.compile("Description: (.*)");
    private static final Pattern PATTERN_SKINS_LABEL = Pattern.compile("Skins: (.*)");
    private static final Pattern PATTERN_COMMA_LIST = Pattern.compile("([^,]+)");
    private static final Pattern PATTERN_TAG_ABIS = Pattern.compile("Tag/ABIs : (.*)");

    public static Target build(List<String> targetLines) {
        Target target = new Target();

        for (String line : targetLines) {
            if (line.startsWith("id:")) {
                setId(target, line);
            } else if (line.startsWith("Name: ")) {
                setName(target, line);
            } else if (line.startsWith("Type: ")) {
                setType(target, line);
            } else if (line.startsWith("Vendor: ")) {
                setVendor(target, line);
            } else if (line.startsWith("API level: ")) {
                setApiLevel(target, line);
            } else if (line.startsWith("Revision: ")) {
                setRevision(target, line);
            } else if (line.startsWith("Description: ")) {
                setDescription(target, line);
            } else if (line.startsWith("Skins: ")) {
                setSkins(target, line);
            } else if (line.startsWith("Tag/ABIs")) {
                setAbis(target, line);
            }
        }

        return target;
    }

    private static void setAbis(Target target, String line) {

        if (line.contains("no ABIs.")) {
            return;
        }

        Matcher tagAbisMatcher = PATTERN_TAG_ABIS.matcher(line);

        if (tagAbisMatcher.find()) {
            Matcher m = PATTERN_COMMA_LIST.matcher(tagAbisMatcher.group(1));

            List<TagAbiPair> pairs = new ArrayList<TagAbiPair>();

            while (m.find()) {
                pairs.add(TagAbiPair.construct(m.group(1).trim()));
            }

            Collections.sort(pairs);

            target.addTagAbis(pairs);
        }
    }

    private static void setSkins(Target target, String line) {
        Matcher skinsMatcher = PATTERN_SKINS_LABEL.matcher(line);

        if (skinsMatcher.find()) {
            Matcher m = PATTERN_COMMA_LIST.matcher(skinsMatcher.group(1));
            while (m.find()) {
                target.addSkin(m.group(1).trim());
            }
        }
    }

    private static void setDescription(Target target, String line) {
        Matcher m = PATTERN_DESCRIPTION.matcher(line);

        if (m.find()) {
            target.setDescription(m.group(1));
        }
    }

    private static void setApiLevel(Target target, String line) {
        Matcher m = PATTERN_API_LEVEL.matcher(line);

        if (m.find()) {
            target.setApiLevel(Integer.parseInt(m.group(1)));
        }
    }

    private static void setRevision(Target target, String line) {
        Matcher m = PATTERN_REVISION.matcher(line);

        if (m.find()) {
            target.setRevision(Integer.parseInt(m.group(1)));
        }
    }

    private static void setVendor(Target target, String line) {
        Matcher m = PATTERN_VENDOR.matcher(line);

        if (m.find()) {
            target.setVendor(m.group(1));
        }
    }

    private static void setType(Target target, String line) {
        Matcher m = PATTERN_TYPE.matcher(line);

        if (m.find()) {
            target.setTargetType(TARGET_TYPE.match(m.group(1)));
        }
    }

    private static void setName(Target target, String line) {
        Matcher m = PATTERN_NAME.matcher(line);

        if (m.find()) {
            String name = m.group(1);
            target.setName(name);
        }
    }

    private static void setId(Target target, String line) {
        Matcher m = PATTERN_ID_LABEL.matcher(line);

        if (m.find() && m.groupCount() >= 2) {
            String id = m.group(1);
            String label = m.group(2);

            target.setId(Integer.parseInt(id));
            target.setIdLabel(label);

            Matcher googleLabel = PATTERN_ID_LABEL_GOOGLE.matcher(label);

            if (googleLabel.find() && googleLabel.groupCount() >= 3) {
                target.setApiLevel(Integer.parseInt(googleLabel.group(3)));
            }
        }
    }

}
