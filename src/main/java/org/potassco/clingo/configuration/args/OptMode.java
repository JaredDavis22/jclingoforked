/*
 * Copyright (C) 2021 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
 
package org.potassco.clingo.configuration.args;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * The optimization algorithm used by the solver. Initial bounds for the objective functions can be added with
 * {@link #withBounds(long...)}, which is what {@link #Enum} needs to enumerate models below a cost.
 */
public enum OptMode implements Option {

    Optimal("opt"),
    Enum("enum"),
    OptimalN("optN"),
    Ignore("ignore");

    private final String mode;

    OptMode(String mode) {
        this.mode = mode;
    }

    /**
     * Returns this mode with initial bounds for the objective functions.
     *
     * @param bounds one bound per objective function
     * @return the bounded option
     */
    public Option withBounds(long... bounds) {
        if (bounds.length == 0) {
            return this;
        }
        return new Bounded(this, bounds);
    }

    @Override
    public String getShellKey() {
        return "--opt-mode";
    }

    @Override
    public String getNativeKey() {
        return "solve.opt_mode";
    }

    @Override
    public String getValue() {
        return mode;
    }

    @Override
    public Option getDefault() {
        return OptMode.Optimal;
    }

    private static final class Bounded implements Option {

        private final OptMode optMode;
        private final long[] bounds;

        private Bounded(OptMode optMode, long[] bounds) {
            this.optMode = optMode;
            this.bounds = bounds.clone();
        }

        @Override
        public String getShellKey() {
            return optMode.getShellKey();
        }

        @Override
        public String getNativeKey() {
            return optMode.getNativeKey();
        }

        @Override
        public String getValue() {
            return optMode.mode + "," + LongStream.of(bounds)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(","));
        }

        @Override
        public Option getDefault() {
            return optMode.getDefault();
        }

        @Override
        public String toString() {
            return getValue();
        }
    }

}
