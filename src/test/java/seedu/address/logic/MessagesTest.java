package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.model.role.Role;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {
    @Test
    public void format_mixedCaseRoles_usesCaseInsensitiveDisplayOrder() {
        Role analyst = new Role("00000000-0000-0000-0000-000000000001", "analyst");
        Role zebra = new Role("00000000-0000-0000-0000-000000000002", "Zebra");
        Person person = new PersonBuilder().withRoles(zebra, analyst).build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
                + "Address: 123, Jurong West Ave 6, #08-111; Status: Applied; Roles: analyst, Zebra; Tags: ",
                Messages.format(person));
    }

    @Test
    public void format_defaultStatusNoTags_includesAppliedStatus() {
        Person person = new PersonBuilder().withTags().build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
                + "Address: 123, Jurong West Ave 6, #08-111; Status: Applied; Roles: No role; Tags: ",
                Messages.format(person));
    }

    @Test
    public void format_explicitStatusAndTag_includesStatusAndTag() {
        Person person = new PersonBuilder().withStatus(Status.INTERVIEWING).withTags("backend").build();

        assertEquals("Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
                + "Address: 123, Jurong West Ave 6, #08-111; Status: Interviewing; "
                + "Roles: No role; Tags: [backend]",
                Messages.format(person));
    }
}
