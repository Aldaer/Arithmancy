package arithmancy;

import javax.naming.OperationNotSupportedException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Exception thrown when calculating expression with unset variables.
 */
class VariableNotSet extends RuntimeException {
    VariableNotSet(String varName) {
        super("Variable not set: " + varName);
    }
}

/**
 * Variable to use in expressions (such as x in "2x+1"). You need not not create instances directly.
 * Variables are generated automatically with ExpressionParser.parse. To get list of variables your expression depends on,
 * use Expression.dependsOnVariables().
 */
public class Variable implements Expression {
    private final String name;
    private Double val;

    Variable(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    /**
     * @return Value of the variable
     * @throws VariableNotSet when variable not set
     */
    @Override
    public double calculate() throws VariableNotSet {
        if (val == null) throw new VariableNotSet(name);
        return val;
    }

    @Override
    public String toLispString() {
        return name;
    }

    @Override
    public Set<Variable> dependsOnVariables() {
        HashSet<Variable> thisv = new HashSet<>();
        thisv.add(this);
        return thisv;
    }

    /**
     * Sets the value of this variable.
     * @param value Value to be used in calculate()
     */
    public void setValue(double value) {
        val = value;
    }

    /**
    * Unsets the value of this variable. <p>
    * Variable must be set again with setValue(). Otherwise calculate() will throw VariableNotSet().
    */
    public void unsetValue() { val = null; }

    public boolean set() {
        return val != null;
    }
}
