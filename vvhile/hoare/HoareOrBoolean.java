package vvhile.hoare;

/**
 * Exactly the classes HoareTriple and BooleanFormular implement this interface.
 * 
 * @author markus
 */
public interface HoareOrBoolean {

    /**
     * @param latex true if the return string should represent LaTeX code
     * @return a string representation
     */
    public String toString(boolean latex);
    
}
