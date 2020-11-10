package vvhile.basic.language;

import java.math.BigInteger;
import vvhile.frontend.Token;
import vvhile.frontend.Token.Symbol;

/**
 * This is a collection of all tokens used by the basic rule set.
 * 
 * @author markus
 */
public class BasicTokens {
    
    private BasicTokens() {
    }

    // language key words
    public static final Symbol SKIP = new Symbol("skip");
    public static final Symbol IF = new Symbol("if");
    public static final Symbol ELSE = new Symbol("else");
    public static final Symbol WHILE = new Symbol("while");
    public static final Symbol TRUE = new Symbol("true");
    public static final Symbol FALSE = new Symbol("false");
    public static final Symbol UNKNOWN = new Symbol("~");
    public static final Symbol SEMICOLON = new Symbol(";");
    public static final Symbol ASSIGN = new Symbol(":=");

    // parantheses
    public static final Symbol L_PAREN = new Symbol("(");
    public static final Symbol R_PAREN = new Symbol(")");
    public static final Symbol L_CURLY = new Symbol("{");
    public static final Symbol R_CURLY = new Symbol("}");
    
    // comparisons
    public static final Symbol LESS_THAN = new Symbol("<");
    public static final Symbol LESS_EQUAL = new Symbol("<=");
    public static final Symbol EQUALS = new Symbol("=");
    public static final Symbol GREATER_EQUAL = new Symbol(">=");
    public static final Symbol GREATER_THAN = new Symbol(">");
    
    // operations
    public static final Symbol PLUS = new Symbol("+");
    public static final Symbol MINUS = new Symbol("-");
    public static final Symbol TIMES = new Symbol("*");
    public static final Symbol DIV = new Symbol("/");
    public static final Symbol AND = new Symbol("&");
    public static final Symbol OR = new Symbol("|");
    public static final Symbol NOT = new Symbol("!");
    
    // logical
    public static final Symbol FORALL = new Symbol("^");
    public static final Symbol EXISTS = new Symbol("?");
    public static final Symbol IN = EXISTS;
    public static final Symbol DOT = new Symbol(".");
    public static final Symbol IMPLIES = new Symbol("->");
    public static final Symbol IMPLIED_BY = new Symbol("<-");
    public static final Symbol TYPE = new Symbol("::");
    public static final Symbol DOTS = new Symbol("...");

    // Extension: Exceptions
    public static final Symbol EXC = new Symbol("exc");
    public static final Symbol TRY = new Symbol("try");
    public static final Symbol CATCH = new Symbol("catch");
    public static final Symbol THROW = new Symbol("throw");

    // Extension: Procedures
    public static final Symbol MAPSTO = IMPLIES;
    public static final Symbol COMMA = new Symbol(",");

    /**
     * The numerical token to appear in a grammar. It has no predefined internal
     * value.
     */
    public static final IntegerScanner.Number NUM = new IntegerScanner.Number(BigInteger.ZERO) {
        @Override
        public String toString() {
            return "<num,?>";
        }
    };

    /**
     * The identifier token to appear in a grammar. It has no predefined
     * internal value.
     */
    public static final Token.Identifier ID = new Token.Identifier("") {
        @Override
        public String toString() {
            return "<id,?>";
        }
    };
    
}
