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

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

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
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

For role-management commands, `AddressBookParser` selects `RoleCommandParser` to parse role names and create the corresponding `Command`. During execution, the command delegates catalogue validation and assignment updates to the `Model`.

### Model component
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="600" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores a shared role catalogue in `AddressBook`, separately from candidate assignments.
* enforces email-only uniqueness through `Person.isSamePerson`, using case-insensitive comparison. `ParserUtil.parseEmail` trims command input, while `Email` validates stored values and preserves case. Full `Person.equals` and `Email.equals` remain case-sensitive. Add, edit, bulk replacement and JSON loading share this identity rule. Edit permits unchanged identity because the list already enforces uniqueness; a changed identity is checked against the entire address book, not only the displayed list.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* tracks whether the displayed list is in expanded view as transient state. The `Logic` API exposes this read-only property to the `UI`; it is not persisted.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>

#### Role catalogue design

The catalogue stores roles independently of candidates so that unused roles remain available, even after all candidates are cleared. Commands identify roles by name rather than list position, so catalogue sorting does not change their targets.

* Each candidate has at least one role from the catalogue. The protected `No role` entry provides a fallback when deleting the candidate's last role.
* Roles keep a stable identity across renames. A rename updates the catalogue and all assigned candidates together, including candidates outside the displayed list.
* The model validates catalogue and assignment changes together before applying them, preventing inconsistent references or partial updates.

### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103-F10-1/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="700" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which coordinates candidate and role files through `RoleDataStorage` and delegates preference storage to `JsonUserPrefsStorage`.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonAddressBookStorage` writes UTF-8 JSON to a temporary file in the destination directory, then atomically replaces the saved file after writing completes. Symbolic-link destinations are resolved to their existing targets before creating the temporary file and replacing the target, preserving the links. Dangling links cause an `IOException` without changing the links or creating files. On filesystems supporting `AclFileAttributeView`, the existing destination's ACL is applied to the temporary file before writing JSON. POSIX permissions are also copied before writing on filesystems supporting `PosixFileAttributeView`. New destinations retain default ACLs and POSIX permissions. ACL or POSIX permission errors, including `AccessDeniedException`, abort the save and trigger temporary-file cleanup. The existing file's access restrictions are not replaced with default permissions when access is denied; `LogicManager` reports the permission error. Failed writes leave the previous saved file intact. Filesystems that cannot perform atomic replacement reject the save instead of attempting a potentially partial overwrite. A filesystem root is rejected as a destination with an `IOException`.

Temporary files use unique names of the form `HRvest-<random>.tmp`, so a later save does not reuse or overwrite an existing temporary file. The `finally` block attempts to remove the current save's temporary file when execution unwinds normally, including after an `IOException` or an unchecked exception. A forced JVM termination or power loss can prevent this cleanup and leave an orphaned temporary file. Subsequent saves ignore these files. After closing all HRvest instances, leftover `HRvest-*.tmp` files can be removed manually; keep the configured JSON data file and any symbolic-link target.

During startup, `MainApp` uses the storage component's `CandidateDataInitializer` to load candidate records before creating the UI or command logic. A `DataLoadingException` triggers a persistent, uniquely named `HRvest-<timestamp>-<random>-corrupt.json` backup through `Storage`, followed by a journal-coordinated save of an empty candidate list and the built-in role catalogue. The local-time `yyyyMMddTHHmmss` timestamp makes the newest backup easy to find when several accumulate. The backup copies the original bytes first and then applies the original's supported access restrictions, so a write-restricted data file can still be backed up. An unsuccessful copy removes only its own partial backup where possible. Recovery resolves symbolic links to existing targets; inaccessible files and dangling links are not treated as missing files.

Both backup and replacement saving must finish before commands become available. Otherwise startup aborts with an actionable terminal error, preserving the original and any completed backup. After successful recovery, `UiManager` displays the backup path and restoration instructions. A later launch loads the saved empty file normally, avoiding repeated backups or sample data. Preference-file recovery remains separate.

The activity diagram below summarizes candidate data initialization and recovery. Load failures include malformed JSON, invalid candidate data, and null entries rejected during deserialization or validation.

