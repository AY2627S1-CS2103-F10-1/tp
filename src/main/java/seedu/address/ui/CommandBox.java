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
    // True when the current text was typed by the user (not written by applyHistoryEntry
    // or cleared by a successful command). Lets us distinguish an unsubmitted draft from
    // text that merely echoes a history entry.
    private boolean isEditingDraft = true;
    private boolean isApplyingProgrammatic = false;

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
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> {
            setStyleToDefault();
            if (!isApplyingProgrammatic) {
                isEditingDraft = true;
            }
        });
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
            applyProgrammaticText("");
            isEditingDraft = true;
        } catch (CommandException | ParseException e) {
            // Keep the failed text so that the user can correct it. Its history entry counts as already shown,
            // so pressing Up shows the command before it instead of appearing to do nothing.
            if (!commandText.isBlank()) {
                history.moveCursorToNewest();
            }
            isEditingDraft = false;
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Intercepts Up and Down arrow key presses to walk through command history.
     * Consumes the event so the caret does not move within the text field.
     */
    private void handleArrowNavigation(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            handleUp();
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            if (handleDown()) {
                event.consume();
            }
        }
    }

    /**
     * Captures the current text as a draft when the user has edited it, then
     * walks one step back in history. Leaves the text field unchanged when the
     * cursor is already at the oldest entry; the caller still consumes the key event.
     */
    private void handleUp() {
        if (!history.canGoBack()) {
            return;
        }
        captureDraftIfEdited();
        history.previous().ifPresent(this::applyHistoryEntry);
    }

    /**
     * Walks one step forward in history. When the cursor reaches past-the-end,
     * restores the saved draft (if any) and marks the restored text as a fresh
     * draft so another Up/Down cycle can re-capture it. Returns true when
     * navigation occurred so the caller can consume the key event; false when
     * the cursor is already past the newest entry.
     */
    private boolean handleDown() {
        if (!history.canGoForward()) {
            return false;
        }
        captureDraftIfEdited();
        Optional<String> entry = history.next();
        if (entry.get().isEmpty() && draft.isPresent()) {
            applyHistoryEntry(draft.get());
            draft = Optional.empty();
            // The restored text is the user's draft. Keep it eligible for re-capture
            // so another Up/Down cycle does not lose it again.
            isEditingDraft = true;
        } else {
            applyHistoryEntry(entry.get());
        }
        return true;
    }

    /**
     * Saves the current text as the pending draft when it reflects a genuine user edit
     * and no draft is already stored. Called before each navigation step.
     */
    private void captureDraftIfEdited() {
        if (draft.isEmpty() && isEditingDraft && !commandTextField.getText().isEmpty()) {
            draft = Optional.of(commandTextField.getText());
        }
    }

    private void applyHistoryEntry(String entry) {
        applyProgrammaticText(entry);
        commandTextField.positionCaret(entry.length());
        isEditingDraft = false;
    }

    /**
     * Writes to the text field without the listener counting it as a user edit,
     * so a recall or clear does not shadow the real draft state.
     */
    private void applyProgrammaticText(String text) {
        isApplyingProgrammatic = true;
        try {
            commandTextField.setText(text);
        } finally {
            isApplyingProgrammatic = false;
        }
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
