import arithmancy.*;

import java.util.Set;

import static IOHelp.IOHelper.readln;
import static IOHelp.IOHelper.writeln;

/**
 * Sample usage for Arithmetic Parser
 */
public class Main {

    public static void main(String[] args) {
        do {
            writeln("Enter expression (or empty string to stop): ");
            String s = readln();
            if (s.equals("")) break;
            Expression e = null;
            try {
                e = ExpressionParser.parse(s);
            } catch (ParsingError parsingError) {
                parsingError.printStackTrace();
                System.exit(-1);
            }

            writeln("== " + e.toLispString());
            writeln("== " + e.toString());

            Set<String> dependencies = e.dependsOnVariables();
            if (dependencies.size() > 0) {
                writeln("Enter the following variables:");
                for (String var: dependencies) {
                    Double varVal = null;
                    System.out.print(var + " = ");
                    do try {
                        varVal = Double.valueOf(readln());
                    } catch (NumberFormatException nfe) {}
                    while (varVal == null);

                    ExpressionParser.setVariable(var, varVal);
                }
            }

            double result = e.calculate();
            writeln("Result = " + result);
        } while (true);
    }

}
