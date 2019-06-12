package vvhile.basic.hoare;

public abstract class Axiom extends Rule {

    @Override
    public HoareTriple[] apply(HoareTriple triple) {
        if (applicable(triple)) {
            return new HoareTriple[0];
        } else {
            throw new IllegalArgumentException("This Rule is not applicable to the given Hoare triple.");
        }
    }

}
