package arithmancy;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test RegEx'es used in Arithmetic Parser
 */
public class ParserExTest {

    @Test
    public void testIsANumber() {
        Matcher m = ParserEx.IS_A_NUMBER.matcher("");
        assertFalse(m.matches());

        m = ParserEx.IS_A_NUMBER.matcher("1as");
        assertFalse(m.matches());

        m = ParserEx.IS_A_NUMBER.matcher("1");
        assertTrue(m.matches());

        m = ParserEx.IS_A_NUMBER.matcher("12.7");
        assertTrue(m.matches());

        m = ParserEx.IS_A_NUMBER.matcher("112..43");
        assertFalse(m.matches());

    }

}