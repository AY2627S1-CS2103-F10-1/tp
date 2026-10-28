---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# HRvest Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* HRvest is a fork of [AddressBook-Level3](https://github.com/se-edu/addressbook-level3). Most of the original code remains unchanged; changes so far have focused on user-facing aspects of the application. The design descriptions and diagrams are also initially inherited from AddressBook-Level3.

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

The UI consists of a `MainWindow` and its parts, such as `CandidateSummaryHeader`, `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. `CandidateSummaryHeader` composes the top row from an `AppLogo` and a group of summary cards: one `CandidateSummaryCard` for all candidates and a `StatusSummaryCard` for each recruitment status. These UI parts, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

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

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
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

Candidate command fields and `find` keywords use `ParserUtil.verifyPrintableAscii` before their existing validation. This policy is deliberately parser-only: placing it in model constructors would also reject existing valid non-ASCII addresses or notes during JSON loading. Status values, indexes, and confirmation keywords retain their dedicated checks.

`ArgumentTokenizer.tokenizeWithValidation` checks selected raw prefix values before trimming; otherwise trailing control characters could disappear, and invisible-only notes or tags could be mistaken for clearing requests. `AddressBookParser` preserves trailing argument characters for the same reason. The regular tokenizer remains available for commands outside this policy. Character errors take precedence over duplicate-prefix and field validation after tokenisation; command-specific unsupported-prefix checks can still run first. JSON and CSV serialization continue to escape values independently of this input policy.

* Tabs, line breaks, accented letters, emoji, curly quotes, non-breaking spaces (`U+00A0`), and zero-width spaces (`U+200B`) are rejected, even when copied text looks normal. Use ordinary spaces and straight quotes instead.
* Unsupported characters are checked before surrounding spaces are trimmed or empty values are interpreted as clearing a field. They are not silently removed or replaced.
* Errors identify the field prefix or search input for the unsupported code point, for example: `a/ contains unsupported character U+00A0. Use printable ASCII only (U+0020-U+007E).` Remove or replace that character and retry. Rejected commands leave candidate data unchanged.
* Ordinary spaces around field values are still trimmed. Empty notes and tags can still be cleared as described in the User Guide.
* Status values, candidate indexes, and `clear confirm` retain their own validation rules rather than this character check. Command words and prefix syntax are unchanged; use ordinary spaces before prefixes.
* This restriction applies only to new command input, not loading or exporting existing valid records. Existing non-ASCII text is preserved, but cannot be entered as a `find` keyword under this policy. JSON saving and CSV export retain their existing escaping; quotes and commas do not need to be removed from otherwise valid values.

#### Displayed-index validation

`status`, `note`, `expand`, `edit`, and `delete` all read their index with `ParserUtil.parseDisplayedIndex`, so they report numeric index errors the same way. The exact messages are listed under each command in the User Guide. A rejected command leaves candidate data, filters, and the expanded view unchanged.

* A number too large for an `int` is reported as an out-of-range index rather than a format error, because no displayed list can be that long.
* A known numeric problem, such as `0`, is reported instead of the generic `Invalid command format!`, so the user knows exactly what to fix.
* Each command keeps its own validation order. For example, `note` still reports a missing `no/` before an invalid index. This keeps the change limited to index errors.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="600" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores a shared role catalog in `AddressBook`, separately from candidate assignments.
* enforces email-only uniqueness through `Person.isSamePerson`, using case-insensitive comparison. `ParserUtil.parseEmail` trims command input, while `Email` validates stored values and preserves case. Full `Person.equals` and `Email.equals` remain case-sensitive. Add, edit, bulk replacement and JSON loading share this identity rule. Edit permits unchanged identity because the list already enforces uniqueness; a changed identity is checked against the entire address book, not only the displayed list.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* tracks whether the displayed list is in expanded view as transient state. The `Logic` API exposes this read-only property to the `UI`; it is not persisted.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>

#### Role catalog design

The catalog stores roles independently of candidates so that unused roles remain available, even after all candidates are cleared. Commands identify roles by name rather than list position, so catalog sorting does not change their targets.

* Each candidate has at least one role from the catalog. `Person` removes `No role` whenever named roles are assigned, including when loading older mixed assignments. The protected `No role` entry provides a fallback when deleting the candidate's last role.
* Roles keep a stable identity across renames. A rename updates the catalog and all assigned candidates together, including candidates outside the displayed list.
* Role lookup keys use Unicode NFC before case conversion; display names and IDs retain their stored values. Catalogs with canonical name collisions are rejected through the existing paired-file backup path, rather than silently merging identities and assignments.
* Parsers validate role names through `ParserUtil.parseRoleName`; commands resolve membership through `RoleCatalog` before changing any data. Person edits replace all assignments in one `setPerson` call.
* `ParserUtil.parseRoleName` also rejects no-break, zero-width and other invisible spaces, because they would let a pasted name look blank or identical to an existing role. As with the printable-ASCII check for candidate fields, this check is parser-only, so catalog entries saved by earlier versions still load.
* Role filters retain IDs rather than `Role` objects because role equality includes the display name. A rename therefore keeps matching the same role; a newly created role with a deleted role's name does not inherit its filter. Status and role groups combine with `AND`, with `OR` within each group.
* The model validates catalog and assignment changes together before applying them, preventing inconsistent references or partial updates. Global role updates retain the current filter or reselect the expanded candidate with their updated roles.

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="700" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which coordinates candidate and role files through `RoleDataStorage` and delegates preference storage to `JsonUserPrefsStorage`.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonAddressBookStorage` never overwrites the data file in place, so a failed or interrupted save leaves the previous file intact. It writes the JSON to a uniquely named `HRvest-<random>.tmp` file in the same folder, gives it the existing file's access permissions, and then atomically replaces the data file with it. Filesystems that cannot replace a file atomically reject the save, and permission errors abort it; `LogicManager` reports either error. A symbolic link to the data file is kept, and its target is replaced. The temporary file is deleted if the save fails, but a crash or power loss can leave it behind; later saves ignore such files, and they can be deleted once HRvest is closed.

