package vvhile.intrep;

import java.util.Map;
import vvhile.frontend.Token;

/**
 *
 * The statement builder uses a pattern, corresponding to the right hand side of
 * a rule, to match it with AST-elements.
 *
 * @author markus
 */
public class StatementBuilder implements ASTElementBuilder {

    private final StatementCreator creator;
    private final Object[] pattern;
    private final ASTElement[] args;
    private int insertAt;

    public StatementBuilder(StatementCreator creator, Object... pattern) {
        this.creator = creator;
        this.pattern = pattern;
        args = new ASTElement[pattern.length];
        // Everthing is inserted backwards: Start at the end.
        insertAt = pattern.length - 1;
    }

    @Override
    public void put(ASTElement element) {
        // Begin at the end of the list of arguments
        args[insertAt--] = element;
    }

    @Override
    public ASTElement build(Map<String, Expression.Variable> vars, boolean parentheses) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Token.Identifier) {
                // Replace identifier with the variables they represent.
                Token.Identifier identifier = (Token.Identifier) args[i];
                if (vars.containsKey(identifier.getName())) {
                    // Variable already exists, take it.
                    args[i] = vars.get(identifier.getName());
                } else {
                    // Otherwise, create a new one.
                    args[i] = new Expression.Variable(Expression.SORT_UNKNOWN, identifier.getName());
                }
            }
        }
        // Use the creator to create the statement from the arguments
        Statement statement = creator.create(args);
        // reset the builder
        insertAt = pattern.length - 1;
        return statement;
    }

    @Override
    public boolean fits(ASTElement peek) {
        if (insertAt < 0) {
            // Pattern already full
            return false;
        } else if (pattern[insertAt] instanceof Token) {
            // Token can be checked easily, they will be dropped anyway
            return pattern[insertAt].equals(peek);
        } else if (pattern[insertAt] instanceof Class) {
            // Check if a substatement is an instance of the demanded class
            return ((Class) pattern[insertAt]).isInstance(peek);
        } else {
            return false;
        }
    }

}
