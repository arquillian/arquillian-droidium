/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.arquillian.droidium.container.configuration;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.arquillian.droidium.container.configuration.Command;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class which tests if tokens added to {@link Command} are valid.
 *
 * @author <a href="@mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class CommandTestCase {

    Command command;

    @Before
    public void init() {
        command = new Command();
    }

    @Test
    public void testDeleteTrailingSpaces() {
        String testString = " abcd   \"  a   \"  \"    c    d\" \"${HOME}\"";
        List<String> list = new ArrayList<String>();
        list.add("abcd");
        list.add("\"a\"");
        list.add("\"c d\"");
        list.add("\"${HOME}\"");
        assertTrue(listsAreSame(list, command.addAsString(testString).getAsList()));
    }

    private boolean listsAreSame(List<String> list1, List<String> list2) {
        if (list1 == null && list2 == null) {
            return true;
        }

        if (list1 != null && list2 != null) {
            if (list1.size() != list2.size()) {
                return false;
            }
            for (int i = 0; i < list1.size(); i++) {
                if (!list1.get(i).equals(list2.get(i))) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}