Before loading preferences, recovering files or constructing storage, `MainApp` acquires a `DataDirectoryLock` on `data/.hrvest.lock`. The OS lock is held through preference saving at shutdown and released on failed initialization. This prevents a second process from loading stale candidates and later overwriting newer data, even through a read command that triggers saving. The lock file stays in place after release: deleting it could let two processes lock different files with the same path. Separate data folders remain independent, and process termination releases the OS lock automatically.

During startup, `MainApp` uses the storage component's `CandidateDataInitializer` to load candidate records before creating the UI or command logic. A `DataLoadingException` triggers a persistent, uniquely named `HRvest-<timestamp>-<random>-corrupt.json` backup through `Storage`, followed by a journal-coordinated save of an empty candidate list and the built-in role catalogue. The local-time `yyyyMMddTHHmmss` timestamp makes the newest backup easy to find when several accumulate. The backup copies the original bytes first and then applies the original's supported access restrictions, so a write-restricted data file can still be backed up. An unsuccessful copy removes only its own partial backup where possible. Recovery resolves symbolic links to existing targets; inaccessible files and dangling links are not treated as missing files.

Both backup and replacement saving must finish before commands become available. Otherwise startup shows an actionable error dialog and exits after dismissal, preserving the original and any completed backup. After successful recovery, `UiManager` displays the backup path and restoration instructions. A later launch loads the saved empty file normally, avoiding repeated backups or sample data. Preference-file recovery remains separate.

The activity diagram below summarizes candidate data initialization and recovery. Load failures include malformed JSON, invalid candidate data, and null entries rejected during deserialization or validation.

<puml src="diagrams/DataRecoveryActivityDiagram.puml" alt="Startup candidate data loading and recovery, including backup and replacement save failures" />

#### Coordinating candidate and role storage

