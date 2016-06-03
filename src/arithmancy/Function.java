package arithmancy;

/**
 * Mathematical function. Equivalent to unary operator with precedence==FUNC. Implement calculate() to provide your own functions.
 * Use argument() as the argument of your function. rightOperand() is equivalent to argument(), leftOperand() throws null pointer exception.
 */
public abstract class Function extends Operator {
    Function() {
        super(Kind.UNARY, Precedence.FUNC);
    }

    @Override
    protected double leftOperand() {
        throw new RuntimeException("Cannot refer to left operand in functions", new NullPointerException());
    }
}
