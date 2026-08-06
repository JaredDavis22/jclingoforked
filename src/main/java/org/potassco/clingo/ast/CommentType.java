package org.potassco.clingo.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Enumeration of comment types
 */
public enum CommentType {

	LINE(0),
	BLOCK(1);

	private static final Map<Integer, CommentType> mapping = new HashMap<>();

	static {
		for (CommentType type : CommentType.values()) {
			mapping.put(
					type.getValue(),
					type
			);
		}
	}

	public static CommentType fromValue(int type) {
		return Objects.requireNonNull(mapping.get(type), "unknown comment type " + type);
	}

	private final int type;

	CommentType(int type) {
		this.type = type;
	}

	public int getValue() {
		return type;
	}
}
