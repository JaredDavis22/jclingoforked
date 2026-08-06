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

import java.lang.ref.Reference;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.potassco.clingo.ast.nodes.Aggregate;
import org.potassco.clingo.ast.nodes.AggregateGuard;
import org.potassco.clingo.ast.nodes.BinaryOperation;
import org.potassco.clingo.ast.nodes.BodyAggregate;
import org.potassco.clingo.ast.nodes.BodyAggregateElement;
import org.potassco.clingo.ast.nodes.BooleanConstant;
import org.potassco.clingo.ast.nodes.Comment;
import org.potassco.clingo.ast.nodes.Comparison;
import org.potassco.clingo.ast.nodes.ConditionalLiteral;
import org.potassco.clingo.ast.nodes.Defined;
import org.potassco.clingo.ast.nodes.Definition;
import org.potassco.clingo.ast.nodes.Disjunction;
import org.potassco.clingo.ast.nodes.Edge;
import org.potassco.clingo.ast.nodes.External;
import org.potassco.clingo.ast.nodes.Function;
import org.potassco.clingo.ast.nodes.HeadAggregate;
import org.potassco.clingo.ast.nodes.HeadAggregateElement;
import org.potassco.clingo.ast.nodes.Heuristic;
import org.potassco.clingo.ast.nodes.Id;
import org.potassco.clingo.ast.nodes.Interval;
import org.potassco.clingo.ast.nodes.Literal;
import org.potassco.clingo.ast.nodes.Minimize;
import org.potassco.clingo.ast.nodes.Pool;
import org.potassco.clingo.ast.nodes.Program;
import org.potassco.clingo.ast.nodes.ProjectAtom;
import org.potassco.clingo.ast.nodes.ProjectSignature;
import org.potassco.clingo.ast.nodes.Rule;
import org.potassco.clingo.ast.nodes.Script;
import org.potassco.clingo.ast.nodes.ShowSignature;
import org.potassco.clingo.ast.nodes.ShowTerm;
import org.potassco.clingo.ast.nodes.SymbolicAtom;
import org.potassco.clingo.ast.nodes.SymbolicTerm;
import org.potassco.clingo.ast.nodes.TheoryAtom;
import org.potassco.clingo.ast.nodes.TheoryAtomDefinition;
import org.potassco.clingo.ast.nodes.TheoryAtomElement;
import org.potassco.clingo.ast.nodes.TheoryDefinition;
import org.potassco.clingo.ast.nodes.TheoryFunction;
import org.potassco.clingo.ast.nodes.TheoryGuard;
import org.potassco.clingo.ast.nodes.TheoryGuardDefinition;
import org.potassco.clingo.ast.nodes.TheoryOperatorDefinition;
import org.potassco.clingo.ast.nodes.TheorySequence;
import org.potassco.clingo.ast.nodes.TheoryTermDefinition;
import org.potassco.clingo.ast.nodes.TheoryUnparsedTerm;
import org.potassco.clingo.ast.nodes.TheoryUnparsedTermElement;
import org.potassco.clingo.ast.nodes.UnaryOperation;
import org.potassco.clingo.ast.nodes.Variable;
import org.potassco.clingo.control.Control;
import org.potassco.clingo.control.LoggerCallback;
import org.potassco.clingo.internal.Clingo;
import org.potassco.clingo.internal.NativeSize;
import org.potassco.clingo.internal.NativeSizeByReference;

