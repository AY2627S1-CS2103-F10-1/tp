package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddRoleCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteRoleCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.EditRoleCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.ListRoleCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_note_dispatchesToNoteParser() throws Exception {
        NoteCommand expected = new NoteCommand(INDEX_FIRST_PERSON, new Note("Passed round 2"));
        assertEquals(expected, parser.parseCommand("note 1 no/Passed round 2"));
    }

    @Test
    public void parseCommand_uppercaseOrMixedCaseCommandWord_dispatchesCommand() throws Exception {
        assertEquals(new NoteCommand(INDEX_FIRST_PERSON, new Note("x")), parser.parseCommand("NOTE 1 no/x"));
        assertTrue(parser.parseCommand("LIST") instanceof ListCommand);
        assertTrue(parser.parseCommand("Help") instanceof HelpCommand);
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), parser.parseCommand("Delete 1"));
        assertEquals(new ClearCommand(true), parser.parseCommand("CLEAR confirm"));
    }

    @Test
    public void parseCommand_multilineNote_throwsParseException() {
        String text = "Round 1: passed\nRound 2: follow up";
        assertThrows(ParseException.class,
                String.format(ParserUtil.MESSAGE_UNSUPPORTED_CHARACTER, "no/", 0x0A), () ->
                parser.parseCommand("note 1 no/" + text));
    }

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().withStatus(Status.APPLIED).build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_addWithStatus_success() throws Exception {
        Person person = new PersonBuilder().withStatus(Status.INTERVIEWING).build();

        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));

        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertEquals(new ClearCommand(false), parser.parseCommand(ClearCommand.COMMAND_WORD));
        assertEquals(new ClearCommand(true),
                parser.parseCommand(ClearCommand.COMMAND_WORD + " " + ClearCommand.CONFIRM_KEYWORD));
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(ClearCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_filter() throws Exception {
        FilterCommand command = (FilterCommand) parser.parseCommand(
                FilterCommand.COMMAND_WORD + " s/Interviewing");
        assertEquals(new FilterCommand(Status.INTERVIEWING), command);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_uppercaseAndMixedCaseCommands_preservesNameCase() throws Exception {
        for (String command : new String[] {"ADD_ROLE", "Add_Role"}) {
            assertEquals(new AddRoleCommand("Engineer"), parser.parseCommand(command + " N/Engineer"));
        }
        for (String command : new String[] {"EDIT_ROLE", "Edit_Role"}) {
            assertEquals(new EditRoleCommand("Engineer", "Designer"),
                    parser.parseCommand(command + " R/Engineer N/Designer"));
        }
        for (String command : new String[] {"DELETE_ROLE", "Delete_Role"}) {
            assertEquals(new DeleteRoleCommand("Engineer"), parser.parseCommand(command + " R/Engineer"));
        }
        for (String command : new String[] {"LIST_ROLE", "List_Role"}) {
            assertEquals(new ListRoleCommand(), parser.parseCommand(command));
        }
    }

    @Test
    public void parseCommand_lowercaseCommandAndUppercasePrefixes_preservesNameCase() throws Exception {
        assertEquals(new AddRoleCommand("Engineer"), parser.parseCommand("add_role N/Engineer"));
        assertEquals(new EditRoleCommand("Engineer", "Designer"),
                parser.parseCommand("edit_role R/Engineer N/Designer"));
        assertEquals(new DeleteRoleCommand("Engineer"), parser.parseCommand("delete_role R/Engineer"));
        assertEquals(new ListRoleCommand(), parser.parseCommand("list_role"));
    }

    @Test
    public void parseCommand_surroundingWhitespace_preservesInternalSpacing() throws Exception {
        assertEquals(new AddRoleCommand("Software  Engineer"),
                parser.parseCommand("  add_role   n/ Software  Engineer   "));
        assertEquals(new AddRoleCommand("Engineer"), parser.parseCommand("add_role\tn/Engineer"));
        assertEquals(new EditRoleCommand("Old", "New"), parser.parseCommand("edit_role\tr/Old\tn/New"));
    }
}
