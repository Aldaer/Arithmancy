package arithmancy;

public class ParsingError extends Exception {
    int pos = -1;

    ParsingError(String message) {
        super("Parse error: " + message);
    }
    ParsingError(int pos, String message) {
        this(message);
        this.pos = pos;
    }
}

class InvalidOperatorKind extends RuntimeException {
    public InvalidOperatorKind(String opName) {
        super("Invalid operator kind: " + opName);
        this.opName = opName;
    }

    final String opName;
}