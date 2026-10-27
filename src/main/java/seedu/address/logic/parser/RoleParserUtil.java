package seedu.address.logic.parser;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.logic.parser.exceptions.ParseException;

/** Shares argument validation for role commands with named parameters. */
final class RoleParserUtil {
    private static final Pattern PARAMETER = Pattern.compile("(?i)(?<!\\S)(n/|r/|p/|e/|a/|t/|no/|s/)");

    private RoleParserUtil() {
    }

    /** Tokenizes allowed prefixes, rejecting duplicates and unexpected preambles or parameters. */
    static ArgumentMultimap parseArguments(String arguments, String usage, Prefix... prefixes) throws ParseException {
        ArgumentMultimap values = ArgumentTokenizer.tokenize(normalizePrefixes(arguments, usage, prefixes), prefixes);
        values.verifyNoDuplicatePrefixesFor(prefixes);
        if (!values.getPreamble().isEmpty()) {
            throw invalidFormat(usage);
        }
        return values;
    }

    /** Returns a required, validated display name. */
    static String requireName(ArgumentMultimap values, Prefix prefix, String usage) throws ParseException {
        return ParserUtil.parseRoleName(values.getValue(prefix).orElseThrow(() -> invalidFormat(usage)));
    }

    static ParseException invalidFormat(String usage) {
        return new ParseException("Invalid role command format. Usage: " + usage);
    }

    /** Canonicalizes prefix case and whitespace while rejecting unsupported parameters. */
    private static String normalizePrefixes(String arguments, String usage, Prefix... prefixes) throws ParseException {
        Set<String> allowed = Arrays.stream(prefixes).map(Prefix::getPrefix)
                .collect(Collectors.toSet());
        Matcher matcher = PARAMETER.matcher(arguments);
        StringBuilder canonical = new StringBuilder();
        while (matcher.find()) {
            String prefix = matcher.group().toLowerCase(Locale.ROOT);
            if (!allowed.contains(prefix)) {
                throw new ParseException("Unexpected role parameter: " + matcher.group() + ". Usage: " + usage);
            }
            matcher.appendReplacement(canonical, Matcher.quoteReplacement(" " + prefix));
        }
        matcher.appendTail(canonical);
        return canonical.toString();
    }

}
