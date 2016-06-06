import arithmancy.Expression;
import arithmancy.ExpressionParser;
import arithmancy.ParsingError;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static IOHelp.IOHelper.readln;
import static IOHelp.IOHelper.writeln;

/**
 * Sample usage for Arithmetic Parser
 */
public class ExampleOfUse {

    public static void main(String[] args) {
        do {
            writeln("Enter expression (or empty string to stop): ");
            String s = readln();
            if (s.equals("")) break;
            try {
                Expression e = ExpressionParser.parse(s);

                writeln("== " + e.toLispString());
                writeln("== " + e.toString());

                Set<String> dependencies = e.dependsOnVariables();
                if (dependencies.size() > 0) {                                          // We have variables!
                    writeln("Enter the values for the following variables:");
                    String loop;
                    do {                                                                // Try many times with different values
                         for (String var: dependencies) {
                            Optional<Double> varVal;
                            boolean badNumber;
                            do {
                                varVal = ExpressionParser.getNamedValue(var);
                                System.out.print(var + (varVal.map(v -> " [" + v.toString() + "]: ").orElse(": ")));
                                s = readln().trim();
                                badNumber = false;
                                try {
                                    varVal = Optional.of(Double.valueOf(s));
                                } catch (NumberFormatException nfe ) {
                                    badNumber = true;
                                }
                            }
                            while ( (badNumber && ! s.equals("")) || ! varVal.isPresent() );

                            ExpressionParser.setVariable(var, varVal.get());
                        }
                        double result = e.calculate();
                        writeln("Result = " + result);
                        writeln("Change variables? y/[n]");
                        loop = readln(); }
                    while (loop.matches("^y.*"));
                } else {
                    double result = e.calculate();
                    writeln("Result = " + result);
                }
            } catch (ParsingError parsingError) {
                System.out.println("Parsing error. " + parsingError.getMessage());
            }
        } while (true);
    }

}
