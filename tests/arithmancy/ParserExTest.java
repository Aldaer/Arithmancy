package arithmancy;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.regex.Matcher;

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