/**
 * Represents a node in the abstract syntax tree.
 * <p>
 * The attributes of an <code>AST</code> are tied to its type.
 * <p>
 * Furthermore, AST nodes implement comparison operators and are
 * ordered structurally ignoring the location. Their string representation
 * corresponds to their gringo representation. In fact, the string
 * representation of any AST obtained from {@link #parseString(String)}
 * can be parsed again. Note that it is possible to construct ASTs
 * that are not parsable, though.
 *
 * <h2>Owned and borrowed nodes</h2>
 * clingo counts references on native nodes, so every node handed out by the binding is either owned or borrowed.
 * <p>
 * An owned node holds a reference of its own and has to be {@link #close() closed} once it is no longer needed. Owned
 * nodes come from {@link #parseString(String)}, {@link #parseFiles(Path...)}, {@link #copy()}, {@link #deepCopy()},
 * {@link #unpool(UnpoolType)} and from the constructors of the classes in the <code>nodes</code> package. Where several
 * of them are returned at once, closing the surrounding {@link AstList} releases all of them, and an {@link AstScope}
 * does the same for nodes built one by one.
 * <p>
 * A borrowed node is kept alive by someone else and needs no closing, so traversing and rewriting a tree requires no
 * bookkeeping at all. Every attribute getter, every element of an {@link AstSequence} and every node passed to an
 * {@link AstCallback} is borrowed. Such a node must not be used after the node it was read from was released, after the
 * attribute it was read from was overwritten, or after the callback that received it returned. Use {@link #retain()} to
 * keep a borrowed node beyond that point.
 * <p>
 * Releasing happens on the calling thread on purpose, because clingo does not count references atomically. For the
 * same reason it is never left to the garbage collector.
 */
public abstract class Ast implements Comparable<Ast>, AutoCloseable {

    private final Pointer ast;

    private boolean owned = true;

    private boolean released;

    /**
     * Adopts an existing reference on the given node, resulting in an owned node that has to be closed.
     */
    public Ast(Pointer ast) {
        this.ast = ast;
    }

