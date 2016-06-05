package arithmancy;

import java.util.regex.Pattern;

import static arithmancy.ParserEx.ADD_SPACES_AROUND;

/**
 * Regex patterns used by arithmetic parser
 */
interface ParserEx {
    // Pattern BEGINS_WITH_A_NUMBER = Pattern.compile("^(\\d+\\.?\\d*)");       - obsolete
    Pattern IS_A_NUMBER = Pattern.compile("^(\\d+\\.?\\d*)$");
    Pattern NONWORD_SEQUENCE = Pattern.compile("\\W+");
    Pattern NONASCII_SEQUENCE = Pattern.compile("[^\\x00-\\x7F]+");

    // Gets re-calculated when adding new known operators.
    //  All characters included in operator tokens are automatically valid.
    Pattern DEFAULT_VALID_CHARS = Pattern.compile("[a-z\\s\\d\\.\\(\\)]*");
    Pattern ANY_NUMBER = Pattern.compile("(\\d+\\.?\\d*)");
    Pattern ANY_WHITESPACE = Pattern.compile("\\s+");
    Pattern SPACES_AROUND_PARENTHESES = Pattern.compile("\\s*(\\(|\\))\\s*");
    Pattern ATOM = Pattern.compile("ATOM");

    // These replacements require regex with a capture group
    String REMOVE_SPACES_AROUND = "$1";
    String ADD_SPACES_AROUND = " $1 ";
}

// Utility function to use with lambdas. Non thread-safe!
class MutableString {
    /**
     * Performs static replaceAll on s as if it was a mutable String
     */
    private final StringBuilder s;

    MutableString(CharSequence initial) {
        s = new StringBuilder(initial);
    }

    void replaceAll(Pattern regexPattern, String replacement) {
        String newS = regexPattern.matcher(s).replaceAll(replacement);
        s.setLength(0);
        s.append(newS);
    }

    void replaceAll(String regex, String replacement) {
        replaceAll(Pattern.compile(regex), replacement);
    }

    void addSpacesAround(String regexLiteral) {
        replaceAll(Pattern.compile('(' + Pattern.quote(regexLiteral) + ')'), ADD_SPACES_AROUND);
    }

    @Override
    public String toString() {
        return s.toString();
    }
}
