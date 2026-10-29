package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tokenizes arguments string of the form: {@code preamble <prefix>value <prefix>value ...}<br>
 *     e.g. {@code some preamble text t/ 11.00 t/12.00 k/ m/ July}  where prefixes are {@code t/ k/ m/}.<br>
 * 1. An argument's value can be an empty string e.g. the value of {@code k/} in the above example.<br>
 * 2. Leading and trailing whitespaces of an argument value will be discarded.<br>
 * 3. An argument may be repeated and all its values will be accumulated e.g. the value of {@code t/}
 *    in the above example.<br>
 * 4. Prefixes are recognised regardless of letter case e.g. {@code T/} is treated as {@code t/}.<br>
 * 5. Within a name, the connector {@code s/o} is part of the name, not a prefix. So are {@code a/l} and
 *    {@code a/p} when another {@code a/} field gives the address.<br>
 */
public class ArgumentTokenizer {

    public static final String MESSAGE_AMBIGUOUS_NAME_CONNECTOR = "%1$s after a name could start the address or be "
            + "part of the name, as in Siti a/p Ahmad. To set the address, put a/ADDRESS before n/NAME. "
            + "To keep %1$s in the name, also give the address with a/ADDRESS.";

    // Legal names such as "Ravi s/o Kumar" use these connectors, which look like the s/ and a/ prefixes.
    private static final Set<String> NAME_CONNECTORS = Set.of("s/o", "a/l", "a/p");
    private static final Set<String> ADDRESS_LIKE_CONNECTORS = Set.of("a/l", "a/p");
    private static final int NAME_CONNECTOR_LENGTH = 3;

    /**
     * Tokenizes an arguments string and returns an {@code ArgumentMultimap} object that maps prefixes to their
     * respective argument values. Only the given prefixes will be recognized in the arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to tokenize the arguments string with
     * @return           ArgumentMultimap object that maps prefixes to their arguments
     */
    public static ArgumentMultimap tokenize(String argsString, Prefix... prefixes) {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        return trimArguments(extractArguments(argsString, positions), prefixes);
    }

    /**
     * Tokenizes arguments, validating selected prefix values before trimming can discard unsupported characters.
     * Preambles and unselected prefixes retain their existing validation rules.
     *
     * @throws ParseException If a selected value contains a character outside printable ASCII, or if an
     *     {@code a/l} or {@code a/p} after a name could be either the address or part of the name.
     */
    public static ArgumentMultimap tokenizeWithValidation(String argsString, Set<Prefix> validatedPrefixes,
            Prefix... prefixes) throws ParseException {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        Optional<String> ambiguousConnector = findAmbiguousNameConnector(argsString, positions);
        if (ambiguousConnector.isPresent()) {
            throw new ParseException(String.format(MESSAGE_AMBIGUOUS_NAME_CONNECTOR, ambiguousConnector.get()));
        }
        ArgumentMultimap rawArguments = extractArguments(argsString, positions);
        for (Prefix prefix : prefixes) {
            if (validatedPrefixes.contains(prefix)) {
                for (String value : rawArguments.getAllValues(prefix)) {
                    ParserUtil.verifyPrintableAscii(value, prefix.getPrefix());
                }
            }
        }
        return trimArguments(rawArguments, prefixes);
    }

    /** Returns a copy with surrounding whitespace removed, preserving the existing tokenizer contract. */
    private static ArgumentMultimap trimArguments(ArgumentMultimap rawArguments, Prefix... prefixes) {
        ArgumentMultimap trimmedArguments = new ArgumentMultimap();
        trimmedArguments.put(new Prefix(""), rawArguments.getPreamble().trim());
        for (Prefix prefix : Arrays.stream(prefixes).distinct().toList()) {
            for (String value : rawArguments.getAllValues(prefix)) {
                trimmedArguments.put(prefix, value.trim());
            }
        }
        return trimmedArguments;
    }

    /**
     * Finds all zero-based prefix positions in the given arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to find in the arguments string
     * @return           List of zero-based prefix positions in the given arguments string
     */
    private static List<PrefixPosition> findAllPrefixPositions(String argsString, Prefix... prefixes) {
        return Arrays.stream(prefixes)
                .flatMap(prefix -> findPrefixPositions(argsString, prefix).stream())
                .collect(Collectors.toList());
    }

