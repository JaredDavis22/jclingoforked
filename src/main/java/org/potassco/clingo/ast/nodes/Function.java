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

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.potassco.clingo.ast.Ast;
import org.potassco.clingo.ast.AstAttribute;
import org.potassco.clingo.ast.AstSequence;
import org.potassco.clingo.ast.AstType;
import org.potassco.clingo.ast.Location;
import org.potassco.clingo.internal.Clingo;
import org.potassco.clingo.internal.NativeSize;

public class Function extends Ast {

    public Function(Pointer ast) {
        super(ast);
    }

    public Function(Location location, String name, AstSequence arguments, int external) {
        super(create(location, name, arguments, external));
    }

    public Location getLocation() {
        Location.ByReference locationByReference = new Location.ByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_location(getPointer(), AstAttribute.LOCATION.getValue(), locationByReference));
        return locationByReference;
    }

    public String getName() {
        String[] stringByReference = new String[1];
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_string(getPointer(), AstAttribute.NAME.getValue(), stringByReference));
        return stringByReference[0];
    }

    public AstSequence getArguments() {
        return new AstSequence(this, AstAttribute.ARGUMENTS);
    }

    public int getExternal() {
        IntByReference intByReference = new IntByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_number(getPointer(), AstAttribute.EXTERNAL.getValue(), intByReference));
        return intByReference.getValue();
    }

    public void setLocation(Location location) {
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_location(getPointer(), AstAttribute.LOCATION.getValue(), location));
    }

    public void setName(String name) {
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_string(getPointer(), AstAttribute.NAME.getValue(), name));
    }

    public void setArguments(AstSequence arguments) {
        new AstSequence(this, AstAttribute.ARGUMENTS).set(arguments);
    }

    public void setExternal(int external) {
        Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_number(getPointer(), AstAttribute.EXTERNAL.getValue(), external));
    }

    private static Pointer create(Location location, String name, AstSequence arguments, int external) {
        PointerByReference pointerByReference = new PointerByReference();
        Clingo.check(Clingo.INSTANCE.clingo_ast_build(AstType.FUNCTION.getValue(), pointerByReference, location, name, arguments.getPointer(), new NativeSize(arguments.size()), external));
        return pointerByReference.getValue();
    }

}
