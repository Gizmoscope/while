package vvhile.basic.language;

import vvhile.intrep.Operator;
import java.math.BigInteger;

/**
 * This is a collection of all operators used by the basic rule set.
 *
 * @author markus
 */
public final class BasicOperators {

    /**
     * Logical and.
     */
    public static Operator<Boolean> AND = Operator.createBinaryOperator(
            Boolean.class, Boolean.class, Boolean.class, (Boolean a, Boolean b) -> a && b, "∧"
    );
    
    /**
     * Logical or.
     */
    public static Operator<Boolean> OR = Operator.createBinaryOperator(
            Boolean.class, Boolean.class, Boolean.class, (Boolean a, Boolean b) -> a || b, "∨"
    );
    
    /**
     * Logical not.
     */
    public static Operator<Boolean> NOT = Operator.createUnaryOperator(
            Boolean.class, Boolean.class, (Boolean a) -> !a, "¬"
    );

    /**
     * Numerical plus.
     */
    public static Operator<BigInteger> PLUS = Operator.createBinaryOperator(
            BigInteger.class, BigInteger.class, BigInteger.class, (BigInteger a, BigInteger b) -> a.add(b), "+"
    );
    
    /**
     * Numerical minus.
     */
    public static Operator<BigInteger> MINUS = Operator.createBinaryOperator(
            BigInteger.class, BigInteger.class, BigInteger.class, (BigInteger a, BigInteger b) -> a.subtract(b), "-"
    );
    
    /**
     * Numerical times (Product).
     */
    public static Operator<BigInteger> TIMES = Operator.createBinaryOperator(
            BigInteger.class, BigInteger.class, BigInteger.class, (BigInteger a, BigInteger b) -> a.multiply(b), "⋅"
    );
    
    /**
     * Numerical long division.
     */
    public static Operator<BigInteger> DIV = Operator.createBinaryOperator(
            BigInteger.class, BigInteger.class, BigInteger.class, (BigInteger a, BigInteger b) -> a.divide(b), "/"
    );
    
    /**
     * Numerical negation (Additive inverse).
     */
    public static Operator<BigInteger> NEGATE = Operator.createUnaryOperator(
            BigInteger.class, BigInteger.class, (BigInteger a) -> a.negate(), "-"
    );

    /**
     * Strict inequation: less than.
     */
    public static Operator<Boolean> LESS_THAN = Operator.createBinaryOperator(
            BigInteger.class, BigInteger.class, Boolean.class, (BigInteger a, BigInteger b) -> a.compareTo(b) < 0, "<"
    );
    
    /**
     * Inequation: less or equal.
     */
    public static Operator<Boolean> LESS_EQUAL = Operator.createBinaryOperator(
            BigInteger.class, BigInteger.class, Boolean.class, (BigInteger a, BigInteger b) -> a.compareTo(b) <= 0, "≤"
    );
    
    /**
     * Inequation: greater or equal.
     */
    public static Operator<Boolean> GREATER_EQUAL = Operator.createBinaryOperator(
            BigInteger.class, BigInteger.class, Boolean.class, (BigInteger a, BigInteger b) -> a.compareTo(b) >= 0, "≥"
    );
    
    /**
     * Strickt inequation: greater than.
     */
    public static Operator<Boolean> GREATER_THAN = Operator.createBinaryOperator(
            BigInteger.class, BigInteger.class, Boolean.class, (BigInteger a, BigInteger b) -> a.compareTo(b) > 0, ">"
    );
    
    /**
     * Logical implication.
     */
    public static Operator<Boolean> IMPLIES = Operator.createBinaryOperator(
            Boolean.class, Boolean.class, Boolean.class, (Boolean a, Boolean b) -> !a || b, "⟹"
    );
    
    /**
     * Logical implication (reversed).
     */
    public static Operator<Boolean> IMPLIED_BY = Operator.createBinaryOperator(
            Boolean.class, Boolean.class, Boolean.class, (Boolean a, Boolean b) -> a || !b, "⟸"
    );
    
    /**
     * Equality.
     */
    public static Operator EQUALS = Operator.createBinaryOperator(
            Object.class, Object.class, Boolean.class, (a, b) -> a.equals(b), "="
    );

    private BasicOperators() {
    }

}
