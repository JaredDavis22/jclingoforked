package org.potassco.clingo.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Enumeration of attributes used by the AST.
 */
public enum AstAttribute {

    ARGUMENT(0),
    ARGUMENTS(1),
    ARITY(2),
    ATOM(3),
    ATOMS(4),
    ATOM_TYPE(5),
    BIAS(6),
    BODY(7),
    CODE(8),
    COEFFICIENT(9),
    COMPARISON(10),
    CONDITION(11),
    ELEMENTS(12),
    EXTERNAL(13),
    EXTERNAL_TYPE(14),
    FUNCTION(15),
    GUARD(16),
    GUARDS(17),
    HEAD(18),
    IS_DEFAULT(19),
    LEFT(20),
    LEFT_GUARD(21),
    LITERAL(22),
    LOCATION(23),
    MODIFIER(24),
    NAME(25),
    NODE_U(26),
    NODE_V(27),
    OPERATOR_NAME(28),
    OPERATOR_TYPE(29),
    OPERATORS(30),
    PARAMETERS(31),
    POSITIVE(32),
    PRIORITY(33),
    RIGHT(34),
    RIGHT_GUARD(35),
    SEQUENCE_TYPE(36),
    SIGN(37),
    SYMBOL(38),
    TERM(39),
    TERMS(40),
    VALUE(41),
    VARIABLE(42),
    WEIGHT(43),
    COMMENT_TYPE(44);

    private static final Map<Integer, AstAttribute> mapping = new HashMap<>();

    static {
        for (AstAttribute attribute : AstAttribute.values()) {
            mapping.put(
                    attribute.getValue(),
                    attribute
            );
        }
    }

    public static AstAttribute fromValue(int attribute) {
        return Objects.requireNonNull(mapping.get(attribute), "unknown ast attribute " + attribute);
    }

    private final int attribute;

    AstAttribute(int attribute) {
        this.attribute = attribute;
    }

    public int getValue() {
        return attribute;
    }

}
