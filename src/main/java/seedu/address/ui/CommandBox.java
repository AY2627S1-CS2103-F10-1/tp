package seedu.address.ui;

import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";

    private final CommandExecutor commandExecutor;
    private final CommandHistory history;
    private Optional<String> draft = Optional.empty();

    @FXML
    private TextField commandTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        this(commandExecutor, new CommandHistory());
    }

    /**
     * Creates a {@code CommandBox} with an injected {@code CommandHistory}, for tests
     * that want to seed or inspect history state.
     */
    CommandBox(CommandExecutor commandExecutor, CommandHistory history) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        this.history = history;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleArrowNavigation);
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        // Record first so the user can press Up to recover the command, successful or not.
        history.add(commandText);
        draft = Optional.empty();

        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            // Clear the failed text so pressing Up immediately after shows it from history,
            // instead of appearing to do nothing because the field already holds that text.
            commandTextField.setText("");
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Intercepts Up and Down arrow key presses to walk through command history.
     * Consumes the event so the caret does not move within the text field.
     */
    private void handleArrowNavigation(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            captureDraftIfStartingNavigation();
            history.previous().ifPresent(this::applyHistoryEntry);
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            Optional<String> entry = history.next();
            if (entry.isEmpty()) {
                return;
            }
            if (entry.get().isEmpty() && draft.isPresent()) {
                applyHistoryEntry(draft.get());
                draft = Optional.empty();
            } else {
                applyHistoryEntry(entry.get());
            }
            event.consume();
        }
    }

    private void captureDraftIfStartingNavigation() {
        if (draft.isEmpty() && history.getCursor() == history.getEntries().size()
                && !commandTextField.getText().isEmpty()) {
            draft = Optional.of(commandTextField.getText());
        }
    }

    private void applyHistoryEntry(String entry) {
        commandTextField.setText(entry);
        commandTextField.positionCaret(entry.length());
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
