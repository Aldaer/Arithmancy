package arithmancy;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static arithmancy.Operator.Kind.*;
import static arithmancy.Operator.Precedence.*;
import static arithmancy.ParserEx.*;

/**
 * Main expression parser. Transforms input string into a tree of Expression objects, which can be used to calculate the result of teh expression.
 *
 */
public class ExpressionParser {

    static String preprocessExpression(String e) {

        // Bring string to lowercase
        e = e.toLowerCase();

        // Add spaces around all known operators to facilitate further processing
        for (String opToken: knownOps().values()) {
            String opPar = '(' + Pattern.quote(opToken) + ')';
            e = e.replaceAll(opPar, ADD_SPACES_AROUND);
        }

        // Ditto for named constants
        for (String consToken: knownNamedConsts.keySet()) {
            String cons = '(' + Pattern.quote(consToken) + ')';
            e = e.replaceAll(cons, ADD_SPACES_AROUND);
        }

        // Ditto for numbers
        e = e.replaceAll(ANY_NUMBER, ADD_SPACES_AROUND);

        // Replace all whitespaces with single spaces, remove all spaces around parentheses, remove starting and ending whitespace
        return e.replaceAll(ANY_WHITESPACE, " ")
            .replaceAll(SPACES_AROUND_PARENTHESES, REMOVE_SPACES_AROUND).trim();
    }

    /**
     * Transforms input string into a tree of Expression objects.<p>
     * To use it, load some operators first.
     * @param expr Expression to parse
     * @return Root of the tree
     */
    public static Expression parse(String expr) throws ParsingError {
        knownVars.clear();

        String normExpr = preprocessExpression(expr);

        if (!validChars.matcher(normExpr).matches()) throw new ParsingError("Invalid character detected");

        int errpos = checkParenthesesAreCorrect(normExpr);
        if (errpos >= 0) throw new ParsingError("Invalid parenthesis structure");

        return parseSubstring(normExpr);
    }

    // ============================= MAIN STRING PARSER =============================

    private static Expression parseSubstring(String expr) throws ParsingError {

        List<Expression> parsedAtomsInParentheses = new ArrayList<>();
        for(int x; (x = expr.indexOf('(')) >= 0; ) {

            // Replace (...) blocks with "ATOM#)"
            String atom = substringInParentheses(expr, x);
            // Parse each atom with recursive call
            parsedAtomsInParentheses.add(parseSubstring(atom));
            int atomNum = parsedAtomsInParentheses.size() - 1;

            expr = (expr.substring(0, x) + " ATOM" + atomNum + ' ' + expr.substring(x + atom.length() + 2)).trim();
        }

        LinkedList<Expression> expressionChain = new LinkedList<>();
        ArrayList<String> atoms = new ArrayList<>(Arrays.asList(expr.split(" ")));                  // TODO: implement smart atom parsing with silent multiplication, i.e. ab = a * b
        if (atoms.size() == 0) throw new ParsingError("Empty expression");

        for(String a: atoms) {
            // ============== Main recognizer. Adds recongnized pieces as incomplete expressions (i. e. operators without operands)


            Matcher m = ATOM.matcher(a);
            if (m.find()) {                                                           // Add already parsed (...) block as a complete Expression
                int atomNum = Integer.valueOf(a.substring(m.end()));
                expressionChain.add(parsedAtomsInParentheses.get(atomNum));

            } else if (IS_A_NUMBER.matcher(a).matches()) {                                          // A number is converted into a Constant
                double d = Double.valueOf(a);
                expressionChain.add(new Constant(d));

            } else if (knownNamedConsts.keySet().contains(a)) {                                     // A named constant
                expressionChain.add(knownNamedConsts.get(a));

            } else {
                boolean isUnary = knownUnaries.keySet().contains(a);
                boolean isBinary = knownBinaries.keySet().contains(a);
                if (isUnary || isBinary) {                                                          // It is a known operator

                    Operator op = null;                                                                   // Select prototype: unary or binary
                    Operator unaryA = isUnary? knownUnaries.get(a) : null;
                    Operator binaryA = isBinary? knownBinaries.get(a) : null;

                    // At least one of (unaryA, binaryA) is guaranteed to be !null
                    if (binaryA == null)
                        op = unaryA;
                    else if (unaryA == null)
                        op = binaryA;
                    else if (expressionChain.size() == 0)                       // Ambiguity: both unary and binary versions of the operator exist
                        op = unaryA;                                            // If expression begins with an operator, it is always unary
                    else {
                        Expression prev = expressionChain.getLast();            // Guaranteed to exist
                        if ((prev instanceof Constant) || (prev instanceof Variable))
                            op = binaryA;                                       // "a - 2" is a - 2, not a*(-2)
                        else if (prev instanceof OperatorInstance) {
                            op = ((OperatorInstance) prev).incomplete() ? unaryA : binaryA;     // (a+2)-3: binary '-', a+ -3: unary '-'
                        }
                    }
                    expressionChain.add(new OperatorInstance(op));

                } else                                                          // Not a number, ATOM or operator? It's a variable.
                    expressionChain.add(addNewVariable(a));                     // addNewVariable() checks for duplicates.
            }
        }

        for (OperatorInstance op: opsReorederedByPrecedence(expressionChain)) {
            int opPos = expressionChain.indexOf(op);
            if (opPos == expressionChain.size() - 1) throw new ParsingError("OperatorInstance " + op.token + " has no right operand");
            op.rightOperand = expressionChain.remove(opPos + 1);
            if (op.kind == BINARY) {
                if (opPos == 0) throw new ParsingError("OperatorInstance " + op.token + " has no left operand");
                op.leftOperand = expressionChain.remove(opPos - 1);
            }
        }

        if (expressionChain.size() > 1) {
            StringBuilder exps = new StringBuilder("Uncollapsed expression: [");
            for (Expression ex: expressionChain) {
                exps.append(" {");
                exps.append(ex.toLispString());
                exps.append('}');
            }
            exps.append(" ]");
            throw new ParsingError(exps.toString());
        }

        return expressionChain.getFirst();
    }

