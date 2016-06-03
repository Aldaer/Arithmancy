import arithmancy.*;

import java.util.Set;

import static IOHelp.IOHelper.readln;
import static IOHelp.IOHelper.writeln;

public class Main {

    public static void main(String[] args) {
        writeln("Enter expression: ");
        String s = readln();
        Expression e = null;
        try {
            e = ExpressionParser.parse(s);
        } catch (ParsingError parsingError) {
            parsingError.printStackTrace();
            System.exit(-1);
        }

        writeln("== " + e.toLispString());

        Set<Variable> dependencies = e.dependsOnVariables();
        if (dependencies.size() > 0) {
            writeln("Enter the following variables:");
            for (Variable v: dependencies) while (!v.set()) {
                System.out.print(v.getName() + " = ");
                v.setValue(Double.valueOf(readln()));
            }
        }

        double result = e.calculate();
        writeln("Result = " + result);
    }


}
