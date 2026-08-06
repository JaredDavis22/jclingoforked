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

package org.potassco.clingo.ast.nodes;

import java.util.NoSuchElementException;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.potassco.clingo.ast.Ast;
import org.potassco.clingo.ast.AstAttribute;
import org.potassco.clingo.ast.AstType;
import org.potassco.clingo.ast.Location;
import org.potassco.clingo.internal.Clingo;

public class TheoryAtomDefinition extends Ast {

    public TheoryAtomDefinition(Pointer ast) {
        super(ast);
    }

    public TheoryAtomDefinition(Location location, int atomType, String name, int arity, String term, Ast guard) {
        super(create(location, atomType, name, arity, term, guard));
    }

    public Location getLocation() {
        Location.ByReference locationByReference = new Location.ByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_location(ast, AstAttribute.LOCATION.getValue(), locationByReference));
        return locationByReference;
    }

    public int getAtomType() {
        IntByReference intByReference = new IntByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_number(ast, AstAttribute.ATOM_TYPE.getValue(), intByReference));
        return intByReference.getValue();
    }

    public String getName() {
        String[] stringByReference = new String[1];
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_string(ast, AstAttribute.NAME.getValue(), stringByReference));
        return stringByReference[0];
    }

    public int getArity() {
        IntByReference intByReference = new IntByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_number(ast, AstAttribute.ARITY.getValue(), intByReference));
        return intByReference.getValue();
    }

    public String getTerm() {
        String[] stringByReference = new String[1];
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_string(ast, AstAttribute.TERM.getValue(), stringByReference));
        return stringByReference[0];
    }

    public boolean hasGuard() {
        return hasOptionalAst(AstAttribute.GUARD);
    }

    public Ast getGuard() {
        return getOptionalAst(AstAttribute.GUARD);
    }

    public void setLocation(Location location) {
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_location(ast, AstAttribute.LOCATION.getValue(), location));
    }

    public void setAtomType(int atomType) {
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_number(ast, AstAttribute.ATOM_TYPE.getValue(), atomType));
    }

    public void setName(String name) {
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_string(ast, AstAttribute.NAME.getValue(), name));
    }

    public void setArity(int arity) {
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_number(ast, AstAttribute.ARITY.getValue(), arity));
    }

    public void setTerm(String term) {
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_string(ast, AstAttribute.TERM.getValue(), term));
    }

    public void setGuard(Ast guard) {
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_optional_ast(this.ast, AstAttribute.GUARD.getValue(), guard.getPointer()));
    }

    private static Pointer create(Location location, int atomType, String name, int arity, String term, Ast guard) {
        PointerByReference pointerByReference = new PointerByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_build(AstType.THEORY_ATOM_DEFINITION.getValue(), pointerByReference, location, atomType, name, arity, term, guard.getPointer()));
        return pointerByReference.getValue();
    }

}
