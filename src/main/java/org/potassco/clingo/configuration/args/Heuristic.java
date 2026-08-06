/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */

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

/**
 * The decision heuristic used by the solver. Every heuristic uses clingo's default parameter unless one is given with
 * {@link #withParameter(int)}.
 */
public enum Heuristic implements Option {
    Berkmin("Berkmin", true),
    Vmtf("Vmtf", true),
    Vsids("Vsids", true),
    Domain("Domain", true),
    Unit("Unit", false),
    None("None", false);

    private final String mode;
    private final boolean lookback;

    Heuristic(String mode, boolean lookback) {
        this.mode = mode;
        this.lookback = lookback;
    }

    /**
     * Returns this heuristic with an explicit parameter. Its meaning depends on the heuristic, for example the number of
     * nogoods to check for Berkmin and the decay factor for Vsids.
     *
     * @param parameter the heuristic parameter
     * @return the parameterized option
     * @throws IllegalStateException if the heuristic does not take a parameter
     */
    public Option withParameter(int parameter) {
        if (!lookback) {
            throw new IllegalStateException("Heuristic '" + mode + "' does not take a parameter");
        }
        return new Parameterized(this, parameter);
    }

    @Override
    public String getShellKey() {
        return "--heuristic";
    }

    @Override
    public String getNativeKey() {
        return "solver.heuristic";
    }

    @Override
    public String getValue() {
        return mode;
    }

    @Override
    public Option getDefault() {
        return Heuristic.Vsids;
    }

    private static final class Parameterized implements Option {

        private final Heuristic heuristic;
        private final int parameter;

        private Parameterized(Heuristic heuristic, int parameter) {
            this.heuristic = heuristic;
            this.parameter = parameter;
        }

        @Override
        public String getShellKey() {
            return heuristic.getShellKey();
        }

        @Override
        public String getNativeKey() {
            return heuristic.getNativeKey();
        }

        @Override
        public String getValue() {
            return heuristic.mode + "," + parameter;
        }

        @Override
        public Option getDefault() {
            return heuristic.getDefault();
        }

        @Override
        public String toString() {
            return getValue();
        }
    }
}
