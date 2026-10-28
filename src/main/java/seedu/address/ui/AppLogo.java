package seedu.address.ui;

import javafx.scene.text.Text;

/**
 * Displays the application's text logo independently of the surrounding header.
 */
public class AppLogo extends UiPart<Text> {
    private static final String FXML = "AppLogo.fxml";

    /**
     * Creates the HRvest text logo.
     */
    public AppLogo() {
        super(FXML);
    }
}
