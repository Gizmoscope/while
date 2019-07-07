package frontend;

/**
 *
 * @author markus
 */
public class NonTerminal implements Variable {
    
    private final String name;

    public NonTerminal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
    
}
