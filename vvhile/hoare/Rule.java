package vvhile.hoare;

/**
 * A Hoare rule connects a Hoare triple, the conclusion, with other Hoare triples
 * or boolean expressions, the premises. If all premises can the shown valid than
 * the conclusion is a valid Hoare triple.
 * 
 * @author markus
 */
public abstract class Rule {
    
    /**
     * Checks wether this rule can be applied to a given Hoare triple.
     * 
     * @param triple a Hoare triple
     * @return true if the rule is applicable
     */
    public abstract boolean applicable(HoareTriple triple);
    
    /**
     * Applies this rule to the given Hoare triple. It determines the premises of
     * this rule depending on the argument. This method should only be applied to
     * a Hoare triple if a prior call to the applicable method returns true.
     * 
     * @param triple a Hoare triple
     * @return the premises of this rule depending on the given argument
     */
    public abstract HoareOrBoolean[] apply(HoareTriple triple);
    
    /**
     * @return A short name describing the rule
     */
    public abstract String getShortName();
    
}
