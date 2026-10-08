package seedu.address.logic;

import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND]");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        Model executionModel = command instanceof NoteCommand ? createNoteUpdateModel() : model;
        commandResult = command.execute(executionModel);
        saveModel(executionModel);

        if (command instanceof NoteCommand) {
            model.setAddressBook(executionModel.getAddressBook());
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        }

        return commandResult;
    }

    /**
     * Saves the model and translates storage failures into user-facing command errors.
     */
    private void saveModel(Model modelToSave) throws CommandException {
        try {
            storage.saveAddressBook(modelToSave.getAddressBook());
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }
    }

    /**
     * Creates a temporary model with the current displayed candidates so a failed note save leaves memory unchanged.
     */
    private Model createNoteUpdateModel() {
        Model stagedModel = new ModelManager(model.getAddressBook(), model.getUserPrefs());
        List<Person> displayedCandidates = List.copyOf(model.getFilteredPersonList());
        stagedModel.updateFilteredPersonList(displayedCandidates::contains);
        return stagedModel;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