    /**
     * @see #findAllPrefixPositions(String, Prefix...)
     */
    private static List<PrefixPosition> findPrefixPositions(String argsString, Prefix prefix) {
        List<PrefixPosition> positions = new ArrayList<>();

        int prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), 0);
        while (prefixPosition != -1) {
            PrefixPosition extendedPrefix = new PrefixPosition(prefix, prefixPosition);
            positions.add(extendedPrefix);
            prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), prefixPosition);
        }

        return positions;
    }

    /**
     * Returns the index of the first occurrence of {@code prefix} in
     * {@code argsString} starting from index {@code fromIndex}. An occurrence
     * is valid if there is a whitespace before {@code prefix}. Letter case is
     * ignored, so "P/" is an occurrence of "p/". Returns -1 if no such
     * occurrence can be found.
     *
     * E.g if {@code argsString} = "e/hip/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns -1 as there are no valid
     * occurrences of "p/" with whitespace before it. However, if
     * {@code argsString} = "e/hi p/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns 5.
     */
    private static int findPrefixPosition(String argsString, String prefix, int fromIndex) {
        String whitespaceAndPrefix = " " + prefix;
        int lastStart = argsString.length() - whitespaceAndPrefix.length();
        for (int start = fromIndex; start <= lastStart; start++) {
            if (argsString.regionMatches(true, start, whitespaceAndPrefix, 0, whitespaceAndPrefix.length())) {
                return start + 1; // +1 as offset for whitespace
            }
        }
        return -1;
    }

    /**
     * Extracts prefixes and their argument values, and returns an {@code ArgumentMultimap} object that maps the
     * extracted prefixes to their respective arguments. Prefixes are extracted based on their zero-based positions in
     * {@code argsString}.
     *
     * @param argsString      Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixPositions Zero-based positions of all prefixes in {@code argsString}
     * @return                ArgumentMultimap object that maps prefixes to their arguments
     */
    private static ArgumentMultimap extractArguments(String argsString, List<PrefixPosition> prefixPositions) {

        // Sort by start position
        prefixPositions.sort((prefix1, prefix2) -> prefix1.getStartPosition() - prefix2.getStartPosition());
        List<PrefixPosition> fieldPositions = removeNameConnectors(argsString, prefixPositions);

        // Insert a PrefixPosition to represent the preamble
        PrefixPosition preambleMarker = new PrefixPosition(new Prefix(""), 0);
        fieldPositions.addFirst(preambleMarker);

        // Add a dummy PrefixPosition to represent the end of the string
        PrefixPosition endPositionMarker = new PrefixPosition(new Prefix(""), argsString.length());
        fieldPositions.add(endPositionMarker);

        // Map prefixes to their argument values (if any)
        ArgumentMultimap argMultimap = new ArgumentMultimap();
        for (int i = 0; i < fieldPositions.size() - 1; i++) {
            // Extract and store prefixes and their arguments
            Prefix argPrefix = fieldPositions.get(i).getPrefix();
            String argValue = extractArgumentValue(argsString, fieldPositions.get(i), fieldPositions.get(i + 1));
            argMultimap.put(argPrefix, argValue);
        }

        return argMultimap;
    }

    /**
     * Returns the sorted {@code prefixPositions} without name connectors inside a name value, so that the
     * {@code s/o} in {@code n/Ravi s/o Kumar} stays in the name.
     */
    private static List<PrefixPosition> removeNameConnectors(String argsString,
            List<PrefixPosition> prefixPositions) {
        List<PrefixPosition> candidates = findNameConnectorCandidates(argsString, prefixPositions);
        boolean hasAddressField = hasAddressField(prefixPositions, candidates);
        List<PrefixPosition> keptPositions = new ArrayList<>();
        for (PrefixPosition position : prefixPositions) {
            // No status is "o", so s/o is always a connector. An address can start with "L " or "P ", so a/l and
            // a/p are connectors only when a separate address field gives the address.
            boolean isConnector = candidates.contains(position)
                    && (!isAddressLikeConnector(argsString, position) || hasAddressField);
            if (!isConnector) {
                keptPositions.add(position);
            }
        }
        return keptPositions;
    }

    /**
     * Returns the sorted {@code prefixPositions} that could be name connectors because they continue a name value,
     * e.g. both {@code a/p} and {@code a/L} in {@code n/Siti a/p Ahmad a/L Street}.
     */
    private static List<PrefixPosition> findNameConnectorCandidates(String argsString,
            List<PrefixPosition> prefixPositions) {
        List<PrefixPosition> candidates = new ArrayList<>();
        Prefix currentFieldPrefix = null;
        for (PrefixPosition position : prefixPositions) {
            boolean isInsideName = PREFIX_NAME.equals(currentFieldPrefix);
            if (isInsideName && findConnectorAt(argsString, position.getStartPosition()).isPresent()) {
                candidates.add(position);
            } else {
                currentFieldPrefix = position.getPrefix();
            }
        }
        return candidates;
    }

    /**
     * Returns the text of the first {@code a/l} or {@code a/p} that continues a name when no separate address
     * field exists, so it could be either the address or part of the name.
     */
    private static Optional<String> findAmbiguousNameConnector(String argsString,
            List<PrefixPosition> prefixPositions) {
        List<PrefixPosition> sortedPositions = new ArrayList<>(prefixPositions);
        sortedPositions.sort(Comparator.comparingInt(PrefixPosition::getStartPosition));
        List<PrefixPosition> candidates = findNameConnectorCandidates(argsString, sortedPositions);
        if (hasAddressField(sortedPositions, candidates)) {
            return Optional.empty();
        }
        return candidates.stream()
                .filter(candidate -> isAddressLikeConnector(argsString, candidate))
                .findFirst()
                .map(candidate -> argsString.substring(candidate.getStartPosition(),
                        candidate.getStartPosition() + NAME_CONNECTOR_LENGTH));
    }

    /** Returns whether an address field exists that is not itself a possible name connector. */
    private static boolean hasAddressField(List<PrefixPosition> positions, List<PrefixPosition> candidates) {
        return positions.stream()
                .anyMatch(position -> position.getPrefix().equals(PREFIX_ADDRESS) && !candidates.contains(position));
    }

    /** Returns whether {@code position} starts {@code a/l} or {@code a/p} followed by whitespace. */
    private static boolean isAddressLikeConnector(String argsString, PrefixPosition position) {
        return findConnectorAt(argsString, position.getStartPosition())
                .filter(ADDRESS_LIKE_CONNECTORS::contains).isPresent();
    }

    /** Returns the lower-case name connector followed by whitespace that starts at {@code start}, if any. */
    private static Optional<String> findConnectorAt(String argsString, int start) {
        int end = start + NAME_CONNECTOR_LENGTH;
        if (end >= argsString.length() || !Character.isWhitespace(argsString.charAt(end))) {
            return Optional.empty();
        }
        String text = argsString.substring(start, end).toLowerCase(Locale.ROOT);
        return NAME_CONNECTORS.contains(text) ? Optional.of(text) : Optional.empty();
    }

    /**
     * Returns the untrimmed value of the argument in the arguments string specified by {@code currentPrefixPosition}.
     * The end position of the value is determined by {@code nextPrefixPosition}.
     */
    private static String extractArgumentValue(String argsString,
                                        PrefixPosition currentPrefixPosition,
                                        PrefixPosition nextPrefixPosition) {
        Prefix prefix = currentPrefixPosition.getPrefix();

        int valueStartPos = currentPrefixPosition.getStartPosition() + prefix.getPrefix().length();
        String value = argsString.substring(valueStartPos, nextPrefixPosition.getStartPosition());

        return value;
    }

    /**
     * Represents a prefix's position in an arguments string.
     */
    private static class PrefixPosition {
        private int startPosition;
        private final Prefix prefix;

        PrefixPosition(Prefix prefix, int startPosition) {
            this.prefix = prefix;
            this.startPosition = startPosition;
        }

        int getStartPosition() {
            return startPosition;
        }

        Prefix getPrefix() {
            return prefix;
        }
    }

}
