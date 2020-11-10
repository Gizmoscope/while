package vvhile.hoare;

/**
 * A Hoare axiom is a special Hoare rule, namely one without premises.
 * 
 * @author markus
 */
public abstract class Axiom extends Rule {

    /**
     * If a Hoare axiom is applied to a Hoare triple the list of premises is
     * always empty.
     * 
     * @param triple a Hoare triple
     * @return empty array if the axiom is applicable
     */
    @Override
    public HoareTriple[] apply(HoareTriple triple) {
        if (applicable(triple)) {
            return new HoareTriple[0];
        } else {
            throw new IllegalArgumentException("This Axiom is not applicable to the given Hoare triple.");
        }
    }

}
