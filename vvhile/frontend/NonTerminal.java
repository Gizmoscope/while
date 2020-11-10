package vvhile.frontend;

/**
 * A non-terminal is a variable that can be replaced by a sequence of other
 * variables according to a given rule.
 * 
 * @author markus
 */
public class NonTerminal implements Variable {
    
    private final String name;

    /**
     * Creates a new non-terminal with the given name.
     * 
     * @param name name of the non-terminal
     */
    public NonTerminal(String name) {
        this.name = name;
    }

    /**
     * @return name of the non-terminal
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
    
}
