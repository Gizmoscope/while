package vvhile.basic;

/**
 *
 * @author markus
 */
public class Configuration {

    private final Statement program;
    private final State state;

    public Configuration(Statement program, State state) {
        this.program = program;
        this.state = state;
    }

    public Statement getProgram() {
        return program;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return program == null ? state.toString() : "〈" + program + ", ...〉";
    }

}
