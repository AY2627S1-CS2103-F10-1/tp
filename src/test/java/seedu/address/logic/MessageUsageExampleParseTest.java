package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExpandCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.StatusCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.person.Status;

/**
 * Verifies that the {@code Example:} line in each command's {@code MESSAGE_USAGE} string
 * actually parses through {@link AddressBookParser} without throwing. Guards against
 * drift between the user-facing help text and the real parsers.
 */
public class MessageUsageExampleParseTest {

    private final AddressBookParser parser = new AddressBookParser();

    @TestFactory
    Stream<DynamicTest> exampleFromEachUsage_parses() {
        List<CommandUsage> usages = List.of(
                new CommandUsage("add", AddCommand.MESSAGE_USAGE),
                new CommandUsage("clear", ClearCommand.MESSAGE_USAGE),
                new CommandUsage("delete", DeleteCommand.MESSAGE_USAGE),
                new CommandUsage("edit", EditCommand.MESSAGE_USAGE),
                new CommandUsage("expand", ExpandCommand.MESSAGE_USAGE),
                new CommandUsage("filter", FilterCommand.MESSAGE_USAGE),
                new CommandUsage("find", FindCommand.MESSAGE_USAGE),
                new CommandUsage("note", NoteCommand.MESSAGE_USAGE),
                new CommandUsage("status", StatusCommand.MESSAGE_USAGE));

        return usages.stream().map(u -> DynamicTest.dynamicTest(u.name + "ExampleParses", () -> {
            String example = extractExample(u.messageUsage);
            assertNotNull(example, "no 'Example:' line found in " + u.name + " MESSAGE_USAGE");
            assertTrue(example.startsWith(u.name + " ") || example.equals(u.name),
                    u.name + " example does not start with its command word: " + example);
            assertDoesNotThrow(() -> parser.parseCommand(example),
                    u.name + " MESSAGE_USAGE example failed to parse: " + example);
        }));
    }

    @Test
    public void filterExample_usesStatusDisplayNameCasing() {
        // Regression test for the inconsistency fixed in #101: FilterCommand's example
        // used to say s/INTERVIEWING while StatusCommand and the UG use s/Interviewing.
        String example = extractExample(FilterCommand.MESSAGE_USAGE);
        assertNotNull(example);
        assertTrue(example.contains("s/" + Status.INTERVIEWING),
                "filter example should use display-name casing (" + Status.INTERVIEWING + "): " + example);
    }

    private static String extractExample(String messageUsage) {
        int marker = messageUsage.indexOf("Example:");
        if (marker < 0) {
            return null;
        }
        String tail = messageUsage.substring(marker + "Example:".length()).trim();
        int newline = tail.indexOf('\n');
        return newline < 0 ? tail : tail.substring(0, newline).trim();
    }

    private record CommandUsage(String name, String messageUsage) {}
}
