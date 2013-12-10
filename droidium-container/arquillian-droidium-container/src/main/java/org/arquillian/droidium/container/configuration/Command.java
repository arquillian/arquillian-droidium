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
package org.arquillian.droidium.container.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.arquillian.droidium.container.utils.StringUtils;

/**
 * Represents emulator command we are creating in order to execute some command on the command line.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class Command {

    private final List<String> command;

    public Command() {
        command = new ArrayList<String>();
    }

    /**
     *
     * @param command
     * @throws IllegalArgumentException if {@code command} is null
     */
    public Command(List<String> command) throws IllegalArgumentException {
        this();
        if (command == null) {
            throw new IllegalArgumentException("command can't be a null list!");
        }
        for (String token : command) {
            add(token);
        }
    }

    public Command(String... tokens) {
        this(Arrays.asList(tokens));

    }

    /**
     * Adds a token to the command list.
     *
     * @param token token to add to the command list
     * @return instance of this {@code Command}
     */
    public Command add(String token) {
        if (token != null && !token.trim().equals("")) {
            command.add(token.trim());
        }
        return this;
    }

    public Command addTokenized(String stringToBeParsed) {
        return add(StringUtils.tokenize(stringToBeParsed));
    }

    /**
     * Add list of tokens to already existing list we are constructing, ignoring null and empty ones.
     *
     * @param tokens tokens we are adding to the already existing list
     * @return instance of this {@code Command}
     */
    public Command add(List<String> tokens) {
        for (String token : tokens) {
            add(token);
        }
        return this;
    }

    /**
     * Remove all occurences of {@code token} from the command list.
     *
     * @param token token to remove
     * @return instance of this {@code EmulatorCommand}
     */
    public Command remove(String token) {
        if (token == null || token.trim().equals("")) {
            return this;
        }

        while (command.remove(token)) {
        }

        return this;
    }

    /**
     * Clears the emulator command list.
     *
     * @return instance of this {@code EmulatorCommand}
     */
    public Command clear() {
        command.clear();
        return this;
    }

    /**
     * Return size of the command.
     *
     * @return number of tokens stored in the command
     */
    public int size() {
        return command.size();
    }

    /**
     *
     * @return command we constructed
     */
    public List<String> getAsList() {
        return command;
    }

    public String[] getAsArray() {
        return command.toArray(new String[command.size()]);
    }

    /**
     * Returns token on i-th position
     *
     * @param i position of token we want to get
     * @return token on i-th position, null if we are out of bounds
     */
    public String get(int i) {
        try {
            return command.get(i);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     *
     * @return last token from the command
     */
    public String getLast() {
        if (command.isEmpty()) {
            return null;
        }
        return command.get(command.size() - 1);
    }

    /**
     *
     * @return first token from the command
     */
    public String getFirst() {
        if (command.isEmpty()) {
            return null;
        }
        return command.get(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : command) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString().trim();
    }
}
