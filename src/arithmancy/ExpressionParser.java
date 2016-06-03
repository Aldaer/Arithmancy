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
        for (String opToken: knownOps.values()) {
            String opPar = '(' + Pattern.quote(opToken) + ')';
            e = e.replaceAll(opPar, ADD_SPACES_AROUND);
        }

        // Add spaces around numbers
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
            // ============== Main recognizer. Adds pieces as incomplete expressions (i. e. operators without operands)


            Matcher m = ATOM.matcher(a);
            if (m.find()) {                                                           // Add already parsed (...) block as a complete Expression
                int atomNum = Integer.valueOf(a.substring(m.end()));
                expressionChain.add(parsedAtomsInParentheses.get(atomNum));

            } else if (IS_A_NUMBER.matcher(a).matches()) {                                          // A number is converted into a Constant
                double d = Double.valueOf(a);
                expressionChain.add(new Constant(d));

            } else if (knownNamedConsts.keySet().contains(a)) {                                     // A named constant
                expressionChain.add(knownNamedConsts.get(a));

            } else if (knownOps.values().contains(a)) {                                             // It is a known operator
                Operator op = null;
                Operator unaryA = null;
                Operator binaryA = null;
                for (Map.Entry<Operator, String> knop : knownOps.entrySet())
                    if (knop.getValue().equals(a)) switch (knop.getKey().kind) {                    // At least one of (unaryA, binaryA) is guaranteed to be !null
                        case UNARY: unaryA = knop.getKey();
                            break;
                        case BINARY: binaryA = knop.getKey();
                            break;
                    }
                if (binaryA == null)
                    op = unaryA;
                else if (unaryA == null)
                    op = binaryA;
                else if (expressionChain.size() == 0)                       // Ambiguity: both unary and binary versions of the operator exist
                    op = unaryA;                                            // If expression begins with an operator, it is always unary
                else {
                    Expression prev = expressionChain.getLast();            // Guaranteed to exist
                    if ((prev instanceof Constant)||(prev instanceof Variable))
                        op = binaryA;                                       // "a - 2" is a - 2, not a*(-2)
                    else if (prev instanceof Operator) {
                        op = ((Operator) prev).incomplete() ? unaryA : binaryA;     // (a+2)-3: binary '-', a+ -3: unary '-'
                    }
                }
                expressionChain.add(op.clone());

            } else                                                          // Not a number, ATOM or operator? It's a variable.
                expressionChain.add(addNewVariable(a));                     // addNewVariable() checks for duplicates.
        }

        for (Operator op: opsReorederedByPrecedence(expressionChain)) {
            int opPos = expressionChain.indexOf(op);
            if (opPos == expressionChain.size() - 1) throw new ParsingError("Operator " + op.token + " has no right operand");
            op.rightOperand = expressionChain.remove(opPos + 1);
            if (op.kind == BINARY) {
                if (opPos == 0) throw new ParsingError("Operator " + op.token + " has no left operand");
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
    private static List<Operator> opsReorederedByPrecedence(List<Expression> chain) {

        LinkedList<Expression> unordered = (chain instanceof LinkedList)? (LinkedList)chain : new LinkedList<>(chain);
        List<Operator> reordered = new ArrayList<>();

        // Loop for all precedences from highest to lowest
        for (int i = Operator.Precedence.values().length; --i >= 0; ) {
            Operator.Precedence prec = Operator.Precedence.values()[i];

            // Functions and other unary operators are evaluated RTL, all other operators are evaluated LTR.
            Iterator<Expression> pass = (prec == Operator.Precedence.FUNC)? unordered.descendingIterator() : unordered.iterator();
            for (; pass.hasNext();) {
                Expression nextEx = pass.next();
                // Adding only operators with current precedence that have no operands
                if ((nextEx instanceof Operator) && (((Operator) nextEx).prec == prec) && ((Operator) nextEx).incomplete()) {
                    reordered.add((Operator) nextEx);
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
        return s.substring(pos+1, p);
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
     * Loads default set of arithmetic operators, functions etc.<p>
     * To add suport for new operators by default, extend this class, override
     * LoadDefaultKnownOperators, call super() and then add any additional
     * operators or functions you need. All functions are implicitly unary operators.
     */
    public static void loadDefaultKnownOperators() {
        clearKnownOperators();

        addOperator(new Operator(BINARY, ADD)  {
            @Override
            public double calculate() {
                return leftOperand() + rightOperand();
            }
        }, "+");

        addOperator(new Operator(BINARY, ADD) {
            @Override
            public double calculate() {
                return leftOperand() - rightOperand();
            }
        }, "-");

        addOperator(new Operator(BINARY, MUL) {
            @Override
            public double calculate() {
                return leftOperand()*rightOperand();
            }
        }, "*");

        addOperator(new Operator(BINARY, MUL) {
            @Override
            public double calculate() {
                return leftOperand()/rightOperand();
            }
        }, "/");

        addOperator(new Operator(BINARY, POW) {
            @Override
            public double calculate() {
                return Math.exp(Math.log(leftOperand())*rightOperand());
            }
        }, "^");

        addOperator(new Function() {       // Unary + and - has the same precedence as functions, so that 2*-1 = -2; sin-x = sin(-x); -x^2 = -(x^2)
            @Override
            public double calculate() {
                return -rightOperand();
            }
        }, "-");

        addOperator(new Function() {
            @Override
            public double calculate() {
                return rightOperand();
            }
        }, "+");
// TODO: add more standard functions
        recalculateValidChars();
    }

    private static void recalculateValidChars() {
        StringBuilder validCh = new StringBuilder("[a-z\\s\\d\\.\\(\\)\\Q");

        for (String opToken:knownOps.values()) {
            validCh.append(opToken);
        }
        validCh.append("\\E]*");
        validChars = Pattern.compile(validCh.toString());
    }

    /**
     * Clears the list of known operators. Call this function to load a nonconventional set of operators.
     */
    public static void clearKnownOperators() {
        knownOps.clear();
        validChars = DEFAULT_VALID_CHARS;
    }

    /**
     * Adds a new operator or function to known operator collection. Returns true if successful.<br>
     * Following restrictions apply (the term "operator" here includes functions):<br>
     * Operator cannot be null.<br>
     * Operator token cannot include uppercase letters.<br>
     * Operator cannot have the same token and number of operands as existing operator.<br>
     * Operator token cannot be a substring or superstring of any operator's token already in collection.<br>
     * Two operators with the same token are possible if and only if one of them is unary and other is binary.
     * @param op Operator to add
     * @param token Representation of the operator in the string expression
     * @return Returns {@code true} if successful, {@code false} if the new operator conflicts with already known operators
     */
    public static boolean addOperator(Operator op, String token) {
        if ((null == op)||(null == token)||(token.equals(""))) return false;
        if (knownOps.containsKey(op)) return true;
        if (!token.equals(token.toLowerCase())) return false;

        for(Map.Entry<Operator, String> knop: knownOps.entrySet()) {
            String knopToken = knop.getValue();
            if ( (knop.getKey().kind == op.kind)&&(knopToken.equals(token)) || (knopToken.contains(token) || token.contains(knopToken))&&(token.length() != knopToken.length()) ) return false;
        }

        knownOps.put(op, token);
        recalculateValidChars();
        op.token = token;
        return true;
    }

    public static void resetNamedConstants() {
        knownNamedConsts.clear();
        addNamedConstant("pi", Math.PI);
        addNamedConstant("e", Math.E);
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

        knownNamedConsts.put(name, new Constant(value));
        return true;
    }

    private static Variable addNewVariable(String name) {                                    // All variables of the same name are intrinsically the same Variable object
        for (Variable v: knownVars) if (v.getName().equals(name)) return v;

        Variable newv = new Variable(name);
        knownVars.add(newv);
        return newv;
    }

    private static Map<Operator, String> knownOps = new HashMap<>();
    private static Pattern validChars = DEFAULT_VALID_CHARS;       // Gets updated when adding new known operators

    private static Set<Variable> knownVars = new HashSet<>();
    private static Map<String, Constant> knownNamedConsts = new HashMap<>();

    static {       // Static init block
        loadDefaultKnownOperators();
        resetNamedConstants();
    }

    // Static class, no instantiation
    private ExpressionParser() {}

    /**
     * Breaks up string that contains no spaces into variables and operators.
     */
    private static String[] breakAtom(String s) {      // TODO: implement this correctly
        String[] sarr = new String[1];
        sarr[0] = s;
        return sarr;
    }

}
