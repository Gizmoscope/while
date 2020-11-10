package vvhile.intrep;

import java.util.Map;

/**
 * An AST-element-builder builds a new AST-element out of a sequence of given 
 * AST-elements. The AST-compiler reads AST-elements from the stack and passes
 * them to a given AST-element-builder as long as those elements fit according to
 * the builders fits()-method. Since the elements come from a stack they usually 
 * appear in reverse order. One should be aware of this when designing a custom
 * AST-element-builder.
 * 
 * @author markus
 */
public interface ASTElementBuilder {

    /**
     * Pass an AST-Element to the builder. The builder may assume that the given
     * element fits as defined by the fits()-method.
     * 
     * @param element 
     */
    public void put(ASTElement element);
    
    /**
     * Build a new AST-Element from the information that was passed to the builder
     * before. The builder might need global information about variables or wether
     * its output should show parentheses or not.
     * 
     * @param vars global set variables
     * @param parentheses result should show parantheses
     * @return new AST-Element
     */
    public ASTElement build(Map<String, Expression.Variable> vars, boolean parentheses);

    /**
     * Says wether a given AST-Element fits the internal pattern of the builder.
     * Wether an element fits or not usually depends on the (number of) elements
     * already passed to th builder via the put()-method.
     * 
     * @param peek
     * @return 
     */
    public boolean fits(ASTElement peek);
    
}
