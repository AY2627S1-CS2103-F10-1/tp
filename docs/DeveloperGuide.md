---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# HRvest Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

<box type="tip">

**Tip:** Click a panel title (a line with an arrow) to expand it. Panels hold implementation detail that most readers can skip. Their text is hidden until you open them, so your browser's search (Ctrl+F) will not find it until you do.
</box>

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* HRvest is based on [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) (AB3) by the SE-EDU initiative. HRvest keeps AB3's architecture, and parts of its code, documentation and diagrams are adapted from AB3.
* HRvest uses [JavaFX](https://openjfx.io/) for its GUI, [Jackson](https://github.com/FasterXML/jackson) for JSON and CSV tag data, and [JUnit 5](https://junit.org/junit5/) for testing.
* Team members used AI coding assistants for parts of the code, tests and documentation, guided by the repository's `AGENTS.md`.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CandidateSummaryHeader`, `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. These UI parts, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The following diagram details `CandidateSummaryHeader`, which composes the top row from an `AppLogo` and a group of summary cards: one `CandidateSummaryCard` for all candidates and a `StatusSummaryCard` for each recruitment status. These parts also inherit from `UiPart`.

<puml src="diagrams/CandidateSummaryHeaderClassDiagram.puml" alt="Structure of the candidate summary header"/>

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info">

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a candidate).<br>
   For `delete 1`, the command first gets the displayed list from the `Model`, then deletes the candidate `p` at index `1` of that list. Other commands may need different interactions with the `Model`.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

Role-management commands use dedicated parsers and delegate catalog validation and assignment updates to the `Model`.

#### Command input character validation

