package vvhile.frontend;

import vvhile.intrep.ASTElement;

/**
 * Tokens are the atomic objects a parser deals with. A token itself might
 * consist of several characters but they are regarded as one unit.
 *
 * @author markus
 */
public interface Token extends Variable, Parser.ParseObject, ASTElement {

    /**
     * The end-of-file token. This is a singleton object.
     */
    public static final EOF EOF = new EOF();

    /**
     * Symbols are fixed predefined tokens. They have a fixed string
     * representation.
     */
    public static class Symbol implements Token {

        private final String symbol;

        /**
         * Creates a new symbol token with the given string representation.
         * 
         * @param symbol string representing the symbol
         */
        public Symbol(String symbol) {
            this.symbol = symbol;
        }

        /**
         * @return the string representation of the symbol
         */
        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    /**
     * Identifier are tokens that represent the names of variables. They are
     * parsed flexibly.
     */
    public static class Identifier implements Token {

        private final String name;

        /**
         * Creates a new identifier with the given name.
         * 
         * @param name Name of the identifier
         */
        public Identifier(String name) {
            this.name = name;
        }

        /**
         * @return the name of the identifier
         */
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

        /**
         * All identifiers are considered equal. As a token we only care if it 
         * is an identifier or not. 
         * 
         * @param obj any object
         * @return true if the object is also an identifier
         */
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Identifier;
        }

    }

    /**
     * The end-of-file token. It usually is the last token read by the parser.
     * There is only one singleton of this class: <code> Token.EOF </code>.
     */
    public static class EOF implements Token {

        private EOF() {
        }

        @Override
        public String toString() {
            return "eof";
        }
    }

    /**
     * The scanner can pass an error token to notify the parser of a problem.
     */
    public static class Error implements Token {

        private final String message;

        /**
         * Creates a new error token with a message describing the problem.
         * 
         * @param message error message
         */
        public Error(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "Error: " + message;
        }
    }

}