<puml src="diagrams/DataRecoveryActivityDiagram.puml" alt="Startup candidate data loading and recovery, including backup and replacement save failures" />

#### Coordinating candidate and role storage

Candidates store role IDs, while `roles.json` stores the catalogue. `RoleDataStorage` loads the catalogue first so that it can validate candidate assignments. Older candidates without role assignments receive `No role`; invalid assignments use the data-recovery process described above.

The two files must stay consistent, but a filesystem rename can replace only one file at a time. To support recovery:

* A rollback journal, `roles-transaction.json`, preserves the previous files before either is replaced. Failed or interrupted saves restore that pair before data is loaded again.
* Invalid journals or incomplete rollback block further saves, preserving the recovery data. Candidate, catalogue, and journal paths must identify different files, and concurrent HRvest instances are unsupported.
* Recovery from invalid candidate or catalogue data backs up both originals before saving an empty candidate list and the built-in catalogue. The catalogue backup uses the candidate backup name with `.roles.json` appended; a catalogue-only backup is used when no candidate file exists.
* If saving a command fails, `LogicManager` restores the previous model data, filter, and expanded view so that memory and saved data remain consistent after successful rollback.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Command history navigation

The command history feature lets a user walk through commands they submitted earlier in the current session, using the `Up` and `Down` arrow keys. It is implemented as a single collaborator (`CommandHistory`) owned by `CommandBox`.

#### How it fits in

`CommandBox` already owns the JavaFX `TextField` and runs user input through `CommandExecutor` on Enter. The new field reuses that path:

1. On Enter, `CommandBox#handleCommandEntered` records the raw text with `CommandHistory#add` **before** dispatching to the executor. Rejected commands are recorded too so the user can press Up and fix a typo. The text field is cleared on both success and failure so the next Up press shows the command from history instead of appearing to do nothing.
2. A `KEY_PRESSED` event filter on `commandTextField` catches `UP` and `DOWN` presses, calls `history.previous()` or `history.next()`, and swaps the text field's contents with the result. The event is consumed so the caret does not move within the text field.
3. When the user presses `Up` while composing a fresh command, `CommandBox` captures the current text as a `draft` before replacing the field with a past entry. Pressing `Down` back past the newest entry restores that draft.

#### `CommandHistory` design

`CommandHistory` is a small helper class in the `ui` package. It stores submitted commands in insertion order and tracks a `cursor` that sits at `entries.size()` when the user is composing a fresh command (the "past-the-end" position).

* `add(String)` appends and resets the cursor to the past-the-end position. Blank commands are ignored, and a command identical to the most recent entry is also skipped so repeated submissions do not take up extra slots.
* `previous()` moves the cursor one step back and returns the command there, or `Optional.empty()` when already at the oldest entry.
* `next()` moves the cursor forward. On reaching the past-the-end position it returns an empty string so the UI knows to clear or restore the field.
* Entries are capped at `MAX_ENTRIES` (100) to bound memory for long-running sessions. The oldest entry is dropped when the cap is reached.

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

### Atomic CSV import

Import is a batch operation over the complete in-memory address book, not the displayed list. `AddressBookParser` rejects arguments through `ImportCommandParser`. `LogicManager` routes `ImportCommand` to `ImportOperation`, which stages and validates the batch before publication. This coordinator is needed because ordinary add/edit saving does not restore model state on save failure.

`StorageManager.readImport` delegates to `CsvImportReader`. For `/app/data/HRvest.json`, it reads only `/app/import.csv`. The reader rejects links and non-regular files, acquires a non-blocking shared lock, and buffers at most 10 MiB while comparing identity, size, and modification time around reading. The lock and channel close on every outcome. Parsing happens on the buffered copy after release, so later edits cannot affect the parsed data. Locks may be advisory and metadata checks cannot detect every concurrent change; users must not edit the input during import or externally modify saved JSON. There is no new coordination protocol for external writers to saved JSON.

