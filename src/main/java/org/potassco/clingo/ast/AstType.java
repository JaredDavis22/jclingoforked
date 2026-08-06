package org.potassco.clingo.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Enumeration of signs.
 * @author Josef Schneeberger
 */
public enum AstType {
	// terms
    ID(0),
    VARIABLE(1),
    SYMBOLIC_TERM(2),
    UNARY_OPERATION(3),
    BINARY_OPERATION(4),
    INTERVAL(5),
    FUNCTION(6),
    POOL(7),
    // simple atoms
    BOOLEAN_CONSTANT(8),
    SYMBOLIC_ATOM(9),
    COMPARISON(10),
    // aggregates
    AGGREGATE_GUARD(11),
    CONDITIONAL_LITERAL(12),
    AGGREGATE(13),
    BODY_AGGREGATE_ELEMENT(14),
    BODY_AGGREGATE(15),
    HEAD_AGGREGATE_ELEMENT(16),
    HEAD_AGGREGATE(17),
    DISJUNCTION(18),
    // theory atoms
    THEORY_SEQUENCE(19),
    THEORY_FUNCTION(20),
    THEORY_UNPARSED_TERM_ELEMENT(21),
    THEORY_UNPARSED_TERM(22),
    THEORY_GUARD(23),
    THEORY_ATOM_ELEMENT(24),
    THEORY_ATOM(25),
    // literals
    LITERAL(26),
    // theory definition
    THEORY_OPERATOR_DEFINITION(27),
    THEORY_TERM_DEFINITION(28),
    THEORY_GUARD_DEFINITION(29),
    THEORY_ATOM_DEFINITION(30),
    // statements
    RULE(31),
    DEFINITION(32),
    SHOW_SIGNATURE(33),
    SHOW_TERM(34),
    MINIMIZE(35),
    SCRIPT(36),
    PROGRAM(37),
    EXTERNAL(38),
    EDGE(39),
    HEURISTIC(40),
    PROJECT_ATOM(41),
    PROJECT_SIGNATURE(42),
    DEFINED(43),
    THEORY_DEFINITION(44),
    COMMENT(45);

    private static final Map<Integer, AstType> mapping = new HashMap<>();

	static {
	    for (AstType type : AstType.values()) {
	    	mapping.put(
	          type.getValue(),
	          type
	        );
	    }
	}

	public static AstType fromValue(int type) {
		return Objects.requireNonNull(mapping.get(type), "unknown ast type " + type);
	}

	private final int type;

	AstType(int type) {
		this.type = type;
	}

	public int getValue() {
		return type;
	}

}