    /**
     * Creates a list of operators ordered y execution precedence from a list of expressions.
     * Constants, vars and COMPLETE operators (i.e. those who already have their right operand) are OMITTED.
     * Input list is unaffected.
     */
    private static List<OperatorInstance> opsReorederedByPrecedence(final Collection<Expression> expChain) {

        LinkedList<Expression> unordered = (expChain instanceof LinkedList)? (LinkedList)expChain : new LinkedList<>(expChain);
        List<OperatorInstance> reordered = new ArrayList<>();

        // Loop for all precedences from highest to lowest
        for (Operator.Precedence currentPrec : Operator.Precedence.descending() ) {

            // Functions and other unary operators are evaluated RTL, all other operators are evaluated LTR.
            Iterator<Expression> pass = (currentPrec == Operator.Precedence.FUNC)? unordered.descendingIterator() : unordered.iterator();
            for (; pass.hasNext();) {
                Expression nextEx = pass.next();
                // Adding only operators with current precedence that have no operands
                if ((nextEx instanceof OperatorInstance) && (((OperatorInstance) nextEx).prec == currentPrec) && ((OperatorInstance) nextEx).incomplete()) {
                    reordered.add((OperatorInstance) nextEx);
                }
            }
        }
        return reordered;
    }

    private static String substringInParentheses(final String s, final int pos) {       // pos must point to opening parenthesis, string must be correct
        int b = 1;
        int p = pos;
        do {
            switch (s.charAt(++p)) {
                case '(': b++;
                    break;
                case ')': b--;
                    break;
            }
        } while (b > 0);
        return s.substring(pos + 1, p);
    }

    /**
     * Checks for correct parenthesis structure. Returns position of incorrect parenthesis, -1 if everything is correct,<p>
     * i.e. all '('s have matching ')'s, s.length if there are not enough ')'s.
     */
     private static int checkParenthesesAreCorrect(final String s) {
         int errpos = -1;
         int b = 0;
         for (int i = 0; i < s.length(); i++) {
             switch (s.charAt(i)) {
                 case '(': b++;
                     break;
                 case ')': b--;
                     break;
             }
             if (b < 0) {
                 errpos = i;
                 break;
             }
         }
         if (b > 0) errpos = s.length();

         return errpos;
     }

    /**
     * Loads default set of arithmetic operators, functions etc.<br>
     * It gets called automatically during static initiation. No need to call it again unless you clear known operator list using clearKnownOperators().<br>
     * To add suport for new operators by default, extend this class, override
     * LoadDefaultKnownOperators, call super() and then add any additional
     * operators or functions you need. All functions are implicitly unary operators.
     */
    public static void loadDefaultKnownOperators() {
        clearKnownOperators();

        addOperator(new Operator("+", ADD, (x, y) -> x + y) );

        addOperator(new Operator("-", ADD, (x, y) -> x - y) );

        addOperator(new Operator("*", MUL, (x, y) -> x * y) );

        addOperator(new Operator("/", MUL, (x, y) -> x / y) );

        addOperator(new Operator("^", POW, (x, y) -> Math.exp(Math.log(x)*y)) );

        addOperator(new Operator("-", FUNC, (x) -> -x ) );

        addOperator(new Operator("+", FUNC, (x) -> x ) );

        addOperator(new Operator("ln", FUNC, Math::log) );

        addOperator(new Operator("exp", FUNC, Math::exp) );

        addOperator(new Operator("sin", FUNC, Math::sin) );

        addOperator(new Operator("cos", FUNC, Math::cos) );

        addOperator(new Operator("tg", FUNC, Math::tan) );

// TODO: add more standard functions
        recalculateValidChars();
    }

