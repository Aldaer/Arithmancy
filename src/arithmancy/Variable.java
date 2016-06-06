package arithmancy;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Exception thrown when calculating expression with unset variables.
 */
class VariableNotSetException extends RuntimeException {
    VariableNotSetException (String varName) {
        super("Variable not set: " + varName);
    }
}

class UnknownVariableException extends RuntimeException {
    UnknownVariableException(String name) {
        super("Unknown variable: " + name);
    }
}

/**
 * Variable to use in expressions (such as x in "2x+1"). You need not not create instances directly.
 * Variables are generated automatically with ExpressionParser.parse. To get list of variables your expression depends on,
 * use Expression.dependsOnVariables().
 */
class Variable implements Expression {
    private final String name;              // Used ONLY in toString() and toLispString()
    private Double val;                     // null if not set

    Variable(String name) {
        this.name = name;
    }

    String getName() { return name; }

    /**
     * @return Value of the variable
     * @throws VariableNotSetException when variable not set
     */
    @Override
    public double calculate() throws VariableNotSetException {
        if (val == null) throw new VariableNotSetException(name);
        return val;
    }

    @Override
    public String toLispString() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }


    @Override
    public Set<String> dependsOnVariables() {
        HashSet<String> thisv = new HashSet<>();
        thisv.add(this.name);
        return thisv;
    }

    @Override
    public boolean complete() {
        return true;
    }

    Optional<Double> getValueOrEmpty() {
        return Optional.ofNullable(val);
    }

    /**
     * Return value of the variable, null if not set
     */
    Double getValueOrNull() {
        return val;
    }

    /**
     * Sets the value of this variable.
     * @param value Value to be used in calculate()
     */
    void setValue(double value) {
        val = value;
    }

    /**
    * Unsets the value of this variable. <p>
    * Variable must be set again with setValue(). Otherwise calculate() will throw VariableNotSet().
    */
    void unsetValue() { val = null; }

    /**
     * Returns true is the var is set
     */
    boolean isSet() {
        return val != null;
    }
}
