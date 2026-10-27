package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.scene.text.Text;

public class AppLogoTest {
    @Test
    public void constructor_standaloneLogo_displaysStyledAppName() {
        Text logo = new AppLogo().getRoot();

        assertEquals("HRVest", logo.getText());
        assertEquals("logo", logo.getId());
        assertTrue(logo.getStyleClass().contains("header-logo"));
        assertTrue(logo.isVisible());
        assertTrue(logo.isManaged());
        assertTrue(logo.getLayoutBounds().getWidth() > 0);
    }
}
