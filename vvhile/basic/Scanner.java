package vvhile.basic;

import java.math.BigInteger;

/**
 *
 * @author markus
 */
public class Scanner {

    private final String input;
    private int pos;
    private char next;
    private int start;
    private int lines;
    private int lineStart;

    public Scanner(String input) {
        this.input = input;
        this.pos = -1;
        this.lines = 1;
        this.lineStart = 0;
    }

    public int getLine() {
        return lines;
    }

    public int getPositionInLine() {
        return pos - lineStart;
    }

    public Token nextToken() {
        if (!nextCharacter(true)) {
            return Token.EOF;
        }
        start = pos;
        switch (next) {
            case 's':
                return scanSkip();
            case 'i':
                return scanIf();
            case 'e':
                return scanElse();
            case 'w':
                return scanWhile();
            case 't':
                return scanTrue();
            case 'f':
                return scanFalse();
            case 'a':
            case 'b':
            case 'd':
            case 'g':
            case 'h':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 'u':
            case 'v':
            case 'x':
            case 'y':
            case 'z':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
                return scanIdentifier();
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return scanNumber();
            case '(':
                return Token.L_PAREN;
            case ')':
                return Token.R_PAREN;
            case ';':
                return Token.SEMICOLON;
            case '{':
                return Token.L_CURLY;
            case '}':
                return Token.R_CURLY;
            case ':':
                if (pos + 1 >= input.length()) {
                    throw new ScanException("Unexpeced end of file", '=');
                }
                switch (input.charAt(pos + 1)) {
                    case '=':
                        next = input.charAt(++pos);
                        return Token.ASSIGN;
                    case ':':
                        next = input.charAt(++pos);
                        return Token.TYPE;
                    default:
                        throw new ScanException("Illegal character: " + next, '=');
                }
            case '|':
                return Token.OR;
            case '<':
                if (pos + 1 >= input.length()) {
                    return Token.LESS_THAN;
                }
                switch (input.charAt(pos + 1)) {
                    case '=':
                        next = input.charAt(++pos);
                        return Token.LESS_EQUAL;
                    case '-':
                        next = input.charAt(++pos);
                        return Token.IMPLIED_BY;
                    default:
                        return Token.LESS_THAN;
                }
            case '=':
                return Token.EQUALS;
            case '>':
                if (pos + 1 >= input.length()) {
                    return Token.GREATER_THAN;
                }
                if (input.charAt(pos + 1) == '=') {
                    next = input.charAt(++pos);
                    return Token.GREATER_EQUAL;
                } else {
                    return Token.GREATER_THAN;
                }
            case '+':
                return Token.PLUS;
            case '-':
                if (pos + 1 >= input.length()) {
                    return Token.MINUS;
                }
                if (input.charAt(pos + 1) == '>') {
                    next = input.charAt(++pos);
                    return Token.MAPSTO;
                } else {
                    return Token.MINUS;
                }
            case '*':
                return Token.TIMES;
            case '&':
                return Token.AND;
            case '!':
                return Token.NOT;
            case ',':
                return Token.COMMA;
            case '~':
                return Token.UNKNOWN;
            case '^':
                return Token.FORALL;
            case '?':
                return Token.EXISTS;
            case '.':
                if (pos + 2 >= input.length()) {
                    return Token.DOT;
                }
                if (input.charAt(pos + 1) == '.' && input.charAt(pos + 2) == '.') {
                    pos += 2;
                    next = input.charAt(pos);
                    return Token.DOTS;
                } else {
                    return Token.DOT;
                }
            default:
                throw new ScanException("Illegal character: " + next);
        }
    }

    /*
     * Symbols that are alowed to follow identifiers without whitespace
     */
    private boolean isSymbolChar(char c) {
        switch (c) {
            case '(':
            case ')':
            case ';':
            case '{':
            case '}':
            case ':':
            case '<':
            case '=':
            case '>':
            case '+':
            case '-':
            case '*':
            case '&':
            case '|':
            case '!':
            case '~':
            case '^':
            case '?':
            case '.':
            case '_':
                return true;
            default:
                return false;
        }
    }

    private Token scanSkip() {
        if (pos + 3 >= input.length()) {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'k') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'i') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'p') {
            return scanIdentifier();
        }
        if (pos + 1 >= input.length()
                || isSymbolChar(input.charAt(pos + 1))
                || Character.isWhitespace(input.charAt(pos + 1))) {
            return Token.SKIP;
        }
        return scanIdentifier();
    }

    private Token scanIf() {
        if (pos + 1 >= input.length()) {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'f') {
            return scanIdentifier();
        }
        if (pos + 1 >= input.length()
                || isSymbolChar(input.charAt(pos + 1))
                || Character.isWhitespace(input.charAt(pos + 1))) {
            return Token.IF;
        }
        return scanIdentifier();
    }

    private Token scanElse() {
        if (pos + 3 >= input.length()) {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'l') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 's') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'e') {
            return scanIdentifier();
        }
        if (pos + 1 >= input.length()
                || isSymbolChar(input.charAt(pos + 1))
                || Character.isWhitespace(input.charAt(pos + 1))) {
            return Token.ELSE;
        }
        return scanIdentifier();
    }

    private Token scanWhile() {
        if (pos + 4 >= input.length()) {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'h') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'i') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'l') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'e') {
            return scanIdentifier();
        }
        if (pos + 1 >= input.length()
                || isSymbolChar(input.charAt(pos + 1))
                || Character.isWhitespace(input.charAt(pos + 1))) {
            return Token.WHILE;
        }
        return scanIdentifier();
    }

    private Token scanTrue() {
        if (pos + 3 >= input.length()) {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'r') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'u') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'e') {
            return scanIdentifier();
        }
        if (pos + 1 >= input.length()
                || isSymbolChar(input.charAt(pos + 1))
                || Character.isWhitespace(input.charAt(pos + 1))) {
            return Token.TRUE;
        }
        return scanIdentifier();
    }

    private Token scanFalse() {
        if (pos + 4 >= input.length()) {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'a') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'l') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 's') {
            return scanIdentifier();
        }
        next = input.charAt(++pos);
        if (next != 'e') {
            return scanIdentifier();
        }
        if (pos + 1 >= input.length()
                || isSymbolChar(input.charAt(pos + 1))
                || Character.isWhitespace(input.charAt(pos + 1))) {
            return Token.FALSE;
        }
        return scanIdentifier();
    }

    private Token scanIdentifier() {
        do {
            switch (next) {
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    next = input.charAt(pos - 1);
                    return new Token.Identifier(input.substring(start, pos--));
            }
        } while (nextCharacter(false));
        return new Token.Identifier(input.substring(start, pos + 1));
    }

    private Token scanNumber() {
        while (nextCharacter(false)) {
            switch (next) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    next = input.charAt(pos - 1);
                    return new Token.Number(
                            new BigInteger(input.substring(start, pos--)));
            }
        }
        return new Token.Number(
                new BigInteger(input.substring(start, pos + 1)));
    }

    private boolean nextCharacter(boolean skipWhitespaces) {
        while (pos + 1 < input.length()) {
            next = input.charAt(++pos);
            if (next == '\n') {
                lines++;
                lineStart = pos;
            }
            if (!skipWhitespaces || !Character.isWhitespace(next)) {
                return true;
            }
        }
        return false;
    }

}
