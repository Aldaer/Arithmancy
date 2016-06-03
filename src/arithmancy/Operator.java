package arithmancy;

import java.util.Iterator;
import java.util.Set;

/**
 * Operator in calculable expressions.  Implement calculate() to provide your own operators. Use leftOperand() and rightOperand() as operands of your operator.
 */
abstract class Operator implements Expression, Cloneable {      // Operators implement prototype design pattern.
                                                                // Operators in expression are cloned from

    enum Kind { UNARY, BINARY }

    /** Operator precedences. Must be declared in ascending order
     */
    enum Precedence { ADD(1), MUL(10), FUNC(20), POW(30);

        @Deprecated
        public int ord() {
            return val;
        }

        Precedence(int val) {
            this.val = val;
        }

        private int val;

        /**
         * Iterator to iterate precedences from highest to lowest.<br>
         * Please note that precedences are iterated highest to lowest BY DEFAULT
         * @return Backwards iterator
         */
    }
    final Kind kind;
    final Precedence prec;

    String token;                   // Gets set when operator is added to the collection of known operators.
                                    // Used ONLY in toLispString()

    protected double argument() {
        return rightOperand.calculate();
    }

    protected double rightOperand() {
        return rightOperand.calculate();
    }

    protected double leftOperand() {
        return leftOperand.calculate();
    }

    boolean incomplete() {
        return rightOperand == null;
    }

    // Always NULL in knownOps, get set it cloned versions
    Expression leftOperand;
    Expression rightOperand;

    Operator(Kind kind, Precedence precedence) {
        this.kind = kind;
        this.prec = precedence;
    }

    @Override
    public Set<Variable> dependsOnVariables() {
        switch (kind) {
            case UNARY:
                return rightOperand.dependsOnVariables();
            case BINARY:
                Set<Variable> r = rightOperand.dependsOnVariables();
                r.addAll(leftOperand.dependsOnVariables());
                return r;
        }
        throw new InvalidOperatorKind(token);
    }

    @Override
    public Operator clone() {
        try {
            return (Operator)super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);           // Operator cannot be cloned = VERY STRANGE
        }

    }

    @Override
    public String toLispString() {
       switch (kind) {
           case UNARY:
               return token + '(' + rightOperand.toLispString() + ')';
           case BINARY:
               return token + '(' + leftOperand.toLispString() + ',' + rightOperand.toLispString() + ')';
       }
        throw new InvalidOperatorKind(token);
    }


}
