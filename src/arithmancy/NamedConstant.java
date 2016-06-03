package arithmancy;

import java.util.HashSet;
import java.util.Set;

class Constant implements Expression {
    private final double val;
    Constant(double value) {
        val = value;
    }

    @Override
    public double calculate() {
        return val;
    }

    @Override
    public String toLispString() {
        return Double.toString(val);
    }

    @Override
    public Set<Variable> dependsOnVariables() {
        return new HashSet<Variable>();
    }
}