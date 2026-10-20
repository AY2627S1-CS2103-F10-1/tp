package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CreateRoleCommand;
import seedu.address.logic.commands.DeleteRoleCommand;
import seedu.address.logic.commands.EditRoleCommand;
import seedu.address.logic.commands.ListRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.role.Role;

/** Parses the shared name-based syntax of role-management commands. */
public class RoleCommandParser implements Parser<Command> {
    private static final Pattern PARAMETER = Pattern.compile("(?i)(?<!\\S)(n/|r/|p/|e/|a/|t/|no/|s/)");

    private final String commandWord;
    private final String usage;

    /** Selects one supported role-management command. */
    public RoleCommandParser(String commandWord) {
        this.commandWord = commandWord;
        usage = switch (commandWord) {
            case CreateRoleCommand.COMMAND_WORD -> CreateRoleCommand.MESSAGE_USAGE;
            case EditRoleCommand.COMMAND_WORD -> EditRoleCommand.MESSAGE_USAGE;
            case DeleteRoleCommand.COMMAND_WORD -> DeleteRoleCommand.MESSAGE_USAGE;
            case ListRoleCommand.COMMAND_WORD -> ListRoleCommand.MESSAGE_USAGE;
            default -> throw new IllegalArgumentException("Unknown role command.");
        };
    }

    @Override
    public Command parse(String arguments) throws ParseException {
        if (commandWord.equals(ListRoleCommand.COMMAND_WORD)) {
            if (!arguments.isBlank()) {
                throw invalidFormat();
            }
            return new ListRoleCommand();
        }
        ArgumentMultimap values = ArgumentTokenizer.tokenize(normalizePrefixes(arguments), PREFIX_NAME, PREFIX_ROLE);
        values.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_ROLE);
        if (!values.getPreamble().isEmpty()) {
            throw invalidFormat();
        }
        return switch (commandWord) {
            case CreateRoleCommand.COMMAND_WORD -> new CreateRoleCommand(requireName(values, PREFIX_NAME));
            case EditRoleCommand.COMMAND_WORD -> new EditRoleCommand(requireName(values, PREFIX_ROLE),
                    requireName(values, PREFIX_NAME));
            case DeleteRoleCommand.COMMAND_WORD -> new DeleteRoleCommand(requireName(values, PREFIX_ROLE));
            default -> throw new IllegalStateException("Unsupported role parser state.");
        };
    }

    private String normalizePrefixes(String arguments) throws ParseException {
        Set<String> allowed = switch (commandWord) {
            case CreateRoleCommand.COMMAND_WORD -> Set.of("n/");
            case DeleteRoleCommand.COMMAND_WORD -> Set.of("r/");
            default -> Set.of("n/", "r/");
        };
        Matcher matcher = PARAMETER.matcher(arguments);
        StringBuilder canonical = new StringBuilder();
        while (matcher.find()) {
            String prefix = matcher.group().toLowerCase(Locale.ROOT);
            if (!allowed.contains(prefix)) {
                throw new ParseException("Unexpected role parameter: " + matcher.group() + ". Usage: " + usage);
            }
            matcher.appendReplacement(canonical, Matcher.quoteReplacement(" " + prefix));
        }
        matcher.appendTail(canonical);
        return canonical.toString();
    }

    private String requireName(ArgumentMultimap values, Prefix prefix) throws ParseException {
        String name = values.getValue(prefix).orElseThrow(this::invalidFormat);
        try {
            Role.normalize(name);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
        return name;
    }

    private ParseException invalidFormat() {
        return new ParseException("Invalid role command format. Usage: " + usage);
    }
}