    private static void recalculateValidChars() {
        StringBuilder validCh = new StringBuilder();
        for (String opToken:knownOps().values()) validCh.append(opToken);

        validChars = Pattern.compile("[a-z\\s\\d\\.\\(\\)" + Pattern.quote(validCh.toString()) + "]*");
    }

    /**
     * Clears the list of known operators. Call this function to load a nonconventional set of operators.
     */
    public static void clearKnownOperators() {
        knownUnaries.clear();
        knownBinaries.clear();
        validChars = DEFAULT_VALID_CHARS;
        knownOpsCache = null;
    }

    /**
     * Adds a new operator or function to known operator collection. Returns true if successful.<br>
     * Following restrictions apply (the term "operator" here includes functions):<br>
     * OperatorInstance cannot be null.<br>
     * OperatorInstance token cannot include uppercase letters.<br>
     * OperatorInstance cannot have the same token and number of operands as existing operator.<br>
     * OperatorInstance token cannot be a substring or superstring of any operator's token already in collection.<br>
     * Two operators with the same token are possible if and only if one of them is unary and other is binary.
     * @param op Operator to add. Effect of the operator is implemented through "effect" parameter during creation of the Operator object.
     * @return Returns {@code true} if successful, {@code false} if the new operator conflicts with already known operators
     */
    public static boolean addOperator(Operator op) {

        if ((null == op)||(null == op.token)||(op.token.equals(""))) return false;
        if (knownOps().containsKey(op)) return true;
        if (!op.token.equals(op.token.toLowerCase())) return false;

        for(Map.Entry<Operator, String> knop: knownOps().entrySet()) {
            String knopToken = knop.getValue();
            if ( (knop.getKey().kind == op.kind)&&(knopToken.equals(op.token)) ||
                    (knopToken.contains(op.token) || op.token.contains(knopToken)) &&(op.token.length() != knopToken.length()) ) return false;
        }

        switch (op.kind) {
            case UNARY:
                knownUnaries.put(op.token, op);
                break;
            case BINARY:
                knownBinaries.put(op.token, op);
                break;
            default:
                throw new InvalidOperatorKind(op.token);
        }
        recalculateValidChars();
        knownOpsCache = null;
        return true;
    }

    public static void resetNamedConstants() {
        knownNamedConsts.clear();
        addNamedConstant("pi", Math.PI);
        addNamedConstant("e", Math.E);
    }

    public static void unsetAllVariables() {
        for (Variable v: knownVars.values()) v.unsetValue();
    }

    /**
     * Adds a new named constant that doesn't expire until resetNamedConstants() is called.<br>
     * There are two named constants by default, pi (= Math.PI) and e (= Math.E).<br>
     * To reset named constants to default, use ExpressionParser.resetKnownConstants().<br>
     * @param name Name of the constant
     * @param value Value of the constant
     * @return true if successful, false if trying to change existing constant.
     */
    public static boolean addNamedConstant(String name, double value) {
        if (knownNamedConsts.keySet().contains(name))
            return value == knownNamedConsts.get(name).calculate();

        knownNamedConsts.put(name, new NamedConstant(value, name));
        return true;
    }

    private static Variable addNewVariable(String name) {                                    // All variables of the same name are intrinsically the same Variable object
        if (knownVars.containsKey(name)) return knownVars.get(name);

        Variable newV = new Variable(name);
        knownVars.put(name, newV);
        return newV;
    }


    /**
     * Breaks up string that contains no spaces into variables and operators.
     */
/*    private static String[] breakAtom(String s) {      // TODO: implement this correctly and use in parseSubstring()
    }*/

    public static void setVariable(String varName, Double varVal) throws UnknownVariableException {
        if (!knownVars.containsKey(varName)) throw new UnknownVariableException(varName);
        knownVars.get(varName).setValue(varVal);
    }


    /** Static class, no instantiation
     */
    private ExpressionParser() {}

    private static Map<String, Operator> knownUnaries = new HashMap<>();
    private static Map<String, Operator> knownBinaries = new HashMap<>();

    private static Map<Operator, String> knownOps() {
        if (null == knownOpsCache) {
            knownOpsCache = new HashMap<>();
            for (Map.Entry<String, Operator> unOp: knownUnaries.entrySet()) {
                knownOpsCache.put(unOp.getValue(), unOp.getKey());
            }
            for (Map.Entry<String, Operator> biOp: knownBinaries.entrySet()) {
                knownOpsCache.put(biOp.getValue(), biOp.getKey());
            }
        }
        return knownOpsCache;
    }

    private static Pattern validChars = DEFAULT_VALID_CHARS;       // Gets updated when adding new known operators

    private static Map<Operator, String> knownOpsCache;            // Invalidate (= null) every time when adding or removing operators
    private static Map<String, Variable> knownVars = new HashMap<>();
    private static Map<String, Constant> knownNamedConsts = new HashMap<>();

    static {       // Static init block
        loadDefaultKnownOperators();
        resetNamedConstants();
    }

}
