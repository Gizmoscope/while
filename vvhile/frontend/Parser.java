package vvhile.frontend;

import vvhile.frontend.Scanner.ScanObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import vvhile.basic.language.BasicRuleSet;
import static vvhile.frontend.Token.EOF;
import vvhile.intrep.ASTCompiler;
import vvhile.intrep.ASTElement;
import vvhile.intrep.Expression;
import vvhile.util.BoundedBuffer;

/**
 * A parser reads tokens from a scanner and matches them agains a grammar. While
 * doing so it determines the rules of the grammar that verify the token
 * sequence as a word of the grammar. All tokens and the rules are passed into a
 * stream that feeds a parsing tree.
 *
 * @author markus
 */
public class Parser {

    // The "word" contains the variables that are matched by the read tokens
    // This stack builds up as non-terminals are replaced by the right-hand-side
    // of a matching rule and build down as the parser reads matching tokens.
    private final Stack<Variable> word;
    // the token stream provided by the scanner
    private final BoundedBuffer<ScanObject> tokenStream;
    // the parsing table representing a grammar
    private final ParsingTable parsingTable;

    // the last token read from the scanner
    private Token nextToken;
    // the list of events occouring while parsing
    private final List<Message> messages;
    // thread object
    private Thread thread;

    /**
     * Creates a new parser that is connected to a scanner via a stream. The
     * parser parses its inputs against the rules of a grammar specified by a
     * set of rules.
     *
     * @param tokenStream stream from scanner
     * @param ruleSet grammar
     * @param start the start symbol of the grammar
     */
    public Parser(BoundedBuffer<ScanObject> tokenStream, RuleSet ruleSet, NonTerminal start) {
        this.tokenStream = tokenStream;
        this.parsingTable = Grammar.generateParsingTable(ruleSet, start);
        this.messages = new LinkedList<>();
        this.word = new Stack<>();
    }

    /**
     * Parses the token stream and passes them and the deduced rules to the
     * buffer. This creates a new thread. The parsing ends after reaching the
     * end of file or if a complete word was read from the grammar. The parser
     * then passes Token.EOF to the buffer.
     *
     * @param start the start symbol of the grammar
     * @param buffer A bounded buffer
     */
    public void startParsing(NonTerminal start, BoundedBuffer<ParseObject> buffer) {
        (thread = new Thread(() -> {
            parse(start, buffer);
            messages.stream().forEach(message -> System.out.println(message));
            thread = null;
        }, "Parser")).start();
    }

    /*
     * Reads from the token stream and matches the tokens against the grammar
     * given by the parsing table starting with the specified start symbol.
     * The passed data (= matching rules + tokens) are passed to the buffer feeding
     * the parsing tree.
     */
    private boolean parse(NonTerminal start, BoundedBuffer<ParseObject> buffer) {
        // initialize word, use start symbol of the grammar
        word.add(start);
        // read from scanner
        ScanObject scanObject = tokenStream.get();
        nextToken = scanObject.getToken();
        // read from scanner as long as there are next tokens and no error accours
        while (true) {
            // there is no non-terminal to which a rule could be applied
            if (word.isEmpty()) {
                messages.add(new Message(Message.MESSAGE, "Traversion of the parse tree is finished!",
                        scanObject.getLine(), scanObject.getPositionInLine()));
                // Good case: scanner reached the end-of-file token
                if (EOF.equals(nextToken)) {
                    buffer.put(EOF);
                    return true;
                } // Bad case: there are still unparsed tokens
                else {
                    messages.add(new Message(Message.WARNING, "Didn't reach the end of the imput!",
                            scanObject.getLine(), scanObject.getPositionInLine()));
                    return false;
                }
            } // the current word starts with a token
            else if (word.peek() instanceof Token) {
                // match the token with the read one
                if (word.peek().equals(nextToken)) {
                    // Success: the read token matches the grammar, pass it to the parse tree
                    word.pop();
                    buffer.put(nextToken);
                    // read from scanner
                    scanObject = tokenStream.get();
                    nextToken = scanObject.getToken();
                } else {
                    // Failed: mismatch
                    messages.add(new Message(Message.ERROR, "Wrong token! \n   Read \""
                            + nextToken + "\", but expected \""
                            + (word.peek() instanceof Token ? word.peek()
                            : parsingTable.getPossibleTokensFor((NonTerminal) word.peek())) + "\"",
                            scanObject.getLine(), scanObject.getPositionInLine()));
                    return false;
                }
            } // The current word is a non-terminal
            else if (word.peek() instanceof NonTerminal) {
                // replace the non-terminal by the rule that matches the read token
                NonTerminal nonTerminal = (NonTerminal) word.pop();
                Rule rule = parsingTable.getRule(nonTerminal, nextToken);
                // if there isn't any, the input does not match the grammar
                if (rule == null) {
                    messages.add(new Message(Message.ERROR, "No applicable Rule found!\n"
                            + "   Expected " + parsingTable.getPossibleTokensFor(nonTerminal) + "\n"
                            + "   but next Token is \"" + nextToken + "\".\n"
                            + "   Current Nonterminal is " + nonTerminal + ".",
                            scanObject.getLine(), scanObject.getPositionInLine()));
                    return false;
                }
                // pass the rule to the parse tree
                buffer.put(rule);
                Variable[] rhs = rule.getRhs();
                // append the right-hand-side of the rule to the word in reverse order
                // therefore the first element is the top-most
                for (int i = rhs.length - 1; i >= 0; i--) {
                    word.add(rhs[i]);
                }
            }
        }
    }

