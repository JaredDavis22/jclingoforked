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

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Collects owned {@link Ast} nodes and releases them together.
 * <p>
 * This is meant for code that assembles a lot of nodes by hand, where one try with resources per node would drown out
 * the actual program being built. Nodes read from an existing tree are borrowed and need not be registered here.
 * <p>
 * A scope releases what it holds in reverse order of registration, on the thread that closes it. Nodes registered here
 * must not outlive the scope, so a node that has to be kept longer belongs into the enclosing scope instead.
 *
 * <pre>{@code
 * try (AstScope scope = new AstScope()) {
 *     Ast head = scope.add(new SymbolicAtom(scope.add(new Function(location, "a", arguments, false))));
 *     builder.add(scope.add(new Rule(location, head, body)));
 * }
 * }</pre>
 */
public class AstScope implements AutoCloseable {

    private final Deque<Runnable> releases = new ArrayDeque<>();

    /**
     * Registers an owned node with this scope.
     *
     * @param node the node to release once this scope is closed
     * @return the given node
     */
    public <T extends Ast> T add(T node) {
        releases.push(node::release);
        return node;
    }

    /**
     * Registers a list of owned nodes with this scope.
     *
     * @param nodes the nodes to release once this scope is closed
     * @return the given list
     */
    public AstList add(AstList nodes) {
        releases.push(nodes::close);
        return nodes;
    }

    /**
     * Releases everything registered with this scope. Repeated calls have no effect.
     */
    @Override
    public void close() {
        while (!releases.isEmpty()) {
            releases.pop().run();
        }
    }
}
