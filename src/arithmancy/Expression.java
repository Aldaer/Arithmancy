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
    double calculate();

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
     * You can iterate through the returned set and call ExpressionParser.setVariable() to assign/unassign values to the vars.
     * @return Set of variable names or an empty set
     */
    Set<String> dependsOnVariables();

    /**
     * Checks if the expression is complete, that is, has no lack of operands.<br>
     * For example, "x", "2.0" and "a + b" are complete, whereas "*", "1 + ln" and "* a" are not.
     * @return If this expression is compete
     */
    boolean complete();
}

