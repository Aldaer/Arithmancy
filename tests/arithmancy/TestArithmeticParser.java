package arithmancy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestArithmeticParser {
    @Before
    public void setUp() throws Exception {
        ExpressionParser.loadDefaultKnownOperators();
    }

    @Test(expected= ParsingError.class)
    public void testBeforeOperatorLoad() throws ParsingError {
        String formula = "1 + 1";
        ExpressionParser.clearKnownOperators();
        ExpressionParser.parse(formula);
    }

    @Test
    public void parseSimpleAddition() throws ParsingError {
        String formula = "1 + 1";

        Expression e = ExpressionParser.parse(formula);

        double result = e.calculate();
        Assert.assertEquals(result, 2.0, 0.00001);
    }

    @Test
    public void checkExpressionPreprocessor() {
        String test = "  A  +(b*3.44^2mgh-1xpi)* (  2.c-1) -b";
        String expected = "a +(b * 3.44 ^ 2 mgh - 1 xpi)*(2. c - 1)- b";

        String result = ExpressionParser.preprocessExpression(test);
        assertTrue(result.equals(expected));
    }

    @Test
    public void parseSimpleParentheses() throws ParsingError {

        String formula = "1 - (  1+( 2 - 4) * 2  ) + 2 * (3 -1)";

        Expression e = ExpressionParser.parse(formula);

        double result = e.calculate();
        Assert.assertEquals(result, 8.0, 0.00001);
    }

    @Test
    public void parseUnaryMinus() throws ParsingError {
        String formula = "1 - (-3.7 + 2)";

        Expression e = ExpressionParser.parse(formula);

        double result = e.calculate();
        Assert.assertEquals(result, 2.7, 0.00001);
    }

    @Test
    public void testOperatorPriority() throws ParsingError {
        String formula = "1+2*3^2";

        Assert.assertEquals(ExpressionParser.parse(formula).calculate(), 19, 0.00001);
    }

    @Test
    public void testNamedConstants() throws ParsingError {
        Expression e = ExpressionParser.parse("2*pi");

        double result = e.calculate();
        Assert.assertEquals(result, 6.283185307, 0.000000001);

        e = ExpressionParser.parse("e^(3*ln 2)");
        result = e.calculate();
        Assert.assertEquals(result, 8.0, 0.000000001);
    }

    @Test(expected = ParsingError.class)
    public void testOperatorWithoutOperand() throws ParsingError {
        Expression e = ExpressionParser.parse("(sin cos) x");
    }

    @Test(expected = ParsingError.class)
    public void testOperatorWithoutOperand2() throws ParsingError {
        Expression e = ExpressionParser.parse("a^sin x");
        System.out.println(e.toLispString());
        System.out.println(e.toString());
    }

}