`StrictCsvParser` accepts fully quoted, seven-column CSV with CRLF record endings. `CandidateCsvParser` requires strict UTF-8 without a BOM and exact headers, validates fields through existing command/model rules, and uses Jackson's existing streaming parser only for a flat tags array of strings. No dependencies are added. Import intentionally has its own encoding and path contract, independent of export. Empty bytes are an explicit header exception; both empty and header-only inputs run the complete save pipeline. The [User Guide](UserGuide.md#importing-candidates-import) lists the template and hard limits: 10 MiB, 10,000 candidates, 65,536 code points per decoded field, 100 tag entries, 256 code points per tag, and the existing 500-code-point note cap. Limits apply before unbounded accumulation; tag count is checked before deduplication. Tags and statuses retain existing parsing semantics.

Before mutation, `ImportOperation` copies the address book and retains the filter predicate and expanded candidate. Candidate value objects are immutable, so the independent address-book collection can share those values safely. A case-insensitive email index detects conflicts both within the batch and against all existing candidates. Only after validation succeeds is the combined collection applied, then persisted through normal JSON saving with its existing atomic-replacement protections. Success clears the filter and exits expanded view. Existing order is preserved and imported records are appended in CSV order.

If reading or validation fails, live state remains untouched. If model application or saving fails, the coordinator restores the in-memory snapshot and view before returning the error; pre-existing unsaved changes are preserved. It never reloads JSON for rollback or modifies the input. Save errors use the normal error format. A rollback failure is reported separately without claiming restoration. This is ordinary exception recovery, not a promise of rollback following JVM termination or power loss; a successfully persisted import remains saved after a later crash.

### CSV export

The `export` command creates a CSV copy of all candidate records, regardless of the current search or filter. It leaves the application's data and current view unchanged.

`ExportCommand` requests an export through `CommandResult`. `LogicManager` passes the complete address book to `StorageManager`, which delegates CSV formatting and writing to `CsvExportWriter`. This keeps file operations in the storage component rather than the model.

You can refer to the diagram below as a reference.

<puml src="diagrams/ExportFeatureClassDiagram.puml" alt="ExportFeatureClassDiagram" />

The writer exports one row per candidate, with a header and all candidate fields, to `export.csv` beside the JSON data file. The data file path is fixed at `data/HRvest.json`, so the export is always `data/export.csv`. The file starts with a UTF-8 byte order mark so that spreadsheet programs such as Excel detect the encoding; without it, Excel reads the file as ANSI and garbles non-ASCII text. Every field is quoted, embedded quotes are doubled. Literal values, including embedded line breaks, are preserved. Status uses its displayed label; an absent note is empty. Tags occupy one CSV field containing a JSON array, such as `["Java","backend"]`, with CSV escaping applied. Tag names are sorted in case-sensitive Java string order, preserving casing; no tags gives `[]`.

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
| `* * *`  | new user          | add a new candidate with contact details              | keep a record of every candidate I am considering                   |
| `* * *`  | new user          | search for a candidate by name                        | view their details without scrolling through the whole list         |
| `* * *`  | new user          | edit a candidate's contact details                    | correct mistakes or outdated details without re-adding them         |
| `* * *`  | user              | delete a candidate from the list                      | keep my list focused on candidates still under consideration        |
| `* * *`  | user              | list every candidate currently in the app             | see every candidate in my pipeline in one view                      |
| `* * *`  | user              | filter the list by candidate status                   | focus on one hiring stage, for example only those interviewing      |
| `* * *`  | user              | update a candidate's status after an interview        | keep the pipeline accurate as candidates move through stages        |
| `* * *`  | user              | view a candidate's full details including all notes   | prepare for the next conversation with them                         |
| `* * *`  | user              | see a confirmation when an action succeeds            | confirm that the command did what I intended                        |

#### Nice-to-have (`* *`)

| Priority | As a …           | I want to …                                                      | So that I can…                                                       |
|----------|-------------------|-------------------------------------------------------------------|-----------------------------------------------------------------------|
| `* *`    | familiar user     | find a candidate by a partial keyword of their name               | find a candidate whose full name I do not remember                    |
| `* *`    | familiar user     | append a note to a candidate without overwriting existing notes   | keep a complete history of my interactions with that candidate        |
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