    @Override
    public String toString() {
        NativeSizeByReference nativeSizeByReference = new NativeSizeByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_to_string_size(getPointer(), nativeSizeByReference));
        int stringSize = (int) nativeSizeByReference.getValue();
        byte[] stringBytes = new byte[stringSize];
        Clingo.check(Clingo.INSTANCE.clingo_ast_to_string(getPointer(), stringBytes, new NativeSize(stringSize)));
        return Native.toString(stringBytes, Clingo.STRING_ENCODING);
    }

    /**
     * @return The type of the node.
     */
    public AstType getType() {
        IntByReference intByReference = new IntByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_get_type(getPointer(), intByReference));
        return AstType.fromValue(intByReference.getValue());
    }

    /**
     * Get the type of the given AST attribute.
     *
     * @param attribute the target attribute
     * @return the resulting type
     */
    public AttributeType getAttributeType(AstAttribute attribute) {
        IntByReference intByReference = new IntByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_type(getPointer(), attribute.getValue(), intByReference));
        return AttributeType.fromValue(intByReference.getValue());
    }

    /**
     * Check if an AST has the given attribute.
     *
     * @param attribute the attribute to check
     * @return the result
     */
    public boolean hasAttribute(AstAttribute attribute) {
        ByteByReference byteByReference = new ByteByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_has_attribute(getPointer(), attribute.getValue(), byteByReference));
        return byteByReference.getValue() > 0;
    }

    /**
     * Check whether an optional attribute of this node is set.
     *
     * @param attribute the optional attribute to check
     * @return the result
     */
    protected boolean hasOptionalAst(AstAttribute attribute) {
        PointerByReference pointerByReference = new PointerByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_optional_ast(getPointer(), attribute.getValue(), pointerByReference));
        Pointer optional = pointerByReference.getValue();
        if (optional == null) {
            return false;
        }
        // reading the attribute increments the reference count, and this check has no use for the node itself
        Clingo.INSTANCE.clingo_ast_release(optional);
        return true;
    }

    /**
     * Get an optional attribute of this node. The returned node is borrowed from this one.
     *
     * @param attribute the optional attribute to read
     * @return the attribute value
     * @throws NoSuchElementException if the attribute is not set
     */
    protected Ast getOptionalAst(AstAttribute attribute) {
        PointerByReference pointerByReference = new PointerByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_optional_ast(getPointer(), attribute.getValue(), pointerByReference));
        if (pointerByReference.getValue() == null) {
            throw new NoSuchElementException("there is no optional ast " + attribute);
        }
        return Ast.borrowChild(pointerByReference.getValue());
    }

    /**
     * Unpool the AST returning a list of ASTs without pool terms.
     *
     * @param unpoolType how to unpool
     * @return list of owned asts
     */
    public AstList unpool(UnpoolType unpoolType) {
        AstList returnValues = new AstList();
        AstCallback callback = ast -> returnValues.add(ast.retain());
        try {
            Clingo.check(Clingo.INSTANCE.clingo_ast_unpool(getPointer(), unpoolType.getValue(), callback, null));
        }
        finally {
            // jna hands the native side a trampoline and keeps no reference on the callback itself, so without this the
            // garbage collector may free the trampoline while clingo is still calling it
            Reference.reachabilityFence(callback);
        }
        return returnValues;
    }

    /**
     * Compute a native 64-bit hash for an AST node.
     * Note that {@link #hashCode()} re-uses this method, but returns a 32-bit value.
     *
     * @return the resulting hash code
     */
    public long getHash() {
        return Clingo.INSTANCE.clingo_ast_hash(getPointer()).longValue();
    }

    /**
     * @return Return an owned shallow copy of the ast.
     */
    public Ast copy() {
        PointerByReference pointerByReference = new PointerByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_copy(getPointer(), pointerByReference));
        return create(pointerByReference.getValue());
    }

    /**
     * @return Return an owned deep copy of the ast.
     */
    public Ast deepCopy() {
        PointerByReference pointerByReference = new PointerByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_deep_copy(getPointer(), pointerByReference));
        return create(pointerByReference.getValue());
    }

    /**
     * Parse the given program and return a list of abstract syntax trees for each statement.
     *
     * @param program String representation of the program.
     */
    public static AstList parseString(String program) {
        AstList asts = new AstList();
        Ast.parseString(program, ast -> asts.add(ast.retain()), null, 0);
        return asts;
    }

    /**
     * Parse the given program and return a list of abstract syntax trees for each statement.
     *
     * @param program      String representation of the program.
     * @param logger       Function to intercept messages normally printed to standard error.
     * @param messageLimit The maximum number of messages passed to the logger.
     */
    public static AstList parseString(String program, LoggerCallback logger, int messageLimit) {
        AstList asts = new AstList();
        Ast.parseString(program, ast -> asts.add(ast.retain()), logger, messageLimit);
        return asts;
    }

    /**
     * Parse the given program and return an abstract syntax tree for each
     * statement via a callback.
     *
     * @param program  String representation of the program.
     * @param callback Callable taking an ast as argument.
     */
    public static void parseString(String program, AstCallback callback) {
        Ast.parseString(program, callback, null, 0);
    }

    /**
     * Parse the given program and return an abstract syntax tree for each
     * statement via a callback.
     *
     * @param program      String representation of the program.
     * @param callback     Callable taking an ast as argument.
     * @param logger       Function to intercept messages normally printed to standard error.
     * @param messageLimit The maximum number of messages passed to the logger.
     */
    public static void parseString(String program, AstCallback callback, LoggerCallback logger, int messageLimit) {
        try {
            Clingo.check(Clingo.INSTANCE.clingo_ast_parse_string(program, callback, null, null, logger, null, messageLimit));
        }
        finally {
            keepAlive(callback, logger);
        }
    }

    /**
     * Parse the given program and return an abstract syntax tree for each
     * statement via a callback.
     *
     * @param program      String representation of the program.
     * @param callback     Callable taking an ast as argument.
     * @param logger       Function to intercept messages normally printed to standard error.
     * @param messageLimit The maximum number of messages passed to the logger.
     */
    public static void parseString(String program, AstCallback callback, Control control, LoggerCallback logger, int messageLimit) {
        try {
            Clingo.check(Clingo.INSTANCE.clingo_ast_parse_string(program, callback, control.getPointer(), null, logger, null, messageLimit));
        }
        finally {
            keepAlive(callback, logger);
        }
    }

    /**
     * Parse the programs in the given files and return an abstract syntax tree for each statement via a callback.
     *
     * @param callback Callable taking an ast as argument.
     * @param paths    The files to parse.
     */
    public static void parseFiles(AstCallback callback, Path... paths) {
        parseFiles(callback, null, 0, paths);
    }

    /**
     * Parse the programs in the given files and return an abstract syntax tree for each statement via a callback.
     *
     * @param callback     Callable taking an ast as argument.
     * @param logger       Function to intercept messages normally printed to standard error.
     * @param messageLimit The maximum number of messages passed to the logger.
     * @param paths        The files to parse.
     */
    public static void parseFiles(AstCallback callback, LoggerCallback logger, int messageLimit, Path... paths) {
        String[] files = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = paths[i].toString();
        }
        try {
            Clingo.check(Clingo.INSTANCE.clingo_ast_parse_files(
                    files, new NativeSize(files.length),
                    callback, null,
                    null,
                    logger, null, messageLimit));
        }
        finally {
            keepAlive(callback, logger);
        }
    }

    /**
     * Parse the programs in the given files and return a list of abstract syntax trees for each statement.
     *
     * @param paths The files to parse.
     */
    public static AstList parseFiles(Path... paths) {
        AstList asts = new AstList();
        parseFiles(ast -> asts.add(ast.retain()), null, 0, paths);
        return asts;
    }

    /**
     * Decrement the reference count of an AST node. The node is deleted if the reference count reaches zero. Repeated
     * calls and calls on borrowed nodes have no effect.
     */
    public void release() {
        if (released || !owned) {
            return;
        }
        released = true;
        Clingo.INSTANCE.clingo_ast_release(ast);
    }

    @Override
    public void close() {
        release();
    }

    /**
     * Acquires a reference of its own for this node, so that it stays valid independently of the node it was borrowed
     * from. An owned node is returned unchanged, therefore this can be called on any node without checking.
     * <p>
     * Since an already owned node is left alone, this does not protect an element of an {@link AstList} against that
     * list being closed. Take the element out of the list with {@link AstList#remove(int)} instead, which hands
     * ownership over to the caller.
     *
     * @return this node, now owned and in need of being closed
     */
    public Ast retain() {
        if (released) {
            throw new IllegalStateException("this ast node was already released");
        }
        if (!owned) {
            Clingo.INSTANCE.clingo_ast_acquire(ast);
            owned = true;
        }
        return this;
    }

    /**
     * @return whether this node holds a reference of its own and hence has to be closed
     */
    public boolean isOwned() {
        return owned;
    }

    /**
     * Equality compare two AST nodes.
     *
     * @param other the right-hand-side AST
     * @return the result of the compariso
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Ast))
            return false;
        return Clingo.INSTANCE.clingo_ast_equal(getPointer(), ((Ast) other).getPointer()) > 0;
    }

    /**
     * Calculates a Java hash for this object.
     * Note, that this method relies on the native hash (see {@link #getHash()}, but consists of 32-bit only.
     */
    @Override
    public int hashCode() {
        return Long.valueOf(getHash()).hashCode();
    }

    /**
     * Less than compare two AST nodes.
     *
     * @param other the right-hand-side AST
     * @return the result of the compariso
     */
    public boolean isLess(Ast other) {
        return Clingo.INSTANCE.clingo_ast_less_than(getPointer(), other.getPointer()) > 0;
    }

    @Override
    public int compareTo(Ast other) {
        return equals(other) ? 0 : isLess(other) ? -1 : 1;
    }

    /**
     * @return the native node behind this object
     * @throws IllegalStateException if this node was already released
     */
    public Pointer getPointer() {
        if (released) {
            throw new IllegalStateException("this ast node was already released");
        }
        return ast;
    }

    /**
     * Keeps callbacks reachable until the native call using them has returned. jna hands the native side a trampoline
     * and keeps no reference on the callback itself, so the garbage collector may otherwise free that trampoline while
     * clingo is still calling it.
     */
    private static void keepAlive(Object callback, Object logger) {
        Reference.reachabilityFence(callback);
        Reference.reachabilityFence(logger);
    }

    /**
     * Wraps a node without taking a reference on it, for nodes that are kept alive by someone else for as long as they
     * are handed out, such as the arguments of an {@link AstCallback}.
     */
    protected static Ast borrow(Pointer ast) {
        Ast node = create(ast);
        node.owned = false;
        return node;
    }

    /**
     * Wraps a node read from an attribute of another node. Attribute accessors increment the reference count, and that
     * reference is given back right away, because the surrounding node holds one for as long as it refers to the child.
     */
    protected static Ast borrowChild(Pointer ast) {
        try {
            return borrow(ast);
        }
        finally {
            Clingo.INSTANCE.clingo_ast_release(ast);
        }
    }

    protected static Ast create(Pointer ast) {
        IntByReference intByReference = new IntByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_get_type(ast, intByReference));
        AstType type = AstType.fromValue(intByReference.getValue());
        return create(ast, type);
    }

    protected static Ast create(Pointer ast, AstType type) {
        switch (type) {
            case ID:
                return new Id(ast);
            case VARIABLE:
                return new Variable(ast);
            case SYMBOLIC_TERM:
                return new SymbolicTerm(ast);
            case UNARY_OPERATION:
                return new UnaryOperation(ast);
            case BINARY_OPERATION:
                return new BinaryOperation(ast);
            case INTERVAL:
                return new Interval(ast);
            case FUNCTION:
                return new Function(ast);
            case POOL:
                return new Pool(ast);
            case BOOLEAN_CONSTANT:
                return new BooleanConstant(ast);
            case SYMBOLIC_ATOM:
                return new SymbolicAtom(ast);
            case COMPARISON:
                return new Comparison(ast);
            case AGGREGATE_GUARD:
                return new AggregateGuard(ast);
            case CONDITIONAL_LITERAL:
                return new ConditionalLiteral(ast);
            case AGGREGATE:
                return new Aggregate(ast);
            case BODY_AGGREGATE_ELEMENT:
                return new BodyAggregateElement(ast);
            case BODY_AGGREGATE:
                return new BodyAggregate(ast);
            case HEAD_AGGREGATE_ELEMENT:
                return new HeadAggregateElement(ast);
            case HEAD_AGGREGATE:
                return new HeadAggregate(ast);
            case DISJUNCTION:
                return new Disjunction(ast);
            case THEORY_SEQUENCE:
                return new TheorySequence(ast);
            case THEORY_FUNCTION:
                return new TheoryFunction(ast);
            case THEORY_UNPARSED_TERM_ELEMENT:
                return new TheoryUnparsedTermElement(ast);
            case THEORY_UNPARSED_TERM:
                return new TheoryUnparsedTerm(ast);
            case THEORY_GUARD:
                return new TheoryGuard(ast);
            case THEORY_ATOM_ELEMENT:
                return new TheoryAtomElement(ast);
            case THEORY_ATOM:
                return new TheoryAtom(ast);
            case LITERAL:
                return new Literal(ast);
            case THEORY_OPERATOR_DEFINITION:
                return new TheoryOperatorDefinition(ast);
            case THEORY_TERM_DEFINITION:
                return new TheoryTermDefinition(ast);
            case THEORY_GUARD_DEFINITION:
                return new TheoryGuardDefinition(ast);
            case THEORY_ATOM_DEFINITION:
                return new TheoryAtomDefinition(ast);
            case RULE:
                return new Rule(ast);
            case DEFINITION:
                return new Definition(ast);
            case SHOW_SIGNATURE:
                return new ShowSignature(ast);
            case SHOW_TERM:
                return new ShowTerm(ast);
            case MINIMIZE:
                return new Minimize(ast);
            case SCRIPT:
                return new Script(ast);
            case PROGRAM:
                return new Program(ast);
            case EXTERNAL:
                return new External(ast);
            case EDGE:
                return new Edge(ast);
            case HEURISTIC:
                return new Heuristic(ast);
            case PROJECT_ATOM:
                return new ProjectAtom(ast);
            case PROJECT_SIGNATURE:
                return new ProjectSignature(ast);
            case DEFINED:
                return new Defined(ast);
            case THEORY_DEFINITION:
                return new TheoryDefinition(ast);
			case COMMENT:
				return new Comment(ast);
            default:
                throw new IllegalStateException("Unknown AST type: " + type.name());
        }
    }

}
