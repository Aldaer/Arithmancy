package arithmancy;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.regex.Matcher;


public class ParserExTest {
/*    @Test
    public void testBeginsWithANumber() {
        Matcher m = ParserEx.BEGINS_WITH_A_NUMBER.matcher("");
        assertFalse(m.find());

        m = ParserEx.BEGINS_WITH_A_NUMBER.matcher("a1");
        assertFalse(m.find());

        m = ParserEx.BEGINS_WITH_A_NUMBER.matcher("1");
        assertTrue(m.find());
        assertTrue(m.group(1).equals("1"));

        m = ParserEx.BEGINS_WITH_A_NUMBER.matcher("12.7adfe");
        assertTrue(m.find());
        assertTrue(m.group(1).equals("12.7"));
    }*/

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