package vvhile.basic.hoare;

/**
 *
 * @author markus
 */
public abstract class Rule {
    
    public abstract boolean applicable(HoareTriple triple);
    
    public abstract HoareOrBoolean[] apply(HoareTriple triple);
    
    public abstract String getShortName();
    
}
