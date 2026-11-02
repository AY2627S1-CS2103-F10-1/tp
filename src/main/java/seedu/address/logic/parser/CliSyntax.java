package seedu.address.logic.parser;

import seedu.address.commons.core.CommandPrefixes;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_ROLE = new Prefix(CommandPrefixes.ROLE);
    public static final Prefix PREFIX_NAME = new Prefix(CommandPrefixes.NAME);
    public static final Prefix PREFIX_PHONE = new Prefix(CommandPrefixes.PHONE);
    public static final Prefix PREFIX_EMAIL = new Prefix(CommandPrefixes.EMAIL);
    public static final Prefix PREFIX_ADDRESS = new Prefix(CommandPrefixes.ADDRESS);
    public static final Prefix PREFIX_TAG = new Prefix(CommandPrefixes.TAG);
    public static final Prefix PREFIX_NOTE = new Prefix(CommandPrefixes.NOTE);
    public static final Prefix PREFIX_STATUS = new Prefix(CommandPrefixes.STATUS);

}
