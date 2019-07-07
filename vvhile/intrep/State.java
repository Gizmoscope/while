package vvhile.intrep;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author markus
 */
public class State {

    private final Map<Expression.Variable, Object> state;

    public State() {
        state = new HashMap<>();
    }

    public Object getValueFor(Expression.Variable variable) {
        return state.get(variable);
    }

    public State substitute(Object value, Expression.Variable variable) {
        state.put(variable, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        state.keySet().stream().forEach((variable) -> {
            Object value = state.get(variable);
            builder.append(variable).append(" = ").append(value == null? "~" : value).append("\n");
        });
        return builder.toString();
    }

}
