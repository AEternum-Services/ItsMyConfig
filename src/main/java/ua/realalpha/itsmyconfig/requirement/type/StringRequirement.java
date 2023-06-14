package ua.realalpha.itsmyconfig.requirement.type;

import ua.realalpha.itsmyconfig.requirement.Requirement;
import ua.realalpha.itsmyconfig.requirement.checker.ContainsCheck;
import ua.realalpha.itsmyconfig.requirement.checker.StringEqualsCheck;

import java.util.Arrays;
import java.util.regex.Pattern;

public class StringRequirement extends Requirement<String> {

    private final Pattern colorFilter = Pattern.compile("[§&][a-zA-Z0-9]");

    public StringRequirement() {
        this.syntax("equals", new StringEqualsCheck());
        this.syntax("contains", new ContainsCheck());
    }


    @Override
    public boolean validate(String identifier, String inputString, String outputString) {
        boolean ignoreCase = identifier.toLowerCase().contains("ignorecase");
        boolean ignoreColor = identifier.toLowerCase().contains("ignorecolor");

        String input = inputString;
        String output = outputString;

        if (ignoreCase) {
            identifier = identifier.replace("ignorecase", "");
            input = inputString.toLowerCase();
            output = outputString.toLowerCase();
        }

        if (ignoreColor) {
            identifier = identifier.replace("ignorecolor", "");
            input = inputString.replaceAll(colorFilter.pattern(), "");
            output = outputString.replaceAll(colorFilter.pattern(), "");
        }

        boolean reverse = identifier.startsWith("!");
        String[] syntaxArguments = identifier.split(" ");
        syntaxArguments = Arrays.copyOfRange(syntaxArguments, 1, syntaxArguments.length);
        boolean result = true;
        for (String syntax : syntaxArguments) {
            result = reverse != this.isValid(syntax, input, output);
            if (!result) break;
        }
        return result;
    }

    @Override
    public String[] identifiers() {
        return new String[] {
                "string",
                "!string"
        };
    }
}