* 1a. A required detail is missing, or a given detail is invalid.

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

* 2c. A new detail is invalid.

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
1. HRvest deletes the candidate, together with the candidate's notes, and shows the deleted candidate.

   Use case ends.

**Extensions**

* 1a. The list is empty.

  Use case ends.

* 2a. The specified candidate is not in the list.

    * 2a1. HRvest shows an error message.

      Use case ends.

**Use case: UC06 - Find a candidate by name**

**MSS**

1. User requests to find candidates whose names contain a keyword.
1. HRvest shows a list of matching candidates.

   Use case ends.

**Extensions**

* 1a. No keyword is given.

    * 1a1. HRvest shows an error message.

      Use case ends.

* 2a. No candidate matches the keyword.

    * 2a1. HRvest informs the user that no candidates match.

      Use case ends.

**Use case: UC07 - Filter candidates by status**

**MSS**

1. User requests to filter candidates by candidate status.
1. HRvest shows candidates with the specified status.

   Use case ends.

**Extensions**

* 1a. The specified status is not a valid candidate status.

    * 1a1. HRvest shows an error message listing the valid candidate statuses.

      Use case ends.

* 2a. No candidates match the specified status.

    * 2a1. HRvest informs the user that no candidates match.

      Use case ends.

**Use case: UC08 - View a candidate's full details**

**MSS**

1. User requests to view the full details of a candidate profile.
1. HRvest shows the candidate's full details, including all notes.

   Use case ends.

**Extensions**

* 1a. The specified candidate is not found.

    * 1a1. HRvest shows an error message.

      Use case ends.

**Use case: UC09 - Add a note to a candidate**

**MSS**

1. User requests to add a note to a specific candidate.
1. HRvest adds the note without overwriting earlier notes and shows the updated candidate profile.

   Use case ends.

**Extensions**

* 1a. The specified candidate is not found.

    * 1a1. HRvest shows an error message.

      Use case ends.

* 1b. The note is empty.

    * 1b1. HRvest shows an error message.

      Use case ends.

**Use case: UC11 - Import candidates**

**MSS**

1. User prepares `import.csv` in the required location and enters `import`.
1. HRvest reads and validates the complete batch, checking email conflicts against all candidates and within the file.
1. HRvest appends and saves the candidates, then shows the default list view with all candidates and the imported count.

   Use case ends.

**Extensions**

* 2a. Input is missing, unreadable, malformed, oversized, or contains invalid/duplicate candidates.

  * 2a1. HRvest explains the failure and leaves the pre-import data and view unchanged.

    Use case ends.

* 3a. Applying or saving fails.

  * 3a1. HRvest restores the pre-import in-memory data and view, retaining unsaved changes, and reports the error. A rollback failure is reported separately.

    Use case ends.

**Use case: UC10 - Export candidate data**

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
* **Role catalogue**: The shared collection of roles available in HRvest, including unused roles and the built-in `No role`.
* **Application**: A candidate's consideration for a particular open role, including the candidate status and relevant hiring details.
* **Candidate profile**: The candidate's contact information and hiring details shown together in HRvest.
* **Follow-up**: An action the hiring manager needs to take for a candidate after a prior interaction.
* **Candidate details**: A candidate's name, phone number, email address, address, status and tags.
* **Candidate status**: One of Applied, Shortlisted, Interviewing, Offered, Accepted, Rejected, or Withdrawn.
* **Duplicate candidate**: A candidate with the same email address as another candidate in HRvest, compared case-insensitively after trimming command input. No other fields, including name, status, tags or notes, affect duplicate detection.
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

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Rejecting note input in `add`

1. Run `add n/A p/123 e/a@x.com a/Blk 1 no/great candidate`. Expected: The error explains that `add` does not support `no/` and directs you to `note`. No candidate is added and existing data stays unchanged.
1. Run `add n/A p/123 e/a@x.com a/Blk 1 no/` (empty note). Expected: The same error, with no candidate or file changes.
1. Repeat with `no/` before the required fields, or after a tag/status field. Expected: The same error, with no candidate or file changes.
1. Run `add n/A p/123 e/a@x.com a/Blk 1 yes/no/feedback` with an unused email. Expected: The candidate is added with the literal address `Blk 1 yes/no/feedback`.

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

