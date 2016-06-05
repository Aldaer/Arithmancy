package arithmancy;

import java.util.HashSet;
import java.util.Set;

class Constant implements Expression {
    private final Double val;
    Constant(Double value) {
        val = value;
    }

    @Override
    public Double calculate() {
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
        return new HashSet<String>();
    }
}

class NamedConstant extends Constant {
    private String name;        // Used only by toString and toLispString functions

    NamedConstant(Double value, String name) {
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
