package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;

class ListRoleCommandTest {
    @Test
    void execute_unusedRoles_listsAllInPredictableOrder() throws Exception {
        ModelManager model = new ModelManager();
        model.createRole("Zoologist");
        model.createRole("Analyst");

        assertEquals("Roles:\nAnalyst\nNo role\nZoologist",
                new ListRoleCommand().execute(model).getFeedbackToUser());
    }

    @Test
    void equalsHashCodeAndToString_sameAndDifferentArguments_preservesValueSemantics() {
        assertEquals(new ListRoleCommand(), new ListRoleCommand());
        assertEquals(new ListRoleCommand().hashCode(), new ListRoleCommand().hashCode());
        assertEquals("list_role", new ListRoleCommand().toString());
    }

}
