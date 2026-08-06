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

package org.potassco.clingo.ast;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of owned {@link Ast} nodes that releases all of them at once.
 * <p>
 * Nodes read from the elements of this list are borrowed and become invalid together with the element they were read
 * from, so they must not be used after this list was closed.
 */
public class AstList extends AbstractList<Ast> implements AutoCloseable {

    private final List<Ast> nodes = new ArrayList<>();

    @Override
    public Ast get(int index) {
        return nodes.get(index);
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public void add(int index, Ast node) {
        nodes.add(index, node);
        modCount++;
    }

    /**
     * Takes a node out of this list, handing ownership of it over to the caller.
     *
     * @param index the position to remove
     * @return the removed node, which now has to be closed by the caller
     */
    @Override
    public Ast remove(int index) {
        modCount++;
        return nodes.remove(index);
    }

    /**
     * Releases every node of this list in reverse order. Repeated calls have no effect.
     */
    @Override
    public void close() {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            nodes.get(i).release();
        }
    }
}
