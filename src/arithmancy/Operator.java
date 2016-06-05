package arithmancy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import static arithmancy.Operator.Kind;
import static arithmancy.Operator.Kind.BINARY;
import static arithmancy.Operator.Kind.UNARY;
import static arithmancy.Operator.Precedence;

/**
 * Operator as used in calculable expressions, such as Expression tree.
 */
class OperatorInstance implements Expression {

    final Precedence prec;
    final Kind kind;
    final String token;

    private final java.util.function.BiFunction<Double, Double, Double> calculateBi;
    private final java.util.function.Function<Double, Double> calculateU;

    OperatorInstance(Operator proto) {
        this.kind = proto.kind;
        this.prec = proto.prec;
        this.token = proto.token;
        calculateBi = proto.calculateBi;
        calculateU = proto.calculateU;
    }

    Expression leftOperand;
    Expression rightOperand;

    @Override
    public Double calculate() {
        switch (kind) {
            case UNARY:
                return calculateU.apply(rightOperand.calculate());
            case BINARY:
                return calculateBi.apply(leftOperand.calculate(), rightOperand.calculate());
        }
        throw new RuntimeException(new InvalidOperatorKind(token));                 // Should never happen
    }

    @Override
    public Set<String> dependsOnVariables() {

        Set<String> r = new HashSet<>();
        switch (kind) {
            case BINARY:
                r.addAll(leftOperand.dependsOnVariables());
            case UNARY:
                r.addAll(rightOperand.dependsOnVariables());
        }
        return r;
    }

    @Override
    public boolean complete() {
        boolean complete = true;
            switch (kind) {
                case BINARY:
                    complete = leftOperand != null && leftOperand.complete();
                case UNARY:
                    complete &= rightOperand != null && rightOperand.complete();
            }
        return complete;
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

    @Override
    public String toString() {
        switch (kind) {
            case UNARY:
                return token + '(' + rightOperand.toString() + ')';
            case BINARY:
                return '(' + leftOperand.toString() + ' ' + token + ' ' + rightOperand.toString() + ')';
        }
        throw new InvalidOperatorKind(token);
    }
}

/**
 * Used to create and instantiate OperatorInstance objects
 */
public class Operator {
    /** Operator precedences. If you're to rewrite this to add custom precedences, declare them in ascending order (for clarity).<br>
     * For normal execution of composite functions (i.e. exp sin x == exp(sin(x))), all UNARY operators (including functions) must have precedence FUNC.
     */
    public enum Precedence {
        /** addition/subtraction */
        ADD(1),
        /** multiplication/division */
        MUL(10),
        /** functions and unary operators (including unary minus) */
        FUNC(20),
        /** exponentiation */
        POW(30);

        Precedence(int val) {
            this.val = val;
        }

        /**
         * Returns numerical value of the precedence. Operators are executed from highest to lowest Precedence.<br>
         * If you edit this class, please declare Enum members in ascending order by this value to avoid confusion.
         * @return Numerical value of the precedence
         */
        public int asInt() {
            return val;
        }

        private final int val;
        private static final TreeSet<Precedence> desc;

        static {
            desc = new TreeSet<>((p1, p2) -> p2.val - p1.val);      // Custom "descending" Comparator
            Arrays.asList(Precedence.values()).forEach(desc::add);  // Same as  desc.addAll(Arrays.asList(Precedence.values()));
        }

        /**
         * Lists all Enum members in descending order.
         * @return List of precedences sorted by value in descending order
         */
        static TreeSet<Precedence> highToLow() {
            return desc;
        }
    }

    /**
     * Operator kind.
     */
    public enum Kind {
        /** Unary operator or function */
        UNARY,
        /** Binary operator */
        BINARY }

    final Precedence prec;
    final Kind kind;
    final String token;

    final java.util.function.BiFunction<Double, Double, Double> calculateBi;
    final java.util.function.Function<Double, Double> calculateU;

    public Operator(String token, Precedence prec, BiFunction<Double, Double, Double> effect) {
        this.prec = prec;
        this.kind = BINARY;
        this.token = token;
        this.calculateBi = effect;
        this.calculateU = null;
    }

    public Operator(String token, Precedence prec, Function<Double, Double> effect) {
        this.prec = prec;
        this.kind = UNARY;
        this.token = token;
        this.calculateBi = null;
        this.calculateU = effect;
    }


}