package seedu.address.testutil;

import java.util.List;

import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;

/** Provides role assignments shared by role model and command tests. */
public class TypicalRoleAssignments {
    /** Returns a fresh model with an Engineer role assigned to one candidate. */
    public static ModelManager getAssignedModel(String... roleNames) {
        ModelManager model = new ModelManager();
        model.createRole("Engineer");
        Person person = new PersonBuilder().build();
        model.addPerson(person);
        model.replacePersonRoles(person, List.of(roleNames));
        return model;
    }
}
