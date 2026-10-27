package seedu.address.commons.core;

import java.util.List;

/** Defines the shared candidate CSV schema for import and export. */
public final class CandidateCsvFormat {
    /** Lists column names in their required CSV order. */
    public static final List<String> HEADERS = List.of("name", "phone", "email", "address", "status", "tags", "note");

    private CandidateCsvFormat() {
    }
}
