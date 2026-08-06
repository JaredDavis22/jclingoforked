package org.potassco.clingo.ast.nodes;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.potassco.clingo.ast.Ast;
import org.potassco.clingo.ast.AstAttribute;
import org.potassco.clingo.ast.AstType;
import org.potassco.clingo.ast.CommentType;
import org.potassco.clingo.ast.Location;
import org.potassco.clingo.internal.Clingo;

public class Comment extends Ast {

	public Comment(Pointer ast) {
		super(ast);
	}

	public Comment(Location location, String value, CommentType commentType) {
		super(create(location, value, commentType.getValue()));
	}

	public Location getLocation() {
		Location.ByReference locationByReference = new Location.ByReference();
		Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_location(ast, AstAttribute.LOCATION.getValue(), locationByReference));
		return locationByReference;
	}

	public String getValue() {
		String[] stringByRef = new String[1];
		Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_string(ast, AstAttribute.VALUE.getValue(), stringByRef));
		return stringByRef[0];
	}

	public CommentType getCommentType() {
		IntByReference intByReference = new IntByReference();
		Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_get_number(ast, AstAttribute.COMMENT_TYPE.getValue(), intByReference));
		return CommentType.fromValue(intByReference.getValue());
	}

	public void setLocation(Location location) {
		Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_location(ast, AstAttribute.LOCATION.getValue(), location));
	}

	public void setValue(String value) {
		Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_string(ast, AstAttribute.VALUE.getValue(), value));
	}

	public void setCommentType(CommentType commentType) {
		Clingo.check(Clingo.INSTANCE.clingo_ast_attribute_set_number(ast, AstAttribute.COMMENT_TYPE.getValue(), commentType.getValue()));
	}

	private static Pointer create(Location location, String value, int commentType) {
		PointerByReference pointerByReference = new PointerByReference();
		Clingo.check(Clingo.INSTANCE.clingo_ast_build(AstType.COMMENT.getValue(), pointerByReference, location, value, commentType));
		return pointerByReference.getValue();
	}
}
