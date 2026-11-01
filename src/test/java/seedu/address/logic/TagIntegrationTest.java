package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.CandidateCsvParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.role.RoleCatalog;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.LogicTestFixture;
import seedu.address.testutil.PersonBuilder;

/** Exercises technical skill tags through commands and real JSON/CSV storage. */
public class TagIntegrationTest {
    @TempDir
    public Path directory;

    @TestFactory
    public Stream<DynamicTest> execute_editTechnicalSkill_storesExactTag() {
        return Stream.of("C++", "C#", ".NET", "Node.js", "machine learning")
                .map(skill -> dynamicTest(skill, () -> {
                    ModelManager model = new ModelManager();
                    model.addPerson(new PersonBuilder().build());
                    LogicTestFixture fixture = new LogicTestFixture(
                            Files.createTempDirectory(directory, "skill"), model);

                    fixture.getLogic().execute("edit 1 t/" + skill);

                    assertEquals(Set.of(skill), tagNames(model.getAddressBook().getPersonList().getFirst()));
                    assertEquals(model.getAddressBook(), fixture.readSaved());
                }));
    }

    @Test
    public void execute_addAndEditTags_trimsDeduplicatesAndPreservesCase() throws Exception {
        ModelManager model = new ModelManager();
        LogicTestFixture fixture = new LogicTestFixture(directory, model);
        fixture.getLogic().execute("add n/Alice p/123 e/alice@example.com a/Office r/No role"
                + " t/ C++  t/C++ t/c++ t/machine  learning t/.NET t/Node.js t/C#");
        Set<String> expected = Set.of("C++", "c++", "machine  learning", ".NET", "Node.js", "C#");
        assertEquals(expected, tagNames(model.getAddressBook().getPersonList().getFirst()));

        fixture.getLogic().execute("edit 1 t/ C++  t/C++ t/c++ t/machine  learning t/.NET t/Node.js t/C#");

        assertEquals(expected, tagNames(model.getAddressBook().getPersonList().getFirst()));
        assertEquals(model.getAddressBook(), fixture.readSaved());
    }

    @Test
    public void execute_bareTag_clearsOnEditButRejectsOnAdd() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withTags("C++", "machine learning").build());
        LogicTestFixture fixture = new LogicTestFixture(directory, model);

        fixture.getLogic().execute("edit 1 t/   ");

        assertEquals(Set.of(), tagNames(model.getAddressBook().getPersonList().getFirst()));
        assertEquals(model.getAddressBook(), fixture.readSaved());
        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () -> fixture.getLogic().execute(
                "add n/Bob p/123 e/bob@example.com a/Office r/No role t/   "));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_tagFollowedByPrefix_keepsCommandBoundary() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        LogicTestFixture fixture = new LogicTestFixture(directory, model);

        fixture.getLogic().execute("edit 1 t/C/C++ t/machine learning n/Alice");

        Person candidate = model.getAddressBook().getPersonList().getFirst();
        assertEquals(Set.of("C/C++", "machine learning"), tagNames(candidate));
        assertEquals("Alice", candidate.getName().fullName);
    }

    @Test
    public void execute_technicalSkills_roundTripsJsonAndCsvEscaping() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        LogicTestFixture fixture = new LogicTestFixture(directory, model);
        Set<String> expected = Set.of("C++", "C#", ".NET", "Node.js", "machine learning",
                "quoted \"skill\"", "tools\\sdk", "comma,value");
        fixture.getLogic().execute("edit 1 t/C++ t/C# t/.NET t/Node.js t/machine learning"
                + " t/quoted \"skill\" t/tools\\sdk t/comma,value");

        assertEquals(expected, tagNames(fixture.readSaved().getPersonList().getFirst()));
        fixture.getLogic().execute("export");
        var imported = CandidateCsvParser.parse(Files.readAllBytes(directory.resolve("export.csv")), new RoleCatalog());
        assertEquals(model.getAddressBook().getPersonList(), imported);
        assertEquals(expected, tagNames(imported.getFirst()));
    }

    private static Set<String> tagNames(Person person) {
        return person.getTags().stream().map(tag -> tag.tagName).collect(Collectors.toSet());
    }
}
