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
    public String toString() {
        return Double.toString(val);
    }

    @Override
    public Set<String> dependsOnVariables() {
        return new HashSet<>();
    }

    @Override
    public boolean complete() {
        return true;
    }
}

class NamedConstant extends Constant {
    private final String name;        // Used only by toString and toLispString functions

    NamedConstant(double value, String name) {
        super(value);
        this.name = name;
    }

    @Override
    public String toLispString() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