### Managing roles

Use a separate test folder and keep a matching backup of candidate and catalogue files.

1. Run `create_role n/Documentation Tester`, then `list_role`. Expected: The unused role appears once. Restart and list again; it remains available.
1. Run `create_role n/documentation tester`, `edit_role r/No role n/Other`, and `list_role extra`. Expected: Each fails without changing candidates or the catalogue.
1. Run `edit_role r/Documentation Tester n/Senior Documentation Tester`. Expected: The renamed entry appears in `list_role`; its ID in `roles.json` is unchanged.
1. To check global assignment updates, close HRvest. In a test copy, assign the new role ID to two candidates' `roles` arrays; give one candidate another valid role too. Restart, filter so one candidate is hidden, and rename the role again. Close HRvest and inspect both files. Expected: The catalogue name changes, both candidates retain the same role ID, and their other fields and assignments remain unchanged.
1. Restart and delete the renamed role with `delete_role r/NAME`, substituting its current name. Expected: The catalogue entry disappears; a candidate losing their last role receives `no-role`, while a candidate with another role retains it. Close and inspect the saved assignments, then restart to confirm they load.
1. Run `delete_role r/No role`. Expected: An error leaves the built-in role intact. Run `clear confirm`, then `list_role`. Expected: Candidates are removed while remaining catalogue entries survive. Restore both test files together after testing.

### Saving data

1. Checking null entries in a test copy of the data file

   1. Close HRvest and back up `data/HRvest.json` and `data/roles.json` together (if the catalogue file exists). Insert `null` alongside a valid candidate in the `persons` array, then launch HRvest. Expected: The app handles a data loading failure and opens a window, rather than crashing with a `NullPointerException`. Close HRvest and restore the valid backup before running further commands.
   1. Restore the test copy, insert `null` in one candidate's `tags` array, and repeat. Expected: The same controlled data loading failure. Restore the valid backup after testing.

1. Dealing with missing/corrupted data files

   1. Prerequisites: Use a test folder and back up its candidate and catalogue files together. Close HRvest, make the test data file invalid (for example, change one candidate's email to `invalid`), and launch. Expected: A warning gives the full path of a new `HRvest-<timestamp>-<random>-corrupt.json` beside the data file, where the timestamp matches the launch time; its bytes match the invalid original. The app shows an empty list, already saved to `data/HRvest.json`.
   1. Run `list`, `find Alice`, and `help`. Expected: The backup remains unchanged. Restart HRvest. Expected: The list stays empty and no additional backup or recovery warning appears.
   1. Close HRvest, copy the backup, repair the invalid email in the copy, and replace the candidate file with that copy. Restore the companion `.roles.json` backup to `data/roles.json` if present. Expected: All candidates return after relaunch; the backup remains unchanged.
   1. Repeat with an invalid catalogue entry and with an unknown candidate role ID. Expected: Both existing originals are backed up before an empty candidate list and built-in catalogue are saved. Restore both files from the matching backup pair before continuing.
   1. In a separate test folder, keep only an invalid `data/roles.json` and launch. Expected: The warning points to a catalogue-only `.roles.json` backup. Close HRvest and restore its repaired copy to `data/roles.json`.
   1. Repeat with malformed JSON and duplicate emails that differ only in case. Expected: The same recovery process, with a separate backup for each invalid file.
   1. Close HRvest and edit a valid test file to contain `"note": ""` and `"status": " Applied "`. Expected: All candidates load, the note is absent, the status is Applied, and no recovery backup is created.
   1. Make the test data file invalid and deny writing in its folder. Launch from a terminal with `java -jar HRvest.jar`. Expected: Startup stops before a window opens, an actionable terminal error appears, and the original file is unchanged. Restore permissions after testing.
   1. Move the data file out of the test folder and launch. Expected: Sample candidates appear without a recovery warning or backup.

1. _{ more test cases … }_
