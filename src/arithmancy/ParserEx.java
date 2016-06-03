package arithmancy;

import java.util.regex.Pattern;

/**
 * Regex patterns used by arithmetic parser
 */
interface ParserEx {
    // Pattern BEGINS_WITH_A_NUMBER = Pattern.compile("^(\\d+\\.?\\d*)");       - obsolete
    Pattern IS_A_NUMBER = Pattern.compile("^(\\d+\\.?\\d*)$");

    /* Gets re-calculated when adding new known operators.
       All characters included in operator tokens are automatically valid. */
    Pattern DEFAULT_VALID_CHARS = Pattern.compile("[a-z\\s\\d\\.\\(\\)]*");
    Pattern ATOM = Pattern.compile("ATOM");

    String ANY_WHITESPACE = "\\s+";
    String ANY_NUMBER = "(\\d+\\.?\\d*)";
    String SPACES_AROUND_PARENTHESES = "\\s*(\\(|\\))\\s*";
    String REMOVE_SPACES_AROUND = "$1";
    String ADD_SPACES_AROUND = " $1 ";
}