Candidates store role IDs, while `roles.json` stores the catalog. `RoleDataStorage` loads the catalog first so that it can validate candidate assignments. Older candidates without role assignments receive `No role`; invalid assignments use the data-recovery process described above.

The two files must stay consistent, but a filesystem rename can replace only one file at a time. To support recovery:

* A rollback journal, `roles-transaction.json`, preserves the previous files before either is replaced. Failed or interrupted saves restore that pair before data is loaded again.
* Invalid journals or incomplete rollback block further saves, preserving the recovery data. Candidate, catalog, and journal paths must identify different files, and concurrent HRvest instances are unsupported.
* Recovery from invalid candidate or catalog data backs up both originals before saving an empty candidate list and the built-in catalog. The catalog backup uses the candidate backup name with `.roles.json` appended; a catalog-only backup is used when no candidate file exists.
* If saving a command fails, `LogicManager` restores the previous model data, filter, and expanded view so that memory and saved data remain consistent after successful rollback.
* `LogicManager` compares the immutable candidate records and role catalog before and after execution. It saves only when persisted data changes, so a storage error cannot block view changes, help, or exit. Import retains its separate save and rollback pipeline, including empty imports.

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
* `add` and `edit` reject `no/` through `ParserUtil#verifyNoNotePrefix`, so `note` is the only command that changes a note.
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

* **Session-only, no persistence.** Keeping history in memory avoids a storage format change for a quality-of-life feature. Users who want cross-session recall can re-type the few commands they care about or wait for a persisted variant later.
* **Record failed commands too.** The whole point of Up/Down is to iterate faster; forcing users to retype a mistyped `filtwr s/Interviewing` would defeat that.
* **Hard cap at 100 entries.** Prevents unbounded growth in long sessions. 100 is chosen to be well above the dozen or so commands a typical session uses while still being cheap.
* **Event filter, not handler.** A filter runs before the `TextField`'s own key-handling, so the arrow keys do not also move the caret while cycling.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th candidate in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new candidate. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the candidate was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the candidate being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### CSV import

Import adds a batch of candidates to the complete address book, regardless of the current filter. `AddressBookParser` uses `ImportCommandParser` to create an `ImportCommand`, and `LogicManager` delegates the operation to `ImportOperation`.

`StorageManager` delegates reading `import.csv` to `CsvImportReader`. Missing files and read failures produce an error explaining the problem and expected path. HRvest reads the file into a bounded buffer and parses only that copy; later edits do not affect the buffered data. `CandidateCsvParser` uses `StrictCsvParser` to parse the buffered CSV and validates candidate fields through the existing command/model rules. Import and export share their headers through `CandidateCsvFormat`.

HRvest does not lock the file or check for concurrent changes: these may cause a normal read/validation error or go undetected and import valid-looking altered data. HRvest does not guarantee that bytes read during an external edit represent one consistent file version. Do not externally modify saved JSON or run another HRvest instance against the same saved data during import.

`ImportOperation` checks for duplicates within the batch and against existing candidates before updating the model and saving through storage. It manages its own rollback because import bypasses `LogicManager`'s ordinary save path. Reading or validation failures leave the model unchanged; application or save failures trigger restoration of the previous data and view. Success shows the full candidate list and exits expanded view.

Batch combination installs the validated list once, preserving existing and CSV order. `UniquePersonList` checks bulk replacements with a case-insensitive email set instead of comparing every pair. This keeps the same identity rule while avoiding quadratic validation during import, snapshots, and saves.

Failed saves retain the previous saved JSON. If rollback itself fails, HRvest reports that separately; stop using the app and check saved data before restarting. Crash/power-loss recovery is not an in-memory rollback guarantee.

