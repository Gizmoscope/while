package vvhile.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import static vvhile.basic.Token.EOF;

/**
 *
 * @author markus
 */
public class Parser {

    private Stack<Variable> word;
    private final Scanner scanner;
    private final ParsingTable parsingTable;

    private Token nextToken;

    private final List<Message> messages;
    private ParseTree parseTree;

    public Parser(Scanner scanner, ParsingTable parsingTable) {
        this.scanner = scanner;
        this.parsingTable = parsingTable;
        this.messages = new LinkedList<>();
    }

    public boolean parseExpression() {
        return parse(BasicRuleSet.EXPR);
    }

    public boolean parseProgram() {
        return parse(BasicRuleSet.STM);
    }

    private boolean parse(NonTerminal start) {
        word = new Stack<>();
        word.add(start);
        parseTree = new ParseTree();
        nextToken = scanner.nextToken();
        while (true) {
            if (word.isEmpty()) {
                messages.add(new Message(Message.MESSAGE, "Traversion of the parse tree is finished!",
                        scanner.getLine(), scanner.getPositionInLine()));
                if (EOF.equals(nextToken)) {
                    return true;
                } else {
                    messages.add(new Message(Message.WARNING, "Didn't reach the end of the imput!",
                            scanner.getLine(), scanner.getPositionInLine()));
                    return false;
                }
            }
            if (word.peek() instanceof Token) {
                if (word.peek().equals(nextToken)) {
                    word.pop();
                    parseTree.addToken(nextToken);
                    nextToken = scanner.nextToken();
                } else {
                    messages.add(new Message(Message.ERROR, "Wrong token! \n   Read \""
                            + nextToken + "\", but expected \""
                            + (word.peek() instanceof Token ? word.peek()
                                    : parsingTable.getPossibleTokensFor((NonTerminal) word.peek())) + "\"",
                            scanner.getLine(), scanner.getPositionInLine()));
                    return false;
                }
            } else if (word.peek() instanceof NonTerminal) {
                NonTerminal nonTerminal = (NonTerminal) word.pop();
                Rule rule = parsingTable.getRule(nonTerminal, nextToken);
                if (rule == null) {
                    messages.add(new Message(Message.ERROR, "No applicable Rule found! \n   Current Nonterminal is "
                            + nonTerminal + " and next Token is \"" + nextToken + "\".",
                            scanner.getLine(), scanner.getPositionInLine()));
                    return false;
                }
                parseTree.addRule(rule);
                Variable[] rhs = rule.getRhs();
                for (int i = rhs.length - 1; i >= 0; i--) {
                    word.add(rhs[i]);
                }
            }
        }
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public static Expression compileExpression(String code) {
        Collection<Rule> rules = BasicRuleSet.BASIC_RULES;

        ParsingTable table = BasicRuleSet.generateParsingTable(
                BasicRuleSet.BASIC_RULES,
                BasicRuleSet.BASIS_NONTERMINALS,
                BasicRuleSet.BASIC_TERMINALS
        );
        Parser parser = new Parser(new Scanner(code), table);
        parser.parseExpression();
        return ToASTCompiler.compileExpression(parser.getParseTree());
    }

    public static Statement compileProgram(String code) {
        Collection<Rule> rules = BasicRuleSet.BASIC_RULES;

        ParsingTable table = BasicRuleSet.generateParsingTable(
                BasicRuleSet.BASIC_RULES,
                BasicRuleSet.BASIS_NONTERMINALS,
                BasicRuleSet.BASIC_TERMINALS
        );
        Parser parser = new Parser(new Scanner(code), table);
        parser.parseProgram();
        return ToASTCompiler.compileStatement(parser.getParseTree());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Expression.Constant.showSorts = false;
        Expression.Variable.showSorts = false;
        String code
                = "n := 8;"
                + "m := 9;"
                + "x := n + m";
        Scanner scanner = new Scanner(code);
        Parser parser = new Parser(scanner,
                BasicRuleSet.generateParsingTable(
                        BasicRuleSet.BASIC_RULES,
                        BasicRuleSet.BASIS_NONTERMINALS,
                        BasicRuleSet.BASIC_TERMINALS
                )
        );
        boolean parsed = parser.parseProgram();
        if (parsed) {
            System.out.println("  SUCCESS");
            ParseTree parseTree = parser.getParseTree();
            Statement compiledCode = ToASTCompiler.compileStatement(parseTree);

//            System.out.println(new ASTToTextExporter(4, false).getText(compiledCode));
//            System.out.println();

            Configuration conf = new Configuration(compiledCode, new State());
            while(conf.getProgram() != null) {
                conf = conf.getProgram().run(conf.getState());
            }

            System.out.println(conf.getState());
        } else {
            System.out.println("  FAIL");
            parser.messages.forEach(m -> System.out.println(m));
        }
    }

    private static <T> Collection<T> unite(Collection<T>... cs) {
        Collection<T> united = new HashSet<>();
        for (Collection<T> c : cs) {
            united.addAll(c);
        }
        return united;
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

}
