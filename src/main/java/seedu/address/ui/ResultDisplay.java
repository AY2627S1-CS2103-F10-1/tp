package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final double MIN_HEIGHT = 90;
    private static final double MAX_HEIGHT = 190;
    private static final double MAX_SCENE_HEIGHT_FRACTION = 0.3;
    private static final double TEXT_PADDING_IN_EMS = 2;

    private final Text measuredText = new Text();
    private final InvalidationListener sizeListener = observable -> updateHeight();

    @FXML
    private TextArea resultDisplay;

    /** Creates a scrollable feedback display that adapts its height to its content and available window space. */
    public ResultDisplay() {
        super(FXML);
        getRoot().setMinHeight(Region.USE_PREF_SIZE);
        resultDisplay.textProperty().addListener(sizeListener);
        resultDisplay.widthProperty().addListener(sizeListener);
        resultDisplay.fontProperty().addListener(sizeListener);
        getRoot().sceneProperty().addListener((observable, previous, current) -> {
            if (previous != null) {
                previous.heightProperty().removeListener(sizeListener);
            }
            if (current != null) {
                current.heightProperty().addListener(sizeListener);
            }
            updateHeight();
        });
        updateHeight();
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
        resultDisplay.setScrollTop(0);
    }

    /** Sizes ordinary feedback compactly and gives wrapped or multi-line results more room without hiding the list. */
    private void updateHeight() {
        // Leave room for the text area's padding and scrollbar, even before its skin has been laid out.
        double textPadding = resultDisplay.getFont().getSize() * TEXT_PADDING_IN_EMS;
        measuredText.setFont(resultDisplay.getFont());
        measuredText.setText(resultDisplay.getText());
        measuredText.setWrappingWidth(Math.max(1, resultDisplay.getWidth() - textPadding));

        double heightLimit = MAX_HEIGHT;
        if (getRoot().getScene() != null) {
            heightLimit = Math.min(heightLimit, getRoot().getScene().getHeight() * MAX_SCENE_HEIGHT_FRACTION);
        }
        double contentHeight = measuredText.getLayoutBounds().getHeight() + textPadding;
        getRoot().setPrefHeight(Math.max(MIN_HEIGHT, Math.min(heightLimit, contentHeight)));
    }

}