See the [User Guide](UserGuide.md#importing-candidates-import) for import limits and usage restrictions.

#### Import CSV format and validation

**Required CSV template:** UTF-8 with or without a single leading byte order mark (BOM), and exact lowercase headers in the order below. Fields may be unquoted or double-quoted. Fields containing commas or quotes must be double-quoted; double any embedded quotes. Quotes must enclose the entire field, with no extra text or spaces outside the closing quote. Quoting does not bypass field validation. LF (`\n`) and CRLF (`\r\n`) record endings are accepted, and the last record need not end with a newline. Empty trailing lines are allowed, but blank lines before the header or between records, whitespace-only trailing lines, bare CR separators, alternate delimiters, and incorrect column counts are rejected.

```csv
"name","phone","email","address","status","tags","note"
"Example Candidate","01234567","candidate@example.com","123 Example Road","Applied","[""Java"",""backend""]",""
```

Configure your editor to save the example as UTF-8 with LF or CRLF line endings. Excel's optional quoting is accepted when saving as comma-delimited UTF-8 CSV; other encodings or delimiters are not. HRvest accepts the leading BOM written by `export`, so you can copy or rename `data/export.csv` to `data/import.csv` without removing it. The file must still meet validation, duplicate, and resource-limit rules. A BOM-only file or repeated leading BOMs are rejected. Only one encoding marker immediately before the header is ignored; markers inside quoted fields are literal data subject to field validation.

Existing field rules apply, including trimming as in commands. Status accepts defined values ignoring case and surrounding whitespace; empty or unknown statuses fail. Tags must be a JSON array of valid tag-name strings, without nulls, nested values, or trailing JSON. Tags need not be sorted; surrounding whitespace is trimmed, repeated identical names collapse, and `Java` and `java` remain distinct. An empty or whitespace-only note means no note. Addresses and notes use their model validation rather than the command-only printable-ASCII check, so supported legacy Unicode data can be exported and imported again. Addresses retain command-style surrounding-space trimming. Nonblank notes are stripped of surrounding whitespace and limited to 500 Unicode code points; tabs, line breaks, and non-ASCII text inside a quoted note are preserved. Formula-like text remains ordinary data where the field permits it; HRvest never executes it.

CSV syntax errors identify the record (the header is record 1) and, for field-specific syntax errors, the one-based field number and name. Messages distinguish missing or extra columns, misplaced or unclosed quotes, content after a closing quote, and unsupported bare CR endings, with guidance on correcting the file. Record numbers count CSV records, not physical lines inside quoted fields.

### CSV export

The `export` command creates a CSV copy of all candidate records, regardless of the current search or filter. It leaves the application's data and current view unchanged.

`ExportCommand` requests an export through `CommandResult`. `LogicManager` passes the complete address book to `StorageManager`, which delegates CSV formatting and writing to `CsvExportWriter`. This keeps file operations in the storage component rather than the model.

You can refer to the diagram below as a reference.

<puml src="diagrams/ExportFeatureClassDiagram.puml" alt="ExportFeatureClassDiagram" />

The writer exports one row per candidate, with a header and the name, phone, email, address, status, tags and note fields, to `export.csv` beside the JSON data file. The data file path is fixed at `data/HRvest.json`, so the export is always `data/export.csv`. The file starts with a UTF-8 byte order mark so that spreadsheet programs such as Excel detect the encoding; without it, Excel reads the file as ANSI and garbles non-ASCII text. Every field is quoted, embedded quotes are doubled. Literal values, including embedded line breaks, are preserved. Status uses its displayed label; an absent note is empty. Tags occupy one CSV field containing a JSON array, such as `["Java","backend"]`, with CSV escaping applied. Tag names are sorted in case-sensitive Java string order, preserving casing; no tags gives `[]`.

**Spreadsheet caution:** This is raw-data CSV, not a spreadsheet document. Import columns as text to preserve leading zeroes in phone numbers and prevent formula-like stored text from being interpreted as formulas. HRvest does not add sanitisation prefixes or formula wrappers, because they would change the stored values in the file; the User Guide warns users instead. Spreadsheet auto-detection can still alter values when the file is opened directly.

HRvest shows a confirmation after the export completes, or an error if it fails. Export bypasses JSON saving because it does not change candidate data. It is non-destructive to HRvest's stored records: the CSV is a separate copy of data that remains available in the application. For this reason, export uses direct writing with failure reporting rather than rollback or deletion of a failed output file; the user can retry after resolving the problem. This differs from saving HRvest's source data, where atomic replacement protects the existing records. The non-destructive guarantee does not extend to an earlier CSV: each export directly overwrites it, so an interrupted or failed write may destroy the previous export and leave an empty or incomplete file.

Failures display `Export failed:` with the reason and destination when available. Check that the file/folder is writable. Directories, symbolic links (including dangling links), and other non-regular destinations are rejected without deleting them. A missing data directory is created first, as JSON saving does. If the required parent directory cannot be determined or created, export fails. No failure redirects output to another location. Candidate data and the current view remain unchanged; a failure after opening the destination warns that the export may be incomplete and the previous export may be lost.


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

Priorities: High (must have) `* * *`, Medium (nice to have) `* *`, Low (unlikely to have) `*`.

Target user: a hiring manager at a tech startup who hires for one to three full-time roles at a time, tracks roughly 10 to 50 candidates, and prefers keyboard commands to mouse-driven forms.

#### Must-have (MVP, `* * *`)

| Priority | As a …           | I want to …                                          | So that I can…                                                     |
|----------|-------------------|-------------------------------------------------------|---------------------------------------------------------------------|
| `* * *`  | new user          | launch HRvest and see usage instructions on screen    | start using the app without reading external docs                   |
| `* * *`  | new user          | add a new candidate with contact details and roles    | keep a record of every candidate I am considering                   |
| `* * *`  | new user          | search for a candidate by name                        | view their details without scrolling through the whole list         |
| `* * *`  | new user          | edit a candidate's details and role assignments      | correct mistakes or outdated details without re-adding them         |
| `* * *`  | user              | delete a candidate from the list                      | keep my list focused on candidates still under consideration        |
| `* * *`  | user              | list every candidate currently in the app             | see every candidate in my pipeline in one view                      |
| `* * *`  | user              | filter the list by candidate status                   | focus on one hiring stage, for example only those interviewing      |
| `* * *`  | user              | update a candidate's status after an interview        | keep the pipeline accurate as candidates move through stages        |
| `* * *`  | user              | view a candidate's full details including their note  | prepare for the next conversation with them                         |
| `* * *`  | user              | see a confirmation when an action succeeds            | confirm that the command did what I intended                        |

#### Nice-to-have (`* *`)

| Priority | As a …           | I want to …                                                      | So that I can…                                                       |
|----------|-------------------|-------------------------------------------------------------------|-----------------------------------------------------------------------|
| `* *`    | familiar user     | find a candidate by a partial keyword of their name               | find a candidate whose full name I do not remember                    |
| `* *`    | familiar user     | append a note to a candidate without overwriting existing notes   | keep a complete history of my interactions with that candidate        |
| `* *`    | familiar user     | create roles before adding candidates                             | prepare the catalog for upcoming hiring                             |
| `* *`    | familiar user     | list all available roles, including unused ones                   | reuse an existing role instead of creating a duplicate                |
| `* *`    | familiar user     | rename a role across all assigned candidates                      | correct a job title consistently                                      |
| `* *`    | familiar user     | remove an obsolete role while keeping candidates' other roles     | keep the catalog relevant without losing unrelated hiring details   |
| `* *`    | familiar user     | filter candidates by the role they applied for                    | focus on one open role at a time                                      |
| `* *`    | familiar user     | combine role and status in a single filter command                | isolate a specific group without running two separate searches        |
| `* *`    | familiar user     | track whether a candidate has accepted an offer                   | know which offers are still pending a response                        |
| `* *`    | expert user       | import a batch of shortlisted applicants from an external source  | avoid retyping candidates sourced from LinkedIn, Indeed, or email     |
| `* *`    | expert user       | mark certain follow-up tasks as done                              | keep track of what I have completed versus what I still need to do   |
| `* *`    | expert user       | batch-update statuses after a mass interview day                  | avoid updating candidates one by one after large hiring events        |

#### Low-priority (`*`)

| Priority | As a …           | I want to …                                              | So that I can…                                                       |
|----------|-------------------|-----------------------------------------------------------|-----------------------------------------------------------------------|
| `*`      | expert user       | view analytics on the hiring funnel                       | report hiring performance to stakeholders                             |
| `*`      | expert user       | set custom follow-up reminders on specific candidates     | follow up with every candidate on time                                |
| `*`      | expert user       | export candidate data as a report                         | share hiring metrics with people outside the app                      |
| `*`      | expert user       | integrate HRvest with job boards such as LinkedIn/Indeed  | add new applicants to HRvest automatically                            |

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
1. HRvest updates the candidate's status and shows the updated candidate.

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

**Use case: UC04 - Edit a candidate's details**

**MSS**

1. User performs <u>List candidates (UC02)</u>.
1. User requests to change one or more of the candidate details of a specific candidate in the list.
1. HRvest updates the candidate's details and shows the updated candidate.

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

1. User prepares `import.csv` in the required location and enters `import`.
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

### Non-Functional Requirements

1. HRvest should run on Windows, Linux, and macOS with Java `25` installed, without requiring an installer or other application-specific software beyond a single JAR file.
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
* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Note**: A single text remark about a candidate, such as interview feedback or follow-up context.
* **Private contact detail**: A contact detail that is not meant to be shared with others

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Launching a second instance

   1. Keep HRvest running and double-click the same JAR again.<br>
      Expected: An error dialog asks you to close the first instance. Dismissing it exits the second instance; the first remains usable and its data is unchanged.

   1. Close the first instance and launch the JAR again.<br>
      Expected: HRvest opens normally with the saved data.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Rejecting note input in `add`

1. Run `add n/A p/123 e/a@x.com a/Blk 1 no/great candidate`. Expected: The error explains that `add` does not support `no/` and directs you to `note`. No candidate is added and existing data stays unchanged.
1. Run `add n/A p/123 e/a@x.com a/Blk 1 no/` (empty note). Expected: The same error, with no candidate or file changes.
1. Repeat with `no/` before the required fields, or after a tag/status field. Expected: The same error, with no candidate or file changes.
1. Run `add n/A p/123 e/a@x.com a/Blk 1 yes/no/feedback r/No role` with an unused email. Expected: The candidate is added with the literal address `Blk 1 yes/no/feedback`.

### Deleting a candidate

1. Deleting a candidate while all candidates are being shown

   1. Prerequisites: List all candidates using the `list` command, with multiple candidates in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No candidate is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Preserving filters after candidate updates

1. Run `find Betsy` (use a candidate name in your test data), then `note 1 no/Follow up`. Expected: Only the matching candidates remain displayed. `delete 1` deletes the displayed candidate.
1. Run `filter s/Applied`, then `status 1 s/Offered`. Expected: The updated candidate disappears, and only remaining Applied candidates are shown. The line above the list reads `Showing N of M candidates (filtered). Type list to show all.`, where `M` still counts the updated candidate. Repeat with `edit 1 s/Offered`.
1. Run `find Betsy`, then change that candidate's name with `edit 1 n/Other Name`. Expected: The candidate disappears if the new name does not match.
1. Run `filter s/Applied`, `expand 1`, then `note 1 no/Updated feedback`, `status 1 s/Offered`, and `edit 1 p/91234567`. Expected: The same candidate remains expanded at index `1` after each update, including the Offered status change. Expansion replaces the earlier status filter with a selection of the candidate. Run `list` to show all candidates again.

### Header summary cards

1. Launch HRvest and widen the window until all summary cards appear beside the logo. Confirm there is one Total candidates card and one card per status, including statuses with zero candidates.
1. Run `list`, then `filter s/Interviewing` and `find Alex`. Check that shown counts match the displayed candidates, while overall counts continue to include hidden candidates. An empty search result should show zero shown counts without clearing overall counts.
1. Add, update a candidate's status, delete, and clear candidates. Check that total and per-status counts update, including when a status edit removes a candidate from the current filter.
1. Slowly narrow the window. Confirm that all cards disappear together, the logo remains visible, and neither partial cards nor a wrapped row appears. Widen it again and confirm every card returns without flickering or clipping.
1. Change candidate data while the cards are hidden, then widen the window. Confirm the restored counts are current. Repeat resizing with wider count values and check that full labels and numbers remain visible whenever the row is shown.

### Candidate note indicator

1. Clearing a note

   1. Run `note 2 no/Follow up`, then `note 2 no/`. Expected: The feedback is `Cleared note for NAME`, and that candidate's sticky note icon disappears. Other candidates' notes and all contact details remain unchanged.
   1. Run `list`, then `expand 2`. Expected: The candidate shows `No note recorded.` Close and restart HRvest; the note remains absent.
   1. Run `list`, `find NAME`, and `note 1 no/` for a candidate with a note. Expected: The note is removed from the displayed candidate. Repeat clearing a candidate without a note; the command succeeds without changing their details.
   1. Run `note 1` without the prefix, or `note 1 no/first no/`. Expected: A missing-prefix or duplicate-prefix error leaves the candidate's note unchanged.

1. Prerequisites: Close HRvest and back up the saved JSON file. In a test copy, give one candidate a non-blank `note` string, leave another candidate without a note, and give the candidate with a note a long name.
1. Launch HRvest using the test data file. Expected: Only the candidate with a note has a sticky note icon beside the name; no note text appears on either card.
1. Hover over the icon. Expected: The tooltip reads `Note available`.
1. Narrow the window until the long name is truncated. Expected: The sticky note icon remains visible, and the candidate's status still appears below the contact details.
1. Run `expand INDEX` for the candidate with a note. Expected: The full note appears and the sticky note icon remains visible. Run `list`, then expand the candidate without a note. Expected: `No note recorded.` appears and no icon space is reserved. Run `list` again. Expected: All candidates return to collapsed cards with note contents hidden.

### Assigning and filtering candidate roles

1. In a test copy without Developer or Designer roles, run `add_role n/Developer` and `add_role n/Designer`. Run `add n/Role Tester p/91234567 e/roles@example.com a/Test Street r/Developer r/developer r/No role` with an unused email, then `find Role Tester`. Expected: The new candidate has only one Developer badge and no No role badge. Restart and find the candidate again; the assignment remains. Use their displayed index in place of `1` below.
1. Run `edit 1 r/Developer r/Designer`, then `edit 1 p/91234567`. Expected: Both roles remain after the phone edit. Run `edit 1 r/No role`; only No role remains.
1. Try `add n/Missing Role p/91234567 e/missingrole@example.com a/Test Street` with an unused email, `edit 1 r/`, and `edit 1 p/98765432 r/Missing`. Expected: Specific errors leave all candidates unchanged; the missing-role error suggests `r/No role`.
1. Assign the Developer role and Interviewing status to one candidate and the Designer role and Applied status to another. Run `filter r/Developer r/Designer s/Interviewing s/Offered`. Expected: Only candidates in either role with status Interviewing or Offered appear. Run `filter r/Designer`; the Applied candidate appears because each filter starts from all candidates.
1. Run `filter r/Missing`. Expected: An error suggests `list_role` and preserves the displayed list. Filter by an unused existing role; expect zero results.
1. Filter by Developer, then rename Developer. Expected: The same candidate still matches. Change their assignments so they no longer have that role; they disappear. In expanded view, an assignment edit keeps the candidate expanded instead.
1. Create and assign a long multiword role. Resize the window and expand the candidate. Expected: All role badges remain readable and wrap within the card.

### Managing roles

Use a separate test folder and keep a matching backup of candidate and catalog files.

1. Run `add_role n/Documentation Tester`, then `list_role`. Expected: The unused role appears once. Restart and list again; it remains available.
1. Run `add_role n/documentation tester`, `edit_role r/No role n/Other`, and `list_role extra`. Expected: Each fails without changing candidates or the catalog.
1. Run `edit_role r/Documentation Tester n/Senior Documentation Tester`. Expected: The renamed entry appears in `list_role`; its ID in `roles.json` is unchanged.
1. Assign `Senior Documentation Tester` to two candidates with `edit INDEX r/Senior Documentation Tester`; give one another existing role using a second `r/` in the same edit. Filter so one candidate is hidden, then rename the role again. Expected: Both assignments update, their role IDs stay unchanged, and other fields and assignments are preserved.
1. Restart and delete the renamed role with `delete_role r/NAME`, substituting its current name. Expected: The catalog entry disappears; a candidate losing their last role receives `no-role`, while a candidate with another role retains it. Close and inspect the saved assignments, then restart to confirm they load.
1. Expand a candidate assigned to a named role. Rename that role, then delete it. Expected: The same candidate stays expanded at index `1` with the updated assignments. Repeat while expanding a candidate without that role; they also stay expanded.
1. Run `delete_role r/No role`. Expected: An error leaves the built-in role intact. Run `clear confirm`, then `list_role`. Expected: Candidates are removed while remaining catalog entries survive. Restore both test files together after testing.

### Saving data

1. Checking null entries in a test copy of the data file

   1. Close HRvest and back up `data/HRvest.json` and `data/roles.json` together (if the catalog file exists). Insert `null` alongside a valid candidate in the `persons` array, then launch HRvest. Expected: The app handles a data loading failure and opens a window, rather than crashing with a `NullPointerException`. Close HRvest and restore the valid backup before running further commands.
   1. Restore the test copy, insert `null` in one candidate's `tags` array, and repeat. Expected: The same controlled data loading failure. Restore the valid backup after testing.

1. Dealing with missing/corrupted data files

   1. Prerequisites: Use a test folder and back up its candidate and catalog files together. Close HRvest, make the test data file invalid (for example, change one candidate's email to `invalid`), and launch. Expected: A warning gives the full path of a new `HRvest-<timestamp>-<random>-corrupt.json` beside the data file, where the timestamp matches the launch time; its bytes match the invalid original. The app shows an empty list, already saved to `data/HRvest.json`.
   1. Run `list`, `find Alice`, and `help`. Expected: The backup remains unchanged. Restart HRvest. Expected: The list stays empty and no additional backup or recovery warning appears.
   1. Close HRvest, copy the backup, repair the invalid email in the copy, and replace the candidate file with that copy. Restore the companion `.roles.json` backup to `data/roles.json` if present. Expected: All candidates return after relaunch; the backup remains unchanged.
   1. Repeat with an invalid catalog entry and with an unknown candidate role ID. Expected: Both existing originals are backed up before an empty candidate list and built-in catalog are saved. Restore both files from the matching backup pair before continuing.
   1. In a separate test folder, keep only an invalid `data/roles.json` and launch. Expected: The warning points to a catalog-only `.roles.json` backup. Close HRvest and restore its repaired copy to `data/roles.json`.
   1. Repeat with malformed JSON and duplicate emails that differ only in case. Expected: The same recovery process, with a separate backup for each invalid file.
   1. Close HRvest and edit a valid test file to contain `"note": ""` and `"status": " Applied "`. Expected: All candidates load, the note is absent, the status is Applied, and no recovery backup is created.
   1. Make the test data file invalid and deny writing in its folder. Launch from a terminal with `java -jar HRvest.jar`. Expected: Startup stops before a window opens, an actionable terminal error appears, and the original file is unchanged. Restore permissions after testing.
   1. Move the data file out of the test folder and launch. Expected: Sample candidates appear without a recovery warning or backup.

1. _{ more test cases … }_
