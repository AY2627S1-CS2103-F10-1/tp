package seedu.address.commons.core;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Defines shared parameter text without coupling domain validation to command parsers. */
public final class CommandPrefixes {
    public static final String NAME = "n/";
    public static final String ROLE = "r/";
    public static final String PHONE = "p/";
    public static final String EMAIL = "e/";
    public static final String ADDRESS = "a/";
    public static final String TAG = "t/";
    public static final String NOTE = "no/";
    public static final String STATUS = "s/";
    public static final List<String> ALL = List.of(NAME, ROLE, PHONE, EMAIL, ADDRESS, TAG, NOTE, STATUS);
    public static final String REGEX = ALL.stream().map(Pattern::quote).collect(Collectors.joining("|"));
    public static final String DESCRIPTION = String.join(", ", ALL.subList(0, ALL.size() - 1))
            + ", or " + ALL.getLast();

    private CommandPrefixes() {
    }
}
