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

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class TagAbiPair implements Comparable<TagAbiPair> {

    private TAG tag = TAG.NOT_DEFINED;

    private ABI abi = ABI.NOT_DEFINED;

    public TagAbiPair(TAG tag, ABI abi) {
        if (tag != null) {
            this.tag = tag;
        }

        if (abi != null) {
            this.abi = abi;
        }
    }

    public TAG getTag() {
        return tag;
    }

    public ABI getAbi() {
        return abi;
    }

    @Override
    public String toString() {
        return tag.toString() + "/" + abi.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((abi == null) ? 0 : abi.hashCode());
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
        TagAbiPair other = (TagAbiPair) obj;
        if (tag != other.tag)
            return false;
        if (abi != other.abi)
            return false;
        return true;
    }

    public static TagAbiPair construct(String tagAbiString) {

        if (tagAbiString == null || tagAbiString.isEmpty()) {
            return null;
        }

        String[] pair = tagAbiString.split("/");

        if (pair.length != 2) {
            return null;
        }

        TAG tag = TAG.match(pair[0]);
        ABI abi = ABI.match(pair[1]);

        TagAbiPair tagAbiPair = new TagAbiPair(tag, abi);

        return tagAbiPair;
    }

    @Override
    public int compareTo(TagAbiPair o) {
        if (o == null) {
            return 1;
        }

        // compare the first enumeration
        int comparision = this.tag.ordinal() - o.tag.ordinal();

        // if there is a match one the first enumeration, lets check the second member
        if (comparision == 0) {
            comparision = this.abi.ordinal() - o.abi.ordinal();
        }

        return comparision;
    }

}
