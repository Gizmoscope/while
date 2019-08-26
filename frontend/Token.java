package frontend;

import java.math.BigInteger;
import vvhile.intrep.Expression;

/**
 *
 * @author markus
 */
public interface Token extends Variable, Parser2.ParseObject {

    public static final EOF EOF = new EOF();

    public static final Symbol SKIP = new Symbol("skip");
    public static final Symbol IF = new Symbol("if");
    public static final Symbol ELSE = new Symbol("else");
    public static final Symbol WHILE = new Symbol("while");
    public static final Symbol TRUE = new Symbol("true");
    public static final Symbol FALSE = new Symbol("false");
    public static final Symbol UNKNOWN = new Symbol("~");

    public static final Symbol L_PAREN = new Symbol("(");
    public static final Symbol R_PAREN = new Symbol(")");
    public static final Symbol SEMICOLON = new Symbol(";");
    public static final Symbol L_CURLY = new Symbol("{");
    public static final Symbol R_CURLY = new Symbol("}");
    public static final Symbol ASSIGN = new Symbol(":=");
    public static final Symbol LESS_THAN = new Symbol("<");
    public static final Symbol LESS_EQUAL = new Symbol("<=");
    public static final Symbol EQUALS = new Symbol("=");
    public static final Symbol GREATER_EQUAL = new Symbol(">=");
    public static final Symbol GREATER_THAN = new Symbol(">");
    public static final Symbol PLUS = new Symbol("+");
    public static final Symbol MINUS = new Symbol("-");
    public static final Symbol TIMES = new Symbol("*");
    public static final Symbol AND = new Symbol("&");
    public static final Symbol OR = new Symbol("|");
    public static final Symbol NOT = new Symbol("!");
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

    public static final Identifier ID = new Identifier("") {
        @Override
        public String toString() {
            return "<id,?>";
        }
    };
    public static final Number NUM = new Number(BigInteger.ZERO) {
        @Override
        public String toString() {
            return "<num,?>";
        }
    };

    public static class Symbol implements Token {

        private final String symbol;

        public Symbol(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    public static class Number implements Token {

        private final BigInteger value;

        public Number(BigInteger value) {
            this.value = value;
        }

        public BigInteger getValue() {
            return value;
        }

        public Expression.Constant getConstant() {
            return new Expression.Constant(Expression.SORT_INTEGER, value);
        }

        @Override
        public String toString() {
            return "<num,\"" + value + "\">";
        }

        @Override
        public int hashCode() {
            return 2;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof Number;
        }
    }

    public static class Identifier implements Token {

        private final String name;

        public Identifier(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "<id,\"" + name + "\">";
        }

        @Override
        public int hashCode() {
            return 3;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof Identifier;
        }

    }

    public static class EOF implements Token {

        private EOF() {
        }

        @Override
        public String toString() {
            return "eof";
        }
    }

    public static class Error implements Token {

        private final String message;
        
        public Error(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Error: " + message;
        }
    }

    public static class StringToken implements Token {
        
        private final String string;

        public StringToken(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return "<string,\"" + string + "\">";
        }
        
    }

}
