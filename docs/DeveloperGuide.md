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

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

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
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonAddressBookStorage` writes UTF-8 JSON to a temporary file in the destination directory, then atomically replaces the saved file after writing completes. Symbolic-link destinations are resolved to their existing targets before creating the temporary file and replacing the target, preserving the links. Dangling links cause an `IOException` without changing the links or creating files. On filesystems supporting `AclFileAttributeView`, the existing destination's ACL is applied to the temporary file before writing JSON. POSIX permissions are also copied before writing on filesystems supporting `PosixFileAttributeView`. New destinations retain default ACLs and POSIX permissions. ACL or POSIX permission errors, including `AccessDeniedException`, abort the save and trigger temporary-file cleanup. The existing file's access restrictions are not replaced with default permissions when access is denied; `LogicManager` reports the permission error. Failed writes leave the previous saved file intact. Filesystems that cannot perform atomic replacement reject the save instead of attempting a potentially partial overwrite. A filesystem root is rejected as a destination with an `IOException`.

Temporary files use unique names of the form `HRvest-<random>.tmp`, so a later save does not reuse or overwrite an existing temporary file. The `finally` block attempts to remove the current save's temporary file when execution unwinds normally, including after an `IOException` or an unchecked exception. A forced JVM termination or power loss can prevent this cleanup and leave an orphaned temporary file. Subsequent saves ignore these files. After closing all HRvest instances, leftover `HRvest-*.tmp` files can be removed manually; keep the configured JSON data file and any symbolic-link target.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Candidate note indicator

`PersonCard` shows a pinned sticky note icon beside the candidate's name when `Person#hasNote()` is true. The icon has the accessible description `Candidate has a note` and tooltip `Note available`. It is invisible and unmanaged when no note exists, so it leaves no extra space. Its minimum width preserves the icon when a long name is truncated. The card does not display note text.

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

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

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
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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

**Value proposition**: HRVest helps a startup hiring manager fill full-time roles faster by tracking candidates and hiring progress with keyboard-first commands, reducing spreadsheet work and making follow-ups easier to spot.


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
1. HRvest adds the candidate with the status Shortlisted and shows the added candidate.

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

*{More to be added}*

### Non-Functional Requirements

1. HRVest should run on Windows, Linux, and macOS with Java `25` installed, without requiring an installer or other application-specific software beyond a single JAR file.
1. HRVest should be usable by a single user without requiring a shared account or a team-operated remote server for ordinary candidate management.
1. Candidate records should be stored locally in a human-editable text file, without a database management system.
1. Core candidate-management commands should work without an internet connection.
1. The primary workflows for adding, finding, viewing, and updating candidate records should be completable using typed commands without mouse interaction.
1. Listing, searching, filtering, and updating 50 candidate records across three open roles should each complete within two seconds.
1. The GUI should work well at 1920×1080 resolution with 100% or 125% display scaling, and remain usable at 1280×720 resolution with 150% display scaling.
1. A successful change to a candidate record should persist after a normal restart, while an invalid command should leave stored candidate records unchanged.

### Glossary

* **Candidate**: A person being considered for an open role.
* **Open role**: A position for which the startup is currently recruiting.
* **Application**: A candidate's consideration for a particular open role, including the candidate status and relevant hiring details.
* **Candidate profile**: The candidate's contact information and hiring details shown together in HRVest.
* **Follow-up**: An action the hiring manager needs to take for a candidate after a prior interaction.
* **Candidate details**: A candidate's name, phone number, email address, role, address and tags.
* **Candidate status**: One of Shortlisted, Interviewing, Offered or Rejected.
* **Duplicate candidate**: A candidate with the same email address and role as another candidate in HRvest.
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

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Candidate note indicator

1. Prerequisites: Close HRvest and back up the saved JSON file. In a test copy, give one candidate a non-blank `note` string, leave another candidate without a note, and give the candidate with a note a long name.
1. Launch HRvest using the test data file. Expected: Only the candidate with a note has a sticky note icon beside the name; no note text appears on either card.
1. Hover over the icon. Expected: The tooltip reads `Note available`.
1. Narrow the window until the long name is truncated. Expected: The sticky note icon remains visible, and the candidate's status still appears below the contact details.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
