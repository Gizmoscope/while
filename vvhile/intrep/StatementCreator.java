package vvhile.intrep;

/**
 * A statement creator is a function that turns a list of tokens, expressions
 * and statements into a statment. Usually, a statement creator will ignore all
 * tokens and only use that given expressions and statements.
 *
 * @author markus
 */
public interface StatementCreator {

    /**
     * Takes a list of arguments and turns them into a statement.
     *
     * @param args list of arguments
     * @return a statement
     */
    Statement create(ASTElement... args);

}