    /**
     * Wait for parser to finish parsing.
     */
    public void join() {
        while (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
            }
        }
    }

    public static Expression parseExpression(String expression) {
        RuleSet grammar = new BasicRuleSet();

        Scanner scanner = Scanner.getDefaultScanner(expression);

        BoundedBuffer<Scanner.ScanObject> scannerToParser = new BoundedBuffer<>(32);
        scanner.startScan(scannerToParser);

        Parser parser = new Parser(scannerToParser, grammar, BasicRuleSet.EXPR);
        BoundedBuffer<Parser.ParseObject> parserToParseTree = new BoundedBuffer<>(32);
        parser.startParsing(BasicRuleSet.EXPR, parserToParseTree);

        ParseTree parseTree = new ParseTree(parserToParseTree);
        BoundedBuffer<Parser.ParseObject> parseTreeToASTCompiler = new BoundedBuffer<>(32);
        parseTree.startRetranslation(parseTreeToASTCompiler);

        ASTCompiler astCompiler = new ASTCompiler(parseTreeToASTCompiler, grammar);
        BoundedBuffer<Parser.ParseObject> astCompilerToInterpreter = new BoundedBuffer<>(32);
        astCompiler.startCompilation(astCompilerToInterpreter);

        scanner.join();
        parser.join();
        parseTree.join();
        astCompiler.join();

        ASTElement result = astCompiler.getRoot();
        if (result instanceof Expression) {
            return (Expression) result;
        } else {
            throw new ParseException("The given String does not represent an expression");
        }
    }

    /**
     * While parsing the parser saves messages containing information about the
     * parsing process.
     */
    public static class Message {

        public static final int MESSAGE = 0;
        public static final int WARNING = 1;
        public static final int ERROR = 2;

        private final int type;
        private final String message;
        private final int line;
        private final int positionInLine;

        /**
         * Creates a new message with the given message and data.
         *
         * @param type type of the message
         * @param message content of the message
         * @param line line in which the event occoured
         * @param positionInLine position in line in which the event occoured
         */
        public Message(int type, String message, int line, int positionInLine) {
            this.type = type;
            this.message = message;
            this.line = line;
            this.positionInLine = positionInLine;
        }

        /**
         * @return type of the message
         */
        public int getType() {
            return type;
        }

        /**
         * @return content of the message
         */
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            String t;
            switch (type) {
                case MESSAGE:
                    t = "";
                    break;
                case WARNING:
                    t = "[WARN]";
                    break;
                case ERROR:
                    t = "[ERR]";
                    break;
                default:
                    t = "[UNKNOWN]";
            }
            return "line " + line + " pos " + positionInLine + " " + t + ": " + message;
        }

    }

    /**
     * Parse objects are the objects that the parser passes to the parse tree.
     * Those objects are tokens and rules.
     */
    public static interface ParseObject {

    }

}
