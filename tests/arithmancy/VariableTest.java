package arithmancy;

import org.junit.Test;

import static org.junit.Assert.*;

public class VariableTest {
    @Test(expected = VariableNotSetException.class)
    public void unsetVariableCannotBeCalculated() {
        Variable v = new Variable("'v");
        v.calculate();
    }

    @Test
    public void setVariableCanBeCalculated() {
        Variable v = new Variable("v");
        v.setValue(1.0);
        assertTrue(v.calculate() == 1.0d);
    }
}