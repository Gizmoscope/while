package frontend;

import frontend.Scanner2.ScanObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import static frontend.Token.EOF;
import threading.BoundedBuffer;

/**
 *
 * @author markus
 */
public class Parser2 {

    private Stack<Variable> word;
    private final BoundedBuffer<ScanObject> tokenStream;
    private final ParsingTable parsingTable;

    private Token nextToken;

    private final List<Message> messages;

    public Parser2(BoundedBuffer<ScanObject> tokenStream, ParsingTable parsingTable) {
        this.tokenStream = tokenStream;
        this.parsingTable = parsingTable;
        this.messages = new LinkedList<>();
    }

    public void startParsing(NonTerminal start, BoundedBuffer<ParseObject> buffer) {
        new Thread(() -> {
            parse(start, buffer);
        }, "Parser").start();
    }
    
    private boolean parse(NonTerminal start, BoundedBuffer<ParseObject> buffer) {
        word = new Stack<>();
        word.add(start);
        ScanObject scanObject = tokenStream.get();
        nextToken = scanObject.getToken();
        while (true) {
            if (word.isEmpty()) {
                messages.add(new Message(Message.MESSAGE, "Traversion of the parse tree is finished!",
                        scanObject.getLine(), scanObject.getPositionInLine()));
                if (EOF.equals(nextToken)) {
                    buffer.put(EOF);
                    return true;
                } else {
                    messages.add(new Message(Message.WARNING, "Didn't reach the end of the imput!",
                            scanObject.getLine(), scanObject.getPositionInLine()));
                    return false;
                }
            }
            if (word.peek() instanceof Token) {
                if (word.peek().equals(nextToken)) {
                    word.pop();
                    buffer.put(nextToken);
                    scanObject = tokenStream.get();
                    nextToken = scanObject.getToken();
                } else {
                    messages.add(new Message(Message.ERROR, "Wrong token! \n   Read \""
                            + nextToken + "\", but expected \""
                            + (word.peek() instanceof Token ? word.peek()
                            : parsingTable.getPossibleTokensFor((NonTerminal) word.peek())) + "\"",
                            scanObject.getLine(), scanObject.getPositionInLine()));
                    return false;
                }
            } else if (word.peek() instanceof NonTerminal) {
                NonTerminal nonTerminal = (NonTerminal) word.pop();
                Rule rule = parsingTable.getRule(nonTerminal, nextToken);
                if (rule == null) {
                    messages.add(new Message(Message.ERROR, "No applicable Rule found! \n   Current Nonterminal is "
                            + nonTerminal + " and next Token is \"" + nextToken + "\".",
                            scanObject.getLine(), scanObject.getPositionInLine()));
                    return false;
                }
                buffer.put(rule);
                Variable[] rhs = rule.getRhs();
                for (int i = rhs.length - 1; i >= 0; i--) {
                    word.add(rhs[i]);
                }
            }
        }
    }

    public static class Message {

        public static final int MESSAGE = 0;
        public static final int WARNING = 1;
        public static final int ERROR = 2;

        private final int type;
        private final String message;
        private final int line;
        private final int positionInLine;

        public Message(int type, String message, int line, int positionInLine) {
            this.type = type;
            this.message = message;
            this.line = line;
            this.positionInLine = positionInLine;
        }

        public int getType() {
            return type;
        }

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
    
    public static interface ParseObject {
        
    }

}
