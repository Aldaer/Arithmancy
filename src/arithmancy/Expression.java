package arithmancy;

import java.util.Set;

/**
 * Generic interface for anything that has a calculable value.
 */
public interface Expression {
    /**
     * Returns the result of calculation
     * @return Result of calculation
     */
    Double calculate();

    /**
     * Returns expression represented as a string of nested operators followed by operands<br>
     * (lisp-style), i.e. (a-1)*2 == *(-(a,1),2)
     * @return String representation of the expression
     */
    String toLispString();

    /**
     * Returns expression represented as a mathematical formula. May be used to check if the parsing was correct.
     */
    @Override
    String toString();

    /**
     * Returns a list of variables this object depends on. Successful calculation requires all variables to be set.<br>
     * You can iterate through the returned set and call ExpressionParser.setVariable() to assign/unassign values to all vars.
     * @return Set of Variable objects
     */
    Set<String> dependsOnVariables();
}