Candidate command fields and `find` keywords use `ParserUtil.verifyPrintableAscii` before their existing validation. The policy is deliberately parser-only: placing it in model constructors would also reject existing valid non-ASCII addresses or notes during JSON loading. Status values, indexes and confirmation keywords keep their dedicated checks. The user-visible rules are in the [User Guide](UserGuide.md#typing-values).

<panel header="How the check is applied" type="seamless">

`ArgumentTokenizer.tokenizeWithValidation` checks selected raw prefix values before trimming; otherwise trailing control characters could disappear, and invisible-only notes or tags could be mistaken for clearing requests. `AddressBookParser` preserves trailing argument characters for the same reason. The regular tokenizer remains available for commands outside this policy. Character errors take precedence over duplicate-prefix and field validation after tokenisation; command-specific unsupported-prefix checks can still run first. JSON and CSV serialization continue to escape values independently of this input policy.

* Tabs, line breaks, accented letters, emoji, curly quotes, non-breaking spaces (`U+00A0`) and zero-width spaces (`U+200B`) are rejected, even when copied text looks normal. Users must use ordinary spaces and straight quotes instead.
* Ordinary spaces around field values are still trimmed, and empty notes and tags can still be cleared as described in the User Guide. Status values, candidate indexes and `clear confirm` keep their own validation rules, and command words and prefix syntax are unchanged.
* Unsupported characters are checked before surrounding spaces are trimmed or empty values are interpreted as clearing a field. They are not silently removed or replaced.
* Errors identify the field prefix or search input for the unsupported code point, for example: `a/ contains unsupported character U+00A0. Use printable ASCII only (U+0020-U+007E).` Remove or replace that character and retry. Rejected commands leave candidate data unchanged.
* This restriction applies only to new command input, not to loading or exporting existing valid records. Existing non-ASCII text is preserved, but cannot be entered as a `find` keyword under this policy. JSON saving and CSV export keep their existing escaping; quotes and commas do not need to be removed from otherwise valid values.
</panel>

#### Prefix-like text in values

`ArgumentTokenizer` recognises a prefix after whitespace in any letter case, so users who type `S/Offered` get a status field. The same rule would also turn text that only looks like a prefix into a new field: `edit 1 a/Unit N/A` would silently rename the candidate to `A`, and `Unit N/A` could not be saved as an address. Two mechanisms address this: rejection of ambiguous input, and a `\/` escape. The rejection, not the escape alone, is what fixes the silent change, because a user who has not read about `\/` would still have `edit 1 a/Unit N/A` rename the candidate; the error stops the edit and shows the escape when it is needed. The user-visible behaviour is in the [User Guide](UserGuide.md#typing-values).

<panel header="How the tokenizer and LogicManager implement this" type="seamless">

* `tokenizeWithValidation`, used by `add`, `edit` and `note`, rejects an upper-case prefix that directly follows a field typed with a lower-case prefix, such as the `N/` in `edit 1 a/Unit N/A` or the `S/` in `edit 1 a/12 Kent Ridge Drive S/Offered`. Either reading is plausible, so the error names the typed prefix, its lower-case form for starting a field, and the escaped form for literal text; it also states that roles and statuses preserve backslashes. If that prefix is already a field and is not the repeatable `t/` or `r/`, the error omits the duplicate-field option. Name connectors such as `S/O` are removed before the check.
* Multi-field commands remain supported. For successful `add` and `edit` commands, `LogicManager` uses `ArgumentTokenizer.getPrefixWarning` to prepend a warning listing every prefix after the first field, preserving input order, letter case and repetitions. An intentional field update and literal prefix-like text can have identical syntax, so the warning applies to all multi-field commands while preserving one-shot operation. It covers lower-case prefixes as well as upper-case ones, but does not prevent unintended edits. Single-field edits and other commands keep their existing feedback. The result is returned only after any required save succeeds; an unchanged edit also carries the warning. Legal-name connectors and escaped slashes are excluded from the prefix list.
* `tokenizeWithValidation` reads `\/` as `/` in the values it validates: names, phones, emails, addresses, tags and notes, so `N\/A` is never matched as a prefix and is saved as `N/A`. Role and status values, including every role command, keep `\/` as typed, so a role saved with `\/` in its name by an earlier version still matches only itself.
* A lower-case prefix inside a value, such as `a/Unit n/a`, still starts a field, as in AB3, and is included in the warning.
</panel>

#### Displayed-index validation

`status`, `note`, `expand`, `edit`, and `delete` all read their index with `ParserUtil.parseDisplayedIndex`, so they report non-positive indexes, and indexes too large for an `int`, the same way. `expand` differs in two respects: it has its own message for a non-numeric index, and it checks the displayed list itself, so an index beyond the list gives `No candidate found with that index. Enter an index between 1 and N.` instead of the shared invalid-index message. The exact messages are in the User Guide's [common rules](UserGuide.md#common-rules). A rejected command leaves candidate data, filters, and the expanded view unchanged.

* A number too large for an `int` is reported as an out-of-range index rather than a format error, because no displayed list can be that long.
* A known numeric problem, such as `0`, is reported instead of the generic `Invalid command format!`, so the user knows exactly what to fix.
* Each command keeps its own validation order. For example, `note` still reports a missing `no/` before an invalid index. This keeps the change limited to index errors.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="600" />

The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores a shared role catalog in `AddressBook`, separately from candidate assignments.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* tracks whether the displayed list is in expanded view as transient state. The `Logic` API exposes this read-only property to the `UI`; it is not persisted.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<panel header="Duplicate detection: email-only identity" type="seamless">

The `Model` enforces email-only uniqueness through `Person.isSamePerson`, using case-insensitive comparison. `ParserUtil.parseEmail` trims command input, while `Email` validates stored values and preserves case. Full `Person.equals` and `Email.equals` remain case-sensitive. Add, edit, bulk replacement and JSON loading share this identity rule. Edit permits unchanged identity because the list already enforces uniqueness; a changed identity is checked against the entire address book, not only the displayed list.
</panel>

#### Role catalog design

<box type="info">

**Design goal:** The catalog stores roles independently of candidates so that unused roles remain available, even after all candidates are cleared. Commands identify roles by name rather than list position, so catalog sorting does not change their targets.
</box>

<panel header="Assignments and the protected `No role`" type="seamless">

* Each candidate has at least one role from the catalog. `Person` removes `No role` whenever named roles are assigned, including when loading older mixed assignments. The protected `No role` entry provides a fallback when deleting the candidate's last role.
* Roles keep a stable identity across renames. A rename updates the catalog and all assigned candidates together, including candidates outside the displayed list.
</panel>

<panel header="Name matching and validation" type="seamless">

* Role lookup keys use Unicode NFC before case conversion; display names and IDs retain their stored values. Catalogs with canonical name collisions are rejected through the existing paired-file backup path, rather than silently merging identities and assignments.
* Parsers validate role names through `ParserUtil.parseRoleName`; commands resolve membership through `RoleCatalog` before changing any data. Person edits replace all assignments in one `setPerson` call.
* `ParserUtil.parseRoleName` also rejects no-break, zero-width and other invisible spaces, because they would let a pasted name look blank or identical to an existing role. As with the printable-ASCII check for candidate fields, this check is parser-only, so catalog entries saved by earlier versions still load.
</panel>

<panel header="Role filters keep IDs, not `Role` objects" type="seamless">

Role filters retain IDs rather than `Role` objects because role equality includes the display name. A rename therefore keeps matching the same role; a newly created role with a deleted role's name does not inherit its filter. Status and role groups combine with `AND`, with `OR` within each group.
</panel>

<panel header="Consistent updates and shared prefixes" type="seamless">

* `AddressBook` validates a complete catalog and assignment replacement before applying a role rename or deletion, preventing inconsistent references or partial updates. `ModelManager` retains the current filter or reselects the expanded candidate after those domain updates; expanded view requires exactly one displayed candidate.
* `CommandPrefixes` in Commons supplies parameter text to `CliSyntax`, role-name validation and role-management parsers. This keeps reserved syntax consistent without making the model depend on Logic.
</panel>

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="700" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which coordinates candidate and role files through `RoleDataStorage` and delegates preference storage to `JsonUserPrefsStorage`.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

<box type="info">

**Design goal:** Storage favours not losing data. Candidate and role data are never overwritten in place (saves write a temporary file and atomically replace the original; `preferences.json` is written directly), `MainApp` takes an operating-system lock on the data folder so two instances cannot corrupt each other, and an unreadable data file is backed up before HRvest starts with an empty list.
</box>

<panel header="Storage safety details: atomic saves, the data-directory lock, and startup recovery" type="seamless">

`JsonAddressBookStorage` never overwrites the data file in place, so a failed or interrupted save leaves the previous file intact. It writes the JSON to a uniquely named `HRvest-<random>.tmp` file in the same folder, gives it the existing file's access permissions, and then atomically replaces the data file with it. Filesystems that cannot replace a file atomically reject the save, and permission errors abort it; `LogicManager` reports either error. A symbolic link to the data file is kept, and its target is replaced. The temporary file is deleted if the save fails, but a crash or power loss can leave it behind; later saves ignore such files, and they can be deleted once HRvest is closed.

Before loading preferences, recovering files or constructing storage, `MainApp` acquires a `DataDirectoryLock` on `data/.hrvest.lock`. The OS lock is held through preference saving at shutdown and released on failed initialization. This prevents a second process from loading stale candidates and later overwriting newer data. The lock file stays in place after release: deleting it could let two processes lock different files with the same path. Separate data folders remain independent, and process termination releases the OS lock automatically.

During startup, `MainApp` uses the storage component's `CandidateDataInitializer` to load candidate records before creating the UI or command logic. A `DataLoadingException` triggers a persistent, uniquely named `HRvest-<timestamp>-<random>-corrupt.json` backup through `Storage`, followed by a journal-coordinated save of an empty candidate list and the built-in role catalogue. The local-time `yyyyMMddTHHmmss` timestamp makes the newest backup easy to find when several accumulate. The backup copies the original bytes first and then applies the original's supported access restrictions, so a write-restricted data file can still be backed up. An unsuccessful copy removes only its own partial backup where possible. Recovery resolves symbolic links to existing targets; inaccessible files and dangling links are not treated as missing files.

Both backup and replacement saving must finish before commands become available. Otherwise startup shows an actionable error dialog and exits after dismissal, preserving the original and any completed backup. After successful recovery, `UiManager` displays the backup path and restoration instructions. A later launch loads the saved empty file normally, avoiding repeated backups or sample data. Preference-file recovery remains separate.
</panel>

The activity diagram below summarizes candidate data initialization and recovery. Load failures include malformed JSON, invalid candidate data or role catalog entries, unknown role IDs, and null entries rejected during deserialization or validation.

<puml src="diagrams/DataRecoveryActivityDiagram.puml" alt="Startup candidate data loading and recovery, including backup and replacement save failures" />

#### Coordinating candidate and role storage

Candidates store role IDs, while `roles.json` stores the catalog. `RoleDataStorage` loads the catalog first so that it can validate candidate assignments. Older candidates without role assignments receive `No role`; invalid assignments use the data-recovery process described above.

<box type="info">

**Why a rollback journal:** The two files must stay consistent, but a filesystem rename can replace only one file at a time. A rollback journal, `roles-transaction.json`, therefore preserves the previous files before either is replaced. Failed or interrupted saves restore that pair before data is loaded again.
</box>

<panel header="Recovery and save rules" type="seamless">

* Invalid journals or incomplete rollback block further saves, preserving the recovery data. Candidate, catalog, and journal paths must identify different files, and concurrent HRvest instances are unsupported.
* Recovery from invalid candidate or catalog data backs up both originals before saving an empty candidate list and the built-in catalog. The catalog backup uses the candidate backup name with `.roles.json` appended; a catalog-only backup is used when no candidate file exists.
* If saving a command fails, `LogicManager` restores the previous model data, filter, and expanded view so that memory and saved data remain consistent after successful rollback.
* `LogicManager` compares the immutable candidate records and role catalog before and after execution. It saves only when persisted data changes, so a storage error cannot block view changes, help, or exit. Import retains its separate save and rollback pipeline, including empty imports.
</panel>

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Candidate notes

Each candidate has at most one note, such as interview feedback. `note INDEX no/NOTE_TEXT` replaces the note of a candidate in the displayed list, and `note INDEX no/` clears it.

#### How it fits in

* `Person` holds the note as an `Optional<Note>`. `Note` is an immutable value that rejects blank text and text longer than `Note.MAX_LENGTH` (500) characters, so a candidate either has a valid note or none. `ParserUtil#parseNote` and `JsonAdaptedPerson` trim the text before creating a `Note`.
* `NoteCommandParser` tokenizes the arguments with `no/` as its only prefix, so text such as `n/A` inside a note stays part of the note. It rejects a missing or repeated `no/`. Blank note text creates a `NoteCommand` that clears the note.
* `NoteCommand#execute` finds the candidate in the displayed list and builds a new `Person` with the same details and the new note, because `Person` is immutable. It then calls `Model#setPerson`, which keeps the current filter and keeps the candidate expanded if it was.
* `add` and `edit` reject `no/` through `ParserUtil#verifyNoNotePrefix`, so `note` is the only command that sets or changes the note of an existing candidate.
* `JsonAdaptedPerson` stores the note text in a `note` field. On loading, a missing, `null` or blank `note` means no note, so data files from before notes existed still load.
* `PersonCard` shows a sticky-note icon when `Person#hasNote()` is true and shows the note text only in expanded view.

The sequence diagram below shows how `note 2 no/Strong on SQL` is executed. `p` is the candidate at index 2 of the displayed list.

<puml src="diagrams/NoteSequenceDiagram.puml" alt="Interactions inside the Logic and Model components for the note 2 no/Strong on SQL command" />

After the note command returns, `LogicManager` saves the address book if its data changed.

#### Design considerations

**Aspect: One note or a list of notes**

* **Alternative 1 (current choice):** One note per candidate, replaced by each `note` command.
  * Pros: Simple to use and display. No extra commands or indexes are needed to correct or remove one entry.
  * Cons: Replacing a note loses the earlier text, so the User Guide tells users to include anything they want to keep.
* **Alternative 2:** A list of notes that `note` appends to.
  * Pros: Keeps a full history of interactions with the candidate.
  * Cons: Needs commands to edit or delete a single entry, and makes the expanded view longer.

**Aspect: A separate command or a field of `add` and `edit`**

* **Alternative 1 (current choice):** A separate `note` command.
  * Pros: The note can contain text that looks like a prefix, because `no/` is the only prefix `note` recognises. An `edit` cannot replace a note by accident.
  * Cons: Users learn one more command.
* **Alternative 2:** A `no/` field in `add` and `edit`.
  * Pros: Fewer commands.
  * Cons: Any other prefix inside the note, such as the `a/` in "follow up on a/b test", would start a new field.

### Command history navigation

The command history feature lets a user walk through commands they submitted earlier in the current session, using the `Up` and `Down` arrow keys. It is implemented as a single collaborator (`CommandHistory`) owned by `CommandBox`.

#### How it fits in

`CommandBox` already owns the JavaFX `TextField` and runs user input through `CommandExecutor` on Enter. The new field reuses that path:

1. On Enter, `CommandBox#handleCommandEntered` records the raw text with `CommandHistory#add` **before** dispatching to the executor. Rejected commands are recorded too. The text field is cleared after a successful command. After a rejected command, the text stays so that the user can fix it, and `CommandHistory#moveCursorToNewest` marks that entry as already shown, so the next Up press shows the command before it instead of appearing to do nothing.
2. A `KEY_PRESSED` event filter on `commandTextField` catches `UP` and `DOWN` presses, calls `history.previous()` or `history.next()`, and swaps the text field's contents with the result. The event is consumed so the caret does not move within the text field.
3. When the user presses `Up` while composing a fresh command, `CommandBox` captures the current text as a `draft` before replacing the field with a past entry. Pressing `Down` back past the newest entry restores that draft.

#### `CommandHistory` design

`CommandHistory` is a small helper class in the `ui` package. It stores submitted commands in insertion order and tracks a `cursor` that sits at `entries.size()` when the user is composing a fresh command (the "past-the-end" position).

* `add(String)` appends and resets the cursor to the past-the-end position. Blank commands are ignored, and a command identical to the most recent entry is also skipped so repeated submissions do not take up extra slots.
* `previous()` moves the cursor one step back and returns the command there, or `Optional.empty()` when already at the oldest entry.
* `next()` moves the cursor forward. On reaching the past-the-end position it returns an empty string so the UI knows to clear or restore the field.
* Entries are capped at `MAX_ENTRIES` (100) to bound memory for long-running sessions. The oldest entry is dropped when the cap is reached.
* `moveCursorToNewest()` moves the cursor onto the newest entry when the command box keeps a rejected command on screen.

The class diagram below shows the collaborator relationship:

<puml src="diagrams/CommandHistoryClassDiagram.puml" alt="CommandHistoryClassDiagram" />

The sequence diagram below shows the flow for a user pressing `Up` twice then `Down` once:

<puml src="diagrams/CommandHistorySequenceDiagram.puml" alt="CommandHistorySequenceDiagram" />

#### Design considerations

* **Session-only, no persistence.** Keeping history in memory avoids a storage format change for a quality-of-life feature. Command history is session-scoped and is not restored after restarting HRvest.
* **Record failed commands too.** The whole point of Up/Down is to iterate faster; forcing users to retype a mistyped `filtwr s/Interviewing` would defeat that.
* **Hard cap at 100 entries.** Prevents unbounded growth in long sessions. 100 is chosen to be well above the dozen or so commands a typical session uses while still being cheap.
* **Event filter, not handler.** A filter runs before the `TextField`'s own key-handling, so the arrow keys do not also move the caret while cycling.

### CSV import

Import adds a batch of candidates to the complete address book, regardless of the current filter. `AddressBookParser` uses `ImportCommandParser` to create an `ImportCommand`, and `LogicManager` delegates the operation to `ImportOperation`.

The class diagram gives a high-level overview of the import classes, to help developers identify which class to inspect for a particular behaviour.

<puml src="diagrams/ImportFeatureClassDiagram.puml" alt="CSV import classes showing command delegation, candidate parsing, and model and storage interfaces" />

<panel header="Which class to inspect" type="seamless">

`ImportCommand` requests the operation, while `ImportOperation` coordinates batch combination, model updates, saving and rollback through the `Model` and `Storage` interfaces. Look at `CandidateCsvParser` for candidate validation and role resolution, and `CsvImportReader` for file reading and input checks. Method parameters and return types are omitted to keep the diagram compact; the existing `Model` interface is shown without its methods.

`StorageManager` delegates reading `import.csv` to `CsvImportReader`. Missing files and read failures produce an error explaining the problem and expected path. It reads the file into a bounded buffer so that later edits do not affect the data being parsed. `CandidateCsvParser` uses `StrictCsvParser` on that buffer and validates candidate fields through the existing command and model rules. Import and export share their headers through `CandidateCsvFormat`.
</panel>

The sequence diagram shows how an import request reaches storage and updates the model. It highlights the boundary between validation, which does not mutate the model, and application and saving, which require rollback on failure.

<puml src="diagrams/ImportSequenceDiagram.puml" alt="CSV import sequence showing validation before model updates, saving, and the rollback boundary" />

<panel header="Validation, application and rollback" type="seamless">

Command parsing and lower-level CSV and JSON processing are omitted from the sequence diagram to focus on this coordination.

`ImportOperation` validates the whole file, then checks for duplicates within the batch and against existing candidates, before updating the model and saving through storage. It manages its own rollback because import bypasses `LogicManager`'s ordinary save path: reading or validation failures leave the model unchanged, and application or save failures restore the previous data and view. Success shows the full candidate list and exits expanded view.
</panel>

<box type="info">

**Why roles are matched by name:** The CSV identifies roles by name, not by catalog ID, because the file may be imported into an HRvest whose catalog has different IDs or lacks those roles. `ImportOperation` passes the current catalog to `CandidateCsvParser`, which matches names as role commands do and creates each unknown name once, so records naming `QA` and `qa` share one new role. The new roles are added to the catalog in the same model update and save as the candidates, so a failure rolls both back. Creating roles, rather than rejecting the import as `add` does, lets an export restore on a fresh installation; the success message names the created roles so that a misspelt name is noticed.
</box>

<panel header="Batch combination, concurrency and failure handling" type="seamless">

* Batch combination installs the validated list once, preserving existing and CSV order. `UniquePersonList` checks bulk replacements with a case-insensitive email set instead of comparing every pair. This keeps the same identity rule while avoiding quadratic validation during import, snapshots and saves.
* `DataDirectoryLock` stops a second HRvest instance from running against the same data folder, so another instance cannot import at the same time. The lock does not cover external processes: HRvest does not lock `import.csv` or check for concurrent changes, so an external edit while the current instance reads it may cause a normal read/validation error, or go undetected and import valid-looking altered data. It does not guarantee that bytes read during an external edit represent one consistent file version. Do not externally modify `import.csv` or the saved JSON during import.
* Failed saves retain the previous saved JSON. If rollback itself fails, HRvest reports that separately; stop using the app and check saved data before restarting. Crash or power-loss recovery is not an in-memory rollback guarantee.
</panel>

See the [User Guide](UserGuide.md#importing-candidates-import) for import limits and usage restrictions.

#### Design considerations

<box type="info">

**Generous but bounded imports:** The resource limits are intended to accommodate reasonable candidate batches and field values for HRvest's target users, rather than narrowly restrict valid recruitment data. For example, an import can contain up to 10,000 candidates in a 10 MiB file, with up to 65,536 Unicode characters per decoded field before trimming. Inputs beyond these bounds are considered outside the reasonable workload supported by this desktop application; rejecting them during reading or parsing bounds resource use instead of allowing arbitrarily large inputs to consume memory or processing time. This is a supported-workload decision, not a claim that larger datasets are inherently invalid. Existing field-validation rules still apply, and even files exported by HRvest must satisfy the import limits before they can be imported again.
</box>

#### Import CSV format and validation

The required header is the one in the example below, in exactly that lowercase order. The file is UTF-8 with or without a single leading byte order mark (BOM).

```csv
"name","phone","email","address","status","tags","note","roles"
"Example Candidate","01234567","candidate@example.com","123 Example Road","Applied","[""Java"",""backend""]","","[""No role""]"
```

<panel header="CSV syntax rules" type="seamless">

* Fields may be unquoted or double-quoted. Fields containing commas or quotes must be double-quoted; double any embedded quotes. Quotes must enclose the entire field, with no extra text or spaces outside the closing quote. Quoting does not bypass field validation.
* LF (`\n`) and CRLF (`\r\n`) record endings are accepted, and the last record need not end with a newline. Empty trailing lines are allowed, but blank lines before the header or between records, whitespace-only trailing lines, bare CR separators, alternate delimiters and incorrect column counts are rejected.
* Configure your editor to save the example as UTF-8 with LF or CRLF line endings. Excel's optional quoting is accepted when saving as comma-delimited UTF-8 CSV; other encodings or delimiters are not.
* HRvest accepts the leading BOM written by `export`, so you can copy or rename `data/export.csv` to `data/import.csv` without removing it. A BOM-only file or repeated leading BOMs are rejected. Only one encoding marker immediately before the header is ignored; markers inside quoted fields are literal data subject to field validation.
* CSV syntax errors identify the record (the header is record 1) and, for field-specific syntax errors, the one-based field number and name. Messages distinguish missing or extra columns, misplaced or unclosed quotes, content after a closing quote, and unsupported bare CR endings, with guidance on correcting the file. Record numbers count CSV records, not physical lines inside quoted fields.
</panel>

<panel header="Field validation rules" type="seamless">

Existing field rules apply, including trimming as in commands. The file must still meet the validation, duplicate and resource-limit rules.

* **Status:** accepts defined values ignoring case and surrounding whitespace; empty or unknown statuses fail.
* **Tags:** a JSON array of valid tag-name strings, without nulls, nested values or trailing JSON. Tags need not be sorted; surrounding whitespace is trimmed, repeated identical names collapse, and `Java` and `java` remain distinct.
* **Roles:** the same JSON array form and limits, but at least one role must be named. Names follow the role name rules, and `No role` is dropped when named roles are present, as in `add`.
* **Note:** an empty or whitespace-only note means no note. Nonblank notes are stripped of surrounding whitespace and limited to 500 Unicode code points; tabs, line breaks and non-ASCII text inside a quoted note are preserved.
* **Address and note:** use their model validation rather than the command-only printable-ASCII check, so supported legacy Unicode data can be exported and imported again. Addresses retain command-style surrounding-space trimming.
* Formula-like text remains ordinary data where the field permits it; HRvest never executes it.
</panel>

### CSV export

The `export` command creates a CSV copy of all candidate records, regardless of the current search or filter. It leaves the application's data and current view unchanged. `ExportCommand` requests an export through `CommandResult`. `LogicManager` passes the complete address book to `StorageManager`, which delegates CSV formatting and writing to `CsvExportWriter`. This keeps file operations in the storage component rather than the model.

The class diagram gives a high-level overview of the export classes, to help developers identify which class to inspect for a particular behaviour.

<puml src="diagrams/ExportFeatureClassDiagram.puml" alt="ExportFeatureClassDiagram" />

<panel header="Which class to inspect" type="seamless">

Look at `ExportCommandParser` for command parsing, `ExportCommand` and `CommandResult` for requesting an export, and `LogicManager` for coordinating the operation and reporting its result. `StorageManager` delegates the export to `CsvExportWriter`, which handles destination resolution, CSV formatting and file writing. Method parameters and return types are omitted to keep the diagram compact.
</panel>

The sequence diagram shows how the export request reaches storage and returns the destination path.

<puml src="diagrams/ExportSequenceDiagram.puml" alt="CSV export sequence showing command delegation to storage and the return path without JSON saving" />

<panel header="Why export skips JSON saving" type="seamless">

`LogicManager` returns the export result immediately, bypassing ordinary JSON saving because export does not change persisted candidate data. Command parsing and file-handling internals are omitted from the diagram.
</panel>

The writer exports one row per candidate, with a header and the name, phone, email, address, status, tags, note and roles fields, to `export.csv` beside the JSON data file. The data file path is fixed at `data/HRvest.json`, so the export is always `data/export.csv`. The file starts with a UTF-8 byte order mark so that spreadsheet programs such as Excel detect the encoding; without it, Excel reads the file as ANSI and garbles non-ASCII text.

<box type="warning">

**Spreadsheet caution:** This is raw-data CSV, not a spreadsheet document. HRvest does not add sanitisation prefixes or formula wrappers, because they would change the stored values in the file; the User Guide warns users to import columns as text instead, which preserves leading zeroes in phone numbers and stops formula-like stored text from being interpreted as formulas. Spreadsheet auto-detection can still alter values when the file is opened directly.
</box>

<panel header="Output format and failure handling" type="seamless">

* **Format:** every field is quoted and embedded quotes are doubled. Literal values, including embedded line breaks, are preserved. Status uses its displayed label; an absent note is empty. Tags occupy one CSV field containing a JSON array, such as `["Java","backend"]`, with CSV escaping applied. Tag names are sorted in case-sensitive Java string order, preserving casing; no tags gives `[]`. Roles use the same form, with names in `list_role` order, ignoring case; a candidate without a named role gives `["No role"]`.
* **Replacing an earlier export:** HRvest shows a confirmation after the export completes, or an error if it fails. Export bypasses JSON saving because it does not change candidate data. To protect an earlier CSV snapshot, it writes a temporary sibling file, preserving the destination's existing access permissions, closes and flushes it to disk, then atomically replaces `export.csv`. There is no non-atomic fallback: unsupported atomic replacement fails while retaining the previous CSV.
* **Cleanup:** handled failures attempt to remove the temporary file. If cleanup also fails, the original export failure remains the cause of the reported error, with the cleanup failure attached to it as a suppressed exception. Cleanup failure or process termination may leave the temporary file behind, but cannot expose a partially written replacement as `export.csv`.
* **Errors:** failures display `Export failed:` with the reason and destination when available; check that the file and folder are writable. Directories, symbolic links (including dangling links) and other non-regular destinations are rejected without deleting them. A missing data directory is created first, as JSON saving does; if the required parent directory cannot be determined or created, export fails. No failure redirects output to another location. Candidate data and the current view remain unchanged, and write, close and replacement failures leave the previous export intact.
</panel>

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a hiring manager at a tech startup hiring for one to three full-time roles at a time
* tracks roughly 10 to 50 candidates at a time, including their contact details and progress through hiring
* needs to find and update candidate records more easily than in a spreadsheet
* can type quickly and prefers keyboard commands to mouse-driven forms
* uses the app individually on a desktop or laptop computer

**Value proposition**: HRvest helps a startup hiring manager fill full-time roles faster by tracking candidates and hiring progress with keyboard-first commands, reducing spreadsheet work and making follow-ups easier to spot.


### User stories

The following user stories describe supported functionality. Priorities indicate their relative importance: High `* * *`, Medium `* *`, Low `*`.

#### High-priority (`* * *`)

| Priority | As a …           | I want to …                                          | So that I can…                                                     |
|----------|-------------------|-------------------------------------------------------|---------------------------------------------------------------------|
| `* * *`  | new user          | open a help window from within HRvest                 | find the link to the User Guide without searching for it            |
| `* * *`  | new user          | add a new candidate with contact details and roles    | keep a record of every candidate I am considering                   |
| `* * *`  | new user          | search for a candidate by name                        | view their details without scrolling through the whole list         |
| `* * *`  | new user          | edit a candidate's details and role assignments      | correct mistakes or outdated details without re-adding them         |
| `* * *`  | user              | delete a candidate from the list                      | keep my list focused on candidates still under consideration        |
| `* * *`  | user              | list every candidate currently in the app             | see every candidate in my pipeline in one view                      |
| `* * *`  | user              | filter the list by candidate status                   | focus on one hiring stage, for example only those interviewing      |
| `* * *`  | user              | update a candidate's status after an interview        | keep the pipeline accurate as candidates move through stages        |
| `* * *`  | user              | view a candidate's full details including their note  | prepare for the next conversation with them                         |
| `* * *`  | user              | see a confirmation when an action succeeds            | confirm that the command did what I intended                        |

#### Medium-priority (`* *`)

| Priority | As a …           | I want to …                                                      | So that I can…                                                       |
|----------|-------------------|-------------------------------------------------------------------|-----------------------------------------------------------------------|
| `* *`    | familiar user     | find a candidate by a partial keyword of their name               | find a candidate whose full name I do not remember                    |
| `* *`    | familiar user     | create roles before adding candidates                             | prepare the catalog for upcoming hiring                             |
| `* *`    | familiar user     | list all available roles, including unused ones                   | reuse an existing role instead of creating a duplicate                |
| `* *`    | familiar user     | rename a role across all assigned candidates                      | correct a job title consistently                                      |
| `* *`    | familiar user     | remove an obsolete role while keeping candidates' other roles     | keep the catalog relevant without losing unrelated hiring details   |
| `* *`    | familiar user     | filter candidates by the role they applied for                    | focus on one open role at a time                                      |
| `* *`    | familiar user     | combine role and status in a single filter command                | isolate a specific group without running two separate searches        |
| `* *`    | familiar user     | track whether a candidate has accepted an offer                   | know which offers are still pending a response                        |
| `* *`    | familiar user     | add or replace a note on a candidate                              | record interview feedback and follow-ups for the next conversation    |
| `* *`    | familiar user     | recall earlier commands with the Up and Down keys                 | repeat or correct a command without retyping it                       |
| `* *`    | familiar user     | see how many candidates are at each status at a glance            | spot where candidates are piling up without running a filter          |
| `* *`    | expert user       | import a batch of shortlisted applicants from an external source  | avoid retyping candidates sourced from LinkedIn, Indeed, or email     |

#### Low-priority (`*`)

| Priority | As a …           | I want to …                                              | So that I can…                                                       |
|----------|-------------------|-----------------------------------------------------------|-----------------------------------------------------------------------|
| `*`      | expert user       | export candidate records as CSV                          | share candidate records with people outside the app                   |
| `*`      | expert user       | clear all candidates after confirming                    | start a new hiring round without deleting candidates one by one       |

*See [the full brainstormed list in the project notes](https://docs.google.com/spreadsheets/d/1N5Ji0Wl8ai_IVu94vVavK0b_f5AiQSDjcHCVw-pA5EM/edit) for stories we considered and dropped.*

### Use cases

(For all use cases below, the **System** is `HRvest` and the **Actor** is the `user`, a hiring manager, unless specified otherwise)

The terms candidate details, candidate status, duplicate candidate and note are defined in the [Glossary](#glossary).

**Use case: UC01 - Add a candidate**

**MSS**

1. User requests to add a candidate, giving the candidate's details.
1. HRvest adds the candidate with status Applied by default (or the specified status, if provided) and shows the added candidate.

   Use case ends.

**Extensions**

* 1a. A required detail is missing, a given detail is invalid, or a role does not exist.

    * 1a1. HRvest shows an error message describing the problem.

      Use case ends.

* 1b. The new candidate would be a duplicate candidate.

    * 1b1. HRvest shows an error message.

      Use case ends.

**Use case: UC02 - List candidates**

**MSS**

1. User requests to list candidates.
1. HRvest shows a list of all candidates.

   Use case ends.

**Extensions**

* 1a. HRvest has no candidates.

    * 1a1. HRvest informs the user that there are no candidates.

      Use case ends.

**Use case: UC03 - Update a candidate's status**

**MSS**

1. User performs <u>List candidates (UC02)</u>.
1. User requests to change the status of a specific candidate in the list to a new candidate status.
1. HRvest updates the candidate's status and shows a success message identifying the candidate.

   Use case ends.

**Extensions**

* 1a. The list is empty.

  Use case ends.

* 2a. The specified candidate is not in the list.

    * 2a1. HRvest shows an error message.

      Use case ends.

* 2b. The new status is not a valid candidate status.

    * 2b1. HRvest shows an error message listing the valid candidate statuses.

      Use case ends.

* 2c. The candidate already has the new status.

    * 2c1. HRvest informs the user that the status is unchanged.

      Use case ends.

* 3a. The updated candidate no longer matches the current filter.

    * 3a1. HRvest shows the success message, but the candidate no longer appears in the filtered results.

      Use case ends.

**Use case: UC04 - Edit a candidate's details**

**MSS**

1. User performs <u>List candidates (UC02)</u>.
1. User requests to change one or more of the candidate details of a specific candidate in the list.
1. HRvest updates the candidate's details and shows a success message identifying the candidate.

   Use case ends.

**Extensions**

* 1a. The list is empty.

  Use case ends.

* 2a. The specified candidate is not in the list.

    * 2a1. HRvest shows an error message.

      Use case ends.

* 2b. No details to change are given.

    * 2b1. HRvest shows an error message.

      Use case ends.

* 2c. A new detail is invalid or a role does not exist.

    * 2c1. HRvest shows an error message describing the problem.

      Use case ends.

* 2d. The changes would make the candidate a duplicate candidate.

    * 2d1. HRvest shows an error message.

      Use case ends.

* 2e. All the given details are the same as the candidate's current details.

    * 2e1. HRvest informs the user that the details are unchanged.

      Use case ends.

* 3a. The updated candidate no longer matches the current filter.

    * 3a1. HRvest shows the success message, but the candidate no longer appears in the filtered results.

      Use case ends.

**Use case: UC05 - Delete a candidate**

**MSS**

1. User performs <u>List candidates (UC02)</u>.
1. User requests to delete a specific candidate in the list.
1. HRvest deletes the candidate, together with the candidate's note, and shows the deleted candidate.

   Use case ends.

**Extensions**

* 1a. The list is empty.

  Use case ends.

* 2a. The specified candidate is not in the list.

    * 2a1. HRvest shows an error message.

      Use case ends.

**Use case: UC06 - Find a candidate by name**

**MSS**

1. User requests to find candidates by name using keywords.
1. HRvest shows a list of matching candidates.

   Use case ends.

**Extensions**

* 1a. No keyword is given.

    * 1a1. HRvest shows an error message.

      Use case ends.

* 2a. No candidate matches the keyword.

    * 2a1. HRvest informs the user that no candidates match.

      Use case ends.

**Use case: UC07 - Filter candidates by status or role**

**MSS**

1. User requests to filter candidates by statuses, roles, or both.
1. HRvest shows candidates matching any of the specified statuses and any of the specified roles, where supplied.

   Use case ends.

**Extensions**

* 1a. A specified status is not a valid candidate status.

    * 1a1. HRvest shows an error message listing the valid candidate statuses.

      Use case ends.

* 1b. A role name is invalid or does not exist.

    * 1b1. HRvest shows an error message describing the problem.

      Use case ends.

* 1c. Neither a status nor a role is supplied.

    * 1c1. HRvest shows an error message describing the required criteria.

      Use case ends.

* 2a. No candidates match the specified criteria.

    * 2a1. HRvest informs the user that no candidates match.

      Use case ends.

**Use case: UC08 - View a candidate's full details**

**MSS**

1. User requests to view the full details of a candidate profile.
1. HRvest shows the candidate's full details, including their note.

   Use case ends.

**Extensions**

* 1a. The specified candidate is not found.

    * 1a1. HRvest shows an error message.

      Use case ends.

**Use case: UC09 - Add or replace a candidate's note**

**MSS**

1. User requests to save a note for a specific candidate in the list.
1. HRvest replaces the candidate's earlier note, if any, with the new note and confirms the update.

   Use case ends.

**Extensions**

* 1a. The specified candidate is not in the list, or the note is longer than 500 characters.

    * 1a1. HRvest shows an error message.

      Use case ends.

* 1b. The note is empty.

    * 1b1. HRvest clears the candidate's note and confirms that it was cleared.

      Use case ends.

**Use case: UC10 - Import candidates**

**MSS**

1. User requests to import the candidates in the prepared `import.csv`.
1. HRvest reads and validates the complete file import.
1. HRvest appends and saves the candidates, then shows the default list view with all candidates and the imported count.

   Use case ends.

**Extensions**

* 2a. Input is missing, unreadable, malformed, oversized, or contains invalid/duplicate candidates.

  * 2a1. HRvest explains the failure and leaves the pre-import data and view unchanged.

    Use case ends.

* 3a. Applying or saving fails.

  * 3a1. HRvest restores the pre-import in-memory data and view, retaining unsaved changes, and reports the error. A rollback failure is reported separately.

    Use case ends.

**Use case: UC11 - Export candidate data**

**MSS**

1. User requests to export candidate data.
1. HRvest exports the candidate data to a file on the user's computer.
1. HRvest shows a confirmation message.

   Use case ends.

**Extensions**

* 2a. There are no candidates.

    * 2a1. HRvest still exports a file with no candidate data.

      Use case resumes at step 3.

* 2b. The data cannot be exported.

    * 2b1. HRvest shows an error message.

      Use case ends.

**Use case: UC12 - Create a role**

**MSS**

1. User requests to create a role, giving its name.
1. HRvest makes the new role available for assigning to candidates.
1. HRvest shows a success message identifying the created role.

   Use case ends.

**Extensions**

* 1a. The name is invalid or matches an existing role, including `No role`.

    * 1a1. HRvest shows an error message.

      Use case ends.

**Use case: UC13 - Rename a role globally**

**MSS**

1. User requests to rename an existing role, giving the role and its new name.
1. HRvest changes the role's name for all candidates who have it, leaving their other roles unchanged.
1. HRvest shows a success message identifying the renamed role.

   Use case ends.

**Extensions**

* 1a. The role does not exist or is the protected `No role` entry.

    * 1a1. HRvest shows an error message.

      Use case ends.

* 1b. The new name is invalid or matches another role, including `No role`.

    * 1b1. HRvest shows an error message.

      Use case ends.

* 1c. The new name is unchanged or differs only in capitalisation.

    * 1c1. HRvest accepts the name and confirms the change, updating the capitalisation if needed.

      Use case ends.

**Use case: UC14 - Delete a role globally**

**MSS**

1. User requests to delete an existing role.
1. HRvest removes the role from the available roles and from all candidates who have it, leaving their other roles unchanged. Candidates left without a role receive `No role`.
1. HRvest shows a success message identifying the deleted role.

   Use case ends.

**Extensions**

* 1a. The role does not exist or is the protected `No role` entry.

    * 1a1. HRvest shows an error message.

      Use case ends.

**Use case: UC15 - List the role catalog**

**MSS**

1. User requests to list available roles.
1. HRvest shows all catalog entries, including unused roles and `No role`, in alphabetical order ignoring case.

   Use case ends.

**Use case: UC16 - Clear all candidates**

**MSS**

1. User requests to clear all candidates.
1. HRvest shows how many candidates would be deleted and asks the user to confirm.
1. User confirms the request.
1. HRvest deletes all candidates, keeps the role catalog, and confirms that the candidates were cleared.

   Use case ends.

**Extensions**

* 1a. The user gives the confirmation together with the request.

  Use case resumes at step 4.

* 1b. HRvest has no candidates.

    * 1b1. HRvest informs the user that there are no candidates to clear.

      Use case ends.

* 1c. The request contains anything other than the confirmation.

    * 1c1. HRvest shows an error message and deletes nothing.

      Use case ends.

* 2a. The user does not confirm and performs another command instead.

    * 2a1. HRvest runs that command normally. The earlier request is not remembered, and nothing is deleted.

      Use case ends.

### Non-Functional Requirements

1. HRvest should run on any mainstream OS (Windows, Linux or macOS) with Java `25` installed, without requiring an installer or other application-specific software beyond a single JAR file.
1. HRvest should be usable by a single user without requiring a shared account or a team-operated remote server for ordinary candidate management.
1. Candidate records should be stored locally in a human-editable text file, without a database management system.
1. Core candidate-management commands should work without an internet connection.
1. The primary workflows for adding, finding, viewing, and updating candidate records should be completable using typed commands without mouse interaction.
1. Listing, searching, filtering, and updating 50 candidate records across three open roles should each complete within two seconds.
1. The GUI should work well at 1920×1080 resolution with 100% or 125% display scaling, and remain usable at 1280×720 resolution with 150% display scaling.
1. A successful change to a candidate record should persist after a normal restart, while an invalid command should leave stored candidate records unchanged.

### Glossary

* **Candidate**: A person being considered for an open role.
* **Open role**: A position for which the startup is currently recruiting.
* **Role catalog**: The shared collection of roles available in HRvest, including unused roles and the built-in `No role`.
* **Application**: A candidate's consideration for a particular open role, including the candidate status and relevant hiring details.
* **Candidate profile**: The candidate's contact information and hiring details shown together in HRvest.
* **Follow-up**: An action the hiring manager needs to take for a candidate after a prior interaction.
* **Candidate details**: A candidate's name, phone number, email address, address, status, roles and tags.
* **Candidate status**: One of Applied, Shortlisted, Interviewing, Offered, Accepted, Rejected, or Withdrawn.
* **Duplicate candidate**: A candidate with the same email address as another candidate in HRvest, compared case-insensitively after trimming command input. No other fields, including name, status, roles, tags or notes, affect duplicate detection.
* **Mainstream OS**: Windows, Linux or macOS
* **Prefix**: A short marker such as `n/` or `s/` that starts a field in a command, for example `n/John Doe`.
* **Displayed list**: The candidates currently shown in the candidate list, after any search or filter. Index commands such as `edit` and `delete` act on this list.
* **Expanded view**: The view that shows a single candidate, at index `1`, with their full note. `expand` enters it and `list` leaves it.
* **Note**: A single text remark about a candidate, such as interview feedback or follow-up context.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info">

**Note:** These instructions only provide a starting point for testers to work on; testers are expected to do more *exploratory* testing. Commands use the sample data unless stated otherwise.
</box>

<box type="warning">

**Caution:** Tests marked _data files_ change or damage the saved data on purpose. Run them in a separate test folder, and restore matching backups of `data/HRvest.json` and `data/roles.json` together afterwards.
</box>

### Launch and shutdown

1. Initial launch
   1. Download the JAR file and copy it into an empty folder, then run `java -jar HRvest.jar` from that folder.<br>
      Expected: The GUI opens with a set of sample candidates. The window size may not be optimal.
1. Launching a second instance
   1. Keep HRvest running, open another terminal in the same folder and run `java -jar HRvest.jar` again.<br>
      Expected: An error dialog asks you to close the first instance. Dismissing it exits the second instance; the first remains usable and its data is unchanged.
1. Saving window preferences
   1. Resize and move the window, close it, then relaunch.<br>
      Expected: The most recent window size and location are retained.

### Adding candidates and rejecting bad input

1. Run `add n/A p/123 e/a@x.com a/Blk 1 no/great candidate`, then the same command with an empty `no/`, and with `no/` before the required fields.<br>
   Expected: Each gives the error that `add` does not support `no/` and directs you to `note`. No candidate is added and existing data is unchanged.
1. Run `add n/A p/123 e/a@x.com a/Blk 1 yes/no/feedback r/No role` with an unused email.<br>
   Expected: The candidate is added with the literal address `Blk 1 yes/no/feedback`.
1. Run `add n/José p/91234567 e/jose@example.com a/Blk 1 r/No role`, then `find José`.<br>
   Expected: Each is rejected with an error that names the unsupported character `U+00E9` and tells you to use printable ASCII only (`U+0020-U+007E`). No candidate is added and the list is unchanged. The same `add` with `Jose` is accepted.

### Deleting a candidate

1. Prerequisites: Run `list` so that several candidates are shown.
1. Run `delete 1`.<br>
   Expected: The first candidate is deleted. The status message shows the deleted candidate's details.
1. Run `delete` and `delete abc`.<br>
   Expected: No candidate is deleted. The message starts with `Invalid command format!` and shows the `delete` usage.
1. Run `delete 0`, then `delete 99999` (with fewer than 99999 candidates).<br>
   Expected: No candidate is deleted. The messages are `Index must be a positive integer.` and `The candidate index provided is invalid.` respectively.

### Updating status, finding and filtering

1. Run `list`, then `status 1 s/Interviewing`.<br>
   Expected: `Updated status of NAME: OLD_STATUS -> Interviewing`, and the first card shows Interviewing.
1. Run `status 1 s/Hired`, then `status 1`.<br>
   Expected: The first lists the valid statuses and the second gives the command format. The candidate is unchanged.
1. Run `find ale`, then `find eoh`.<br>
   Expected: `ale` lists Alex Yeoh, because it starts a word in the name; `eoh` gives `0 candidate(s) listed!`
1. In a test copy, add candidates named `Tan Wei Ming (Kelvin)` and `Martin Luther King, Jr.` with distinct emails. Run `find (Kelvin)`, `find King,` and `find (),`.<br>
   Expected: The first two find the matching candidate, as `find Kelvin` and `find King` do. `find (),` matches no candidates.
1. Run `filter s/Interviewing s/Offered`, then `filter s/Hired`.<br>
   Expected: The first lists only candidates with either status. The second lists the valid statuses, and the displayed list is unchanged.

### Preserving filters after candidate updates

1. Prerequisites: The sample data has only one Applied candidate. Run `list`, then `status 2 s/Applied` and `status 3 s/Applied` so that three candidates are Applied.
1. Run `filter s/Applied`, then `status 1 s/Offered`.<br>
   Expected: The updated candidate disappears, and only the remaining Applied candidates are shown. The line above the list reads `Showing 2 of M candidates (filtered). Type list to show all.`, where `M` is the total number of candidates. Repeat with `edit 1 s/Offered`.
1. Run `find Betsy` (use a name in your data), then `note 1 no/Follow up` and `edit 1 n/Other Name`.<br>
   Expected: Only matching candidates are displayed after the note. After the rename, the candidate disappears if the new name does not match.
1. Run `list`, `filter s/Applied`, `expand 1`, then `note 1 no/Updated feedback`, `status 1 s/Offered` and `edit 1 p/91234567`.<br>
   Expected: The same candidate stays expanded at index `1` after each update, including the Offered status change. Run `list` to show all candidates again.

### Viewing a candidate's full record and notes

1. Run `list`, `note 2 no/Strong on SQL`, then `expand 2`.<br>
   Expected: Only that candidate is shown, at index `1`, with `Note: Strong on SQL`. The line above the list reads `Expanded view of 1 of N candidates. Type list to show all.`
1. Run `list`, `note 1 no/`, then `expand 1`.<br>
   Expected: `Cleared note for NAME` after `note`, and `No note recorded.` in the expanded record. Clearing a candidate without a note succeeds and changes nothing.
1. Run `expand 0` and `expand abc`.<br>
   Expected: Each gives an error, and the displayed list is unchanged.
1. Run `note 1` (no prefix) and `note 1 no/first no/`.<br>
   Expected: A missing-prefix error and a duplicate-prefix error. The candidate's note is unchanged.
1. Note indicator: In a test copy, give one candidate a note and a long name, and another no note. Launch, hover over the icon, and narrow the window.<br>
   Expected: Only the candidate with a note has the sticky note icon; hovering shows `Note available`, and the icon stays visible when the long name is truncated. No note text appears on collapsed cards.

### Legal-name markers and prefix-like text

1. Run `edit 1 n/John a/L Street a/9 Road` (try `a/P Lane` too).<br>
   Expected: A clarification error asks for `a/ADDRESS` before `n/NAME`. Nothing changes.
1. Run `edit 1 a/L Street n/John`, then `edit 1 a/9 Road n/Siti a/p Ahmad`.<br>
   Expected: The first sets name `John` and address `L Street`. The second keeps the legal name intact with address `9 Road`. Both persist after a restart.
1. Run `add n/Ravi s/o Kumar p/91234567 e/lineageso@example.com a/1 Road r/No role` and `add n/Siti a/p Ahmad p/91234567 e/lineage@example.com a/1 Road r/No role`, each with an unused email.<br>
   Expected: `s/o` remains in the name, and the second candidate's legal name and address are both saved correctly because the phone field separates them.
1. Run `edit 1 a/Unit N/A`, then `edit 1 s/Applied S/Offered`.<br>
   Expected: The first says `N/` could be text or the `n/` prefix and suggests `N\/`. The second says `s/` can be given only once and suggests only `S\/`. Nothing changes.
1. Run `edit 1 a/Unit N\/A`.<br>
   Expected: The address is `Unit N/A` and the name is unchanged, also after a restart.
1. Run `edit 1 A/Unit N/A`, then `edit 1 a/Unit n/a`, then `edit 1 A/Unit N\/A`.<br>
   Expected: The first starts with `Warning: Prefixes read as additional fields: N/.` and sets name `A`, address `Unit`. The second sets name `a` and the warning lists `n/`. The third sets the address to `Unit N/A` without a warning.
1. Run `edit 1 A/Unit P/123 N/A`, then `edit 1 a/Unit t/C++ t/N\/A`.<br>
   Expected: The first warning lists both `P/` and `N/` (address `Unit`, phone `123`, name `A`). The second lists `t/, t/` and the tags are `C++` and `N/A`.
1. Run `edit 1 A/12 Kent Ridge Drive S/Offered`, then `note 1 no/Status NO/go` and `note 1 no/Status NO\/go`.<br>
   Expected: The edit treats both upper-case prefixes as fields and the warning identifies `S/`. The first note is rejected; the second saves `Status NO/go`.

### Managing and filtering roles

1. Run `add_role n/Developer` and `add_role n/Designer`, then `add n/Role Tester p/91234567 e/roles@example.com a/Test Street r/Developer r/developer r/No role` with an unused email, then `find Role Tester`.<br>
   Expected: The candidate has one Developer badge and no No role badge, also after a restart. Use their displayed index in place of `1` below.
1. Run `edit 1 r/Developer r/Designer`, then `edit 1 p/91234567`, then `edit 1 r/No role`.<br>
   Expected: Both roles remain after the phone edit; after the last command only No role remains.
1. Run `add n/Missing Role p/91234567 e/missingrole@example.com a/Test Street`, `edit 1 r/` and `edit 1 p/98765432 r/Missing`.<br>
   Expected: Specific errors, the missing-role error suggests `r/No role`, and nothing changes.
1. Assign Developer and Interviewing to one candidate, and Designer and Applied to another. Run `filter r/Developer r/Designer s/Interviewing s/Offered`, then `filter r/Designer`.<br>
   Expected: The first shows only candidates in either role with Interviewing or Offered. The second shows the Applied Designer, because each filter starts from all candidates.
1. Run `filter r/Missing`, then filter by an unused existing role.<br>
   Expected: An error that suggests `list_role` and keeps the displayed list; then zero results.
1. Run `add_role n/Documentation Tester`, `list_role`, then restart and `list_role` again.<br>
   Expected: The unused role appears once, and is still available after the restart.
1. Run `add_role n/documentation tester`, `edit_role r/No role n/Other` and `list_role extra`.<br>
   Expected: Each fails without changing candidates or the catalog.
1. Assign `Documentation Tester` to a candidate, filter by it, then run `edit_role r/Documentation Tester n/Senior Documentation Tester`.<br>
   Expected: The renamed role appears in `list_role` with the same ID in `roles.json`, the filter still matches the same candidate, and hidden candidates with the role are renamed too.
1. Run `delete_role r/Senior Documentation Tester`, then restart.<br>
   Expected: The role disappears; a candidate who loses their last role receives `no-role`, and others keep their other roles. In expanded view, the expanded candidate stays expanded at index `1`.
1. Run `delete_role r/No role`, then `clear confirm` and `list_role`.<br>
   Expected: An error leaves the built-in role intact. After the clear, candidates are gone but catalog entries remain.
1. Create at least five short roles and run `list_role`; then repeat with many roles or a long wrapping name.<br>
   Expected: The result box grows so more than two names are visible while candidate cards stay visible, and stays bounded and scrollable for large catalogs. `list` restores the ordinary height. A long multiword role wraps within the card.

### Exporting, importing and clearing

1. Run `export`.<br>
   Expected: `Exported successfully to PATH.`, where `PATH` ends in `data/export.csv`. The file starts with the header `"name","phone","email","address","status","tags","note","roles"` and has one row per candidate, including candidates hidden by a filter.
1. Copy the template from the User Guide's `import` section into `data/import.csv` with an unused email, then run `import`, then `import` again.<br>
   Expected: `Imported 1 candidate successfully.` and the candidate appears at the end of the list with `No role`. The second import gives an error naming the duplicate CSV record, and nothing is added.
1. Run `add_role n/Developer` if needed, `edit 1 r/Developer` and `export`. Replace `data/import.csv` with a copy of `data/export.csv`, run `clear confirm`, then `import`.<br>
   Expected: Every candidate returns with its previous roles, and the first candidate still has Developer.
1. In a test copy of that CSV, insert a no-break space (`U+00A0`) after a role name in a later record's `roles` array, then run `import`.<br>
   Expected: An error identifies that record and the `roles` field. No candidates or roles are added, and the filter, expanded view and both JSON files are unchanged.
1. Run `clear`, `clear 1`, then `clear confirm`.<br>
   Expected: `clear` gives the candidate count and asks you to enter `clear confirm`, deleting nothing. `clear 1` gives an invalid-command-format error. `clear confirm` gives `All candidates have been cleared.`, and `list_role` still shows your roles. Running `clear confirm` straight away also works.

### Help, summary cards and command history

1. Run `help`, or press `F1`.<br>
   Expected: A help window shows the User Guide's URL and a **Copy URL** button.
1. Summary cards: Widen the window until all cards appear, then run `filter s/Interviewing` and `find Alex`; add, update a status, delete and clear candidates; then narrow the window.<br>
   Expected: One Total candidates card and one card per status appear, including statuses with zero. Shown counts match the displayed list while overall counts include hidden candidates, and all counts update after each change. When the window is too narrow, all cards disappear together and the logo stays; widening restores them with current counts.
1. Command history: Restart HRvest so that the history is empty, then run `list`, then `find Alex`, then type `fin` without pressing Enter. Press `Up` three times, then `Down` twice.<br>
   Expected: `Up` shows `find Alex`, then `list`, and stays on `list` at the earliest entry. `Down` returns to `find Alex`, then restores the `fin` draft (or clears the box if there was none).
1. Run a mistyped command such as `filtwr s/Applied`, then press `Up`.<br>
   Expected: The rejected command stays in the box in red so that you can fix it, and `Up` shows the command before it (`find Alex`).

### Saving data

<box type="warning">

**Caution:** These tests change the data files (_data files_). Run them only on a copy.
</box>

1. Null entries
   1. Back up both JSON files together, insert `null` beside a valid candidate in the `persons` array, then launch.<br>
      Expected: HRvest handles a data loading failure and opens a window, rather than crashing with a `NullPointerException`. Restore the backup. Repeat with `null` inside one candidate's `tags` array.
1. Corrupted or missing files
   1. Prerequisites: Use a test folder with both files backed up. Close HRvest, change one candidate's email to `invalid`, and launch.<br>
      Expected: A warning gives the full path of a new `HRvest-<timestamp>-<random>-corrupt.json` beside the data file, whose bytes match the invalid original. The app shows an empty list, already saved to `data/HRvest.json`.
   1. Run `list`, `find Alice` and `help`, then restart.<br>
      Expected: The backup is unchanged, the list stays empty and no extra backup or recovery warning appears.
   1. Close HRvest, repair the email in a copy of the backup, replace the candidate file with it, and restore the companion `.roles.json` backup to `data/roles.json` if present.<br>
      Expected: All candidates return after relaunch.
   1. Repeat with an invalid catalog entry, an unknown candidate role ID, malformed JSON and duplicate emails that differ only in case.<br>
      Expected: The same recovery process, with both existing originals backed up first and a separate backup for each invalid file.
   1. In a separate test folder, keep only an invalid `data/roles.json` and launch.<br>
      Expected: The warning points to a catalog-only `.roles.json` backup.
   1. Edit a valid test file to contain `"note": ""` and `"status": " Applied "`.<br>
      Expected: All candidates load, the note is absent, the status is Applied, and no recovery backup is created.
   1. Make the data file invalid and deny writing in its folder, then launch from a terminal.<br>
      Expected: Startup stops before a window opens, an actionable terminal error appears, and the original file is unchanged. Restore permissions afterwards.
   1. Move the data file out of the test folder and launch.<br>
      Expected: Sample candidates appear without a recovery warning or backup.
