package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class ArgumentTokenizerTest {

    private final Prefix unknownPrefix = new Prefix("--u");
    private final Prefix pSlash = new Prefix("p/");
    private final Prefix dashT = new Prefix("-t");
    private final Prefix hatQ = new Prefix("^Q");

    @Test
    public void tokenize_nameConnectorsInName_keptInName() {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" n/Ravi S/O Kumar a/l Siti a/p Ahmad s/Offered"
                + " a/1 Road", CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_STATUS, CliSyntax.PREFIX_ADDRESS);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_NAME, "Ravi S/O Kumar a/l Siti a/p Ahmad");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_STATUS, "Offered");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ADDRESS, "1 Road");
    }

    @Test
    public void tokenize_nameConnectorOutsideName_treatedAsPrefix() {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" a/Blk 5 s/o Kumar",
                CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_STATUS, CliSyntax.PREFIX_ADDRESS);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ADDRESS, "Blk 5");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_STATUS, "o Kumar");
    }

    @Test
    public void tokenize_connectorLettersStartingValue_treatedAsPrefix() {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" n/Ravi s/Offered a/live Road s/o",
                CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_STATUS, CliSyntax.PREFIX_ADDRESS);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_NAME, "Ravi");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_STATUS, "Offered", "o");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ADDRESS, "live Road");
    }

    @Test
    public void tokenize_addressLikeConnectorWithoutOtherAddress_readAsAddress() {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" n/John a/L Street",
                CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_NAME, "John");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ADDRESS, "L Street");
    }

    @Test
    public void tokenizeWithValidation_addressLikeConnectorWithoutOtherAddress_throwsParseException() {
        assertThrows(ParseException.class,
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "A/P"), () ->
                ArgumentTokenizer.tokenizeWithValidation(" n/Siti A/P Ahmad", Set.of(),
                        CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS));
    }

    @Test
    public void tokenizeWithValidation_onlyConnectorLikeAddresses_throwsParseException() {
        assertThrows(ParseException.class,
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/p"), () ->
                ArgumentTokenizer.tokenizeWithValidation(" n/Siti a/p Ahmad a/L Street", Set.of(),
                        CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS));
    }

    @Test
    public void tokenizeWithValidation_connectorImmediatelyBeforeAddress_throwsParseException() {
        assertThrows(ParseException.class,
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/L"), () ->
                ArgumentTokenizer.tokenizeWithValidation(" n/John a/L Street a/9 Road", Set.of(),
                        CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS));
        assertThrows(ParseException.class,
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_NAME_CONNECTOR, "a/p"), () ->
                ArgumentTokenizer.tokenizeWithValidation(" n/Siti a/p Ahmad a/1 Road", Set.of(),
                        CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS));
    }

    @Test
    public void tokenizeWithValidation_fieldSeparatesConnectorAndAddress_keepsConnectorInName() throws Exception {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithValidation(
                " n/Siti a/p Ahmad p/91234567 a/1 Road", Set.of(),
                CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_PHONE, CliSyntax.PREFIX_ADDRESS);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_NAME, "Siti a/p Ahmad");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_PHONE, "91234567");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ADDRESS, "1 Road");
    }

    @Test
    public void tokenizeWithValidation_connectorLikeAddressBeforeName_readAsAddress() throws Exception {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithValidation(" a/L Street n/Siti a/p Ahmad",
                Set.of(), CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_NAME, "Siti a/p Ahmad");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ADDRESS, "L Street");
    }

    @Test
    public void tokenizeWithValidation_statusConnectorWithoutAddress_keptInName() throws Exception {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithValidation(" n/Ravi s/o Kumar", Set.of(),
                CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_STATUS, CliSyntax.PREFIX_ADDRESS);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_NAME, "Ravi s/o Kumar");
        assertArgumentAbsent(argMultimap, CliSyntax.PREFIX_STATUS);
    }

    @Test
    public void tokenizeWithValidation_statusConnectorBeforeAddress_keptInName() throws Exception {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithValidation(" n/Ravi S/O Kumar a/1 Road",
                Set.of(), CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_STATUS, CliSyntax.PREFIX_ADDRESS);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_NAME, "Ravi S/O Kumar");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ADDRESS, "1 Road");
        assertArgumentAbsent(argMultimap, CliSyntax.PREFIX_STATUS);
    }

    @Test
    public void tokenizeWithValidation_uppercasePrefixAfterLowercasePrefix_throwsParseException() {
        assertThrows(ParseException.class,
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_PREFIX, "N/", "n/", "N\\/"), () ->
                ArgumentTokenizer.tokenizeWithValidation(" a/Unit N/A", Set.of(),
                        CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS));
        // Tags can repeat, so starting another t/ field is still a valid fix.
        assertThrows(ParseException.class,
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_PREFIX, "T/", "t/", "T\\/"), () ->
                ArgumentTokenizer.tokenizeWithValidation(" t/java T/backend", Set.of(),
                        CliSyntax.PREFIX_TAG));
    }

    @Test
    public void tokenizeWithValidation_uppercasePrefixAlreadyGiven_suggestsOnlyEscape() {
        assertThrows(ParseException.class,
                String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_REPEATED_PREFIX, "N/", "n/", "N\\/"), () ->
                ArgumentTokenizer.tokenizeWithValidation(" n/Bob a/Unit N/A", Set.of(),
                        CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS));
        assertThrows(ParseException.class, String.format(ArgumentTokenizer.MESSAGE_AMBIGUOUS_REPEATED_PREFIX,
                "No/", "no/", "No\\/"), () ->
                ArgumentTokenizer.tokenizeWithValidation(" no/Status No/go", Set.of(), CliSyntax.PREFIX_NOTE));
    }

    @Test
    public void tokenizeWithValidation_ambiguityAfterRoleOrStatus_qualifiesEscapeAdvice() {
        String expected = "N/ in a value could be text or the n/ prefix. Use n/ to start a new field. "
                + "For text in a name, phone, email, address or note, type N\\/. "
                + "Roles and statuses keep backslashes literally.";
        for (String arguments : List.of(" r/ N/A", " s/Applied N/A")) {
            assertThrows(ParseException.class, expected, () -> ArgumentTokenizer.tokenizeWithValidation(
                    arguments, Set.of(CliSyntax.PREFIX_NAME), CliSyntax.PREFIX_NAME,
                    CliSyntax.PREFIX_ROLE, CliSyntax.PREFIX_STATUS));
        }
    }

    @Test
    public void tokenizeWithValidation_repeatedPrefixAfterRole_qualifiesEscapeWithoutOfferingDuplicate() {
        String expected = "N/ in a value could be text or another n/ prefix, but n/ can be given only once. "
                + "For text in a name, phone, email, address or note, type N\\/. "
                + "Roles and statuses keep backslashes literally.";
        assertThrows(ParseException.class, expected, () -> ArgumentTokenizer.tokenizeWithValidation(
                " n/Alice r/ N/A", Set.of(CliSyntax.PREFIX_NAME), CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ROLE));
    }

    @Test
    public void tokenizeWithValidation_consistentPrefixCase_noAmbiguity() throws Exception {
        // Upper-case prefixes still work when nothing typed in lower case precedes them.
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithValidation(" N/John A/Unit 5 p/123",
                Set.of(), CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS, CliSyntax.PREFIX_PHONE);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_NAME, "John");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ADDRESS, "Unit 5");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_PHONE, "123");
    }

    @Test
    public void tokenizeWithValidation_escapedSlash_keptAsTextInValidatedValues() throws Exception {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeWithValidation(
                " a/Unit N\\/A n\\/a n/Amy r/SRE\\/Ops", Set.of(CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS),
                CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_ADDRESS, CliSyntax.PREFIX_ROLE);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ADDRESS, "Unit N/A n/a");
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_NAME, "Amy");
        // Role names are matched as typed, so an existing role containing \/ stays reachable.
        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ROLE, "SRE\\/Ops");
    }

    @Test
    public void tokenize_escapedSlash_keptAsTyped() {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" r/SRE\\/Ops", CliSyntax.PREFIX_ROLE);

        assertArgumentPresent(argMultimap, CliSyntax.PREFIX_ROLE, "SRE\\/Ops");
    }

    @Test
    public void tokenize_emptyArgsString_noValues() {
        String argsString = "  ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);

        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);
    }

    private void assertPreamblePresent(ArgumentMultimap argMultimap, String expectedPreamble) {
        assertEquals(expectedPreamble, argMultimap.getPreamble());
    }

    private void assertPreambleEmpty(ArgumentMultimap argMultimap) {
        assertTrue(argMultimap.getPreamble().isEmpty());
    }

    /**
     * Asserts all the arguments in {@code argMultimap} with {@code prefix} match the {@code expectedValues}
     * and only the last value is returned upon calling {@code ArgumentMultimap#getValue(Prefix)}.
     */
    private void assertArgumentPresent(ArgumentMultimap argMultimap, Prefix prefix, String... expectedValues) {

        // Verify the last value is returned
        assertEquals(expectedValues[expectedValues.length - 1], argMultimap.getValue(prefix).get());

        // Verify the number of values returned is as expected
        assertEquals(expectedValues.length, argMultimap.getAllValues(prefix).size());

        // Verify all values returned are as expected and in order
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], argMultimap.getAllValues(prefix).get(i));
        }
    }

    private void assertArgumentAbsent(ArgumentMultimap argMultimap, Prefix prefix) {
        assertFalse(argMultimap.getValue(prefix).isPresent());
    }

    @Test
    public void tokenize_noPrefixes_allTakenAsPreamble() {
        String argsString = "  some random string /t tag with leading and trailing spaces ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString);

        // Same string expected as preamble, but leading/trailing spaces should be trimmed
        assertPreamblePresent(argMultimap, argsString.trim());

    }

    @Test
    public void tokenize_oneArgument() {
        // Preamble present
        String argsString = "  Some preamble string p/ Argument value ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreamblePresent(argMultimap, "Some preamble string");
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

        // No preamble
        argsString = " p/   Argument value ";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

    }

    @Test
    public void tokenize_multipleArguments() {
        // Only two arguments are present
        String argsString = "SomePreambleString -t dashT-Value p/pSlash value";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentAbsent(argMultimap, hatQ);

        // All three arguments are present
        argsString = "Different Preamble String ^Q111 -t dashT-Value p/pSlash value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "Different Preamble String");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentPresent(argMultimap, hatQ, "111");

        /* Also covers: Reusing of the tokenizer multiple times */

        // Reuse tokenizer on an empty string to ensure ArgumentMultimap is correctly reset
        // (i.e. no stale values from the previous tokenizing remain)
        argsString = "";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);

        /* Also covers: testing for prefixes not specified as a prefix */

        // Prefixes not previously given to the tokenizer should not return any values
        argsString = unknownPrefix + "some value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertArgumentAbsent(argMultimap, unknownPrefix);
        assertPreamblePresent(argMultimap, argsString); // Unknown prefix is taken as part of preamble
    }

    @Test
    public void tokenize_multipleArgumentsWithRepeats() {
        // Two arguments repeated, some have empty values
        String argsString = "SomePreambleString -t dashT-Value ^Q ^Q -t another dashT value p/ pSlash value -t";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value", "another dashT value", "");
        assertArgumentPresent(argMultimap, hatQ, "", "");
    }

    @Test
    public void tokenize_prefixInDifferentCase_treatedAsSamePrefix() {
        Prefix noteSlash = new Prefix("no/");
        String argsString = "Preamble P/upper p/lower NO/note No/mixed";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, noteSlash);

        assertPreamblePresent(argMultimap, "Preamble");
        assertArgumentPresent(argMultimap, pSlash, "upper", "lower");
        assertArgumentPresent(argMultimap, noteSlash, "note", "mixed");
    }

    @Test
    public void tokenize_prefixInDifferentCaseWithoutWhitespace_takenAsValue() {
        String argsString = "Preamble p/one/P/two-P/three";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);

        assertPreamblePresent(argMultimap, "Preamble");
        assertArgumentPresent(argMultimap, pSlash, "one/P/two-P/three");
    }

    @Test
    public void tokenize_multipleArgumentsJoined() {
        String argsString = "SomePreambleStringp/ pSlash joined-tjoined -t not joined^Qjoined";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleStringp/ pSlash joined-tjoined");
        assertArgumentAbsent(argMultimap, pSlash);
        assertArgumentPresent(argMultimap, dashT, "not joined^Qjoined");
        assertArgumentAbsent(argMultimap, hatQ);
    }

    @Test
    public void equalsMethod() {
        Prefix aaa = new Prefix("aaa");

        assertEquals(aaa, aaa);
        assertEquals(aaa, new Prefix("aaa"));

        assertNotEquals(aaa, "aaa");
        assertNotEquals(aaa, new Prefix("aab"));
    }

}
