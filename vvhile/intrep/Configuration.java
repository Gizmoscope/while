package vvhile.intrep;

/**
 * A configuration consists of a program and the state it is in. Abstractly this
 * describes a point on the execution path of a program which represents the
 * state of the program and the subprogram that is still being executed.
 *
 * @author markus
 */
public class Configuration {

    private final Statement program;
    private final State state;

    /**
     * Creates a configuration given the arguments. The program can be null. In
     * that case the configuration is call terminated.
     *
     * @param program a program
     * @param state a state
     */
    public Configuration(Statement program, State state) {
        this.program = program;
        this.state = state;
    }

    /**
     * @return the program
     */
    public Statement getProgram() {
        return program;
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @return a string representation showing the state if the configuration is
     * terminated or the program otherwise.
     */
    @Override
    public String toString() {
        return program == null
                ? state.toString()
                : "〈" + program + ", ...〉";
    }

}
