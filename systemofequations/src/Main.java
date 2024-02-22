import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Main {

    private static Map<String, Integer> solvedVariables = new HashMap<>();
    private static Map<String, String> equations = new HashMap<>();

    // Used to prevent infinite loops that would cause a Stack overflow
    private static List<String> checkedVariables = new ArrayList<>();

    public static void main (String[] args) {
        // keep this function call here
        Scanner s = new Scanner(System.in);
        System.out.print(SystemofEquations(s.nextLine()));
    }

    public static String SystemofEquations(String str){
        /* Parse the input, check for errors and return a Map of equations:
            key: the variable the equations is resolved for (left side)
            value: the right side of the equation
         */
        equations = parseInput(str.toLowerCase());

        // Start to solve the system of equations with the one which has x on the left side
        try {
            Integer x = solveEquationFor("x");
            return x.toString();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Map<String, String> parseInput(String input){
        if(input == "")
            throw new IllegalArgumentException("The input String cannot be empty");
        List<String> splitInput = Arrays.stream(input.split("; ")).toList();
        Map<String, String> equations = new HashMap<>();
        splitInput.stream().forEach(equation ->{
            try {
                String leftSide = equation.split(" = ")[0];
                String rightSide = equation.split(" = ")[1];
                if(equations.containsKey(leftSide))
                    throw new IllegalArgumentException("The same variable cannot be on the left side in multiple equations");
                equations.put(leftSide, rightSide);
            } catch (Exception e){
                throw new IllegalArgumentException("Left and right side of the equations cannot be empty");
            }
        });
        checkEquationsValidity(equations);
        return equations;
    }

    private static void checkEquationsValidity(Map<String, String> equations){
        if(!equations.containsKey("x"))
            throw new IllegalArgumentException("The system must contain an equation that define the value of x");

        equations.forEach((variable, equation)-> {
            if(!variable.matches("[a-z]+")) {
                throw new IllegalArgumentException(String
                        .format("The left side of the equations must be made up of letters only. \nThis is not valid: %s = %s", variable, equation));
            }
            if(!equation.matches("(\\s*[a-z0-9]+\\s\\^)*(\\s*[a-z0-9]+)+(;\\s)*"))
                throw new IllegalArgumentException(String
                        .format("The right side of this equation has an incorrect format: \n%s = %s", variable, equation));

            String[] equationParts = equation.split(" ");
            if(Arrays.stream(equationParts).anyMatch(variable::equals))
                throw new IllegalArgumentException(String
                        .format("A variable cannot be both on the left and right side of an equation: \n%s = %s", variable, equation));
        });
    }

    private static Integer solveEquationFor(String solvingVariable) throws RuntimeException {
        /* This could also be achieved setting a max recursion depth, but without more input specifications
         this solution will work, even if potentially it could end in a stack overflow, given a big enough set of variables
         */
        if(checkedVariables.contains(solvingVariable)){
            // We are in a loop, we must exit and return null since the system has no solution
            throw new RuntimeException("Unsolvable System");
        }
        checkedVariables.add(solvingVariable);

        // This list will contain all the solved variables of the current equation
        List<String> currentEquationValues = new ArrayList<>();

        /* Evaluates all the parts of an unsolved equation:
            if the part is a number, it is added to the equation values
            if the part is a variable (=not a number), it tries recursively to solve the equation mapped for that variable
            if the part is "^" is ignored
         */
        String[] equationParts = equations.get(solvingVariable).split(" ");
        for (String part : equationParts) {
            if (part.matches("\\d+"))
                currentEquationValues.add(part);
            else if(part.matches("[a-z]+")){
                if (solvedVariables.get(part) == null) {
                    Integer partValue = solveEquationFor(part);
                    currentEquationValues.add(partValue.toString());
                } else
                    currentEquationValues.add(solvedVariables.get(part).toString());
            }
        }

        /* Once an equation has all its parts solved (=numbers), it stores it into the map of
            solved variables and it returns the value for that variable;
            if that variable is x (the first call of this method) then the program returns the answer
         */
        Integer variableValue = currentEquationValues
                .stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList())
                .stream()
                .reduce(0, (acc, part) -> acc ^ part);
        solvedVariables.put(solvingVariable, variableValue);
        return variableValue;
    }
}