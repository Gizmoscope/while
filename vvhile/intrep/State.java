package vvhile.intrep;

import java.util.HashMap;
import java.util.Map;

/**
 * An object of the State class represents the current state of a program 
 * execution. It is the assigment of the current values to the variables of the
 * program.
 * 
 * @author markus
 */
public class State {

    // Assignment of values to variables
    private final Map<Expression.Variable, Object> state;

    public State() {
        state = new HashMap<>();
    }

    /**
     * Returns the current value for the given variable in this state.
     * 
     * @param variable a variable
     * @return current value of that variable in this state
     */
    public Object getValueFor(Expression.Variable variable) {
        return state.get(variable);
    }

    /**
     * Assigns a new value to a variable.
     * 
     * @param value new value
     * @param variable a variable
     * @return the state this method is called upon with updated variable
     */
    public State substitute(Object value, Expression.Variable variable) {
        state.put(variable, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        state.keySet().stream().forEach((variable) -> {
            Object value = state.get(variable);
            builder
                    .append(variable)
                    .append(" = ")
                    .append(value == null? "~" : value)
                    .append("\n");
        });
        return builder.toString().trim();
    }

}
