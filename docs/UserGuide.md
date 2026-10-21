---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# HRvest User Guide

HRvest is a **desktop address book that helps hiring managers at startups track candidates through the hiring pipeline with less administrative work**. While it has a GUI (Graphical User Interface), most interactions are short typed commands in a CLI (Command Line Interface), so if you type fast, you can work faster than in a mouse-driven applicant tracking system.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103-F10-1/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for HRvest.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar HRvest.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all candidates.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a candidate named `John Doe` to HRvest.

   * `delete 3` : Deletes the 3rd candidate shown in the current list.

   * `clear` : Deletes all candidates.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for `help`, `list`, `exit`, `clear`, and `export` are ignored.<br>
  For example, `help 123` is interpreted as `help`. The role catalogue command `list_role` is an exception: it rejects all arguments.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a candidate: `add`

Adds a candidate to HRvest.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [s/STATUS] [t/TAG]... `

**Parameters:**

| Parameter | Required? | Accepted values and behavior |
|---|---|---|
| `n/NAME` | Yes | A non-blank name containing only letters, digits, and spaces, e.g. `n/John Doe`. Leading and trailing spaces are removed; internal spaces and letter case are preserved. Punctuation such as apostrophes and hyphens is not accepted. |
| `p/PHONE_NUMBER` | Yes | At least three digits, e.g. `p/98765432`. Spaces within the number, `+`, hyphens, and parentheses are not accepted. Leading zeroes are preserved. |
| `e/EMAIL` | Yes | An address in `local-part@domain` format, e.g. `e/john.doe@example.com`. The local part allows letters, digits, and `+`, `_`, `.`, or `-` between alphanumeric groups; it must start and end with a letter or digit. Domain labels allow letters, digits, and internal hyphens; the final label must contain at least two characters. Spaces are not accepted. |
| `a/ADDRESS` | Yes | Any non-blank, single-line address, e.g. `a/John street, block 123, #01-01`. Spaces and punctuation are allowed. |
| `s/STATUS` | No | One of `Applied`, `Shortlisted`, `Interviewing`, `Offered`, `Accepted`, `Rejected`, or `Withdrawn`. Defaults to `Applied` if omitted. Input ignores letter case, so `s/interviewing` is saved and displayed as `Interviewing`. A bare `s/` is invalid rather than using the default. |
| `t/TAG` | No | A non-blank label containing only letters and digits, e.g. `t/backend`. Spaces and punctuation are not accepted. Repeat the prefix for multiple tags: `t/backend t/Java`. Identical tags are removed, but tags are case-sensitive: `t/Java` and `t/java` are distinct. Omit `t/` for no tags; a bare `t/` is invalid when adding. |

**Input and results:**

* Supply each of `n/`, `p/`, `e/`, `a/`, and `s/` at most once. Only `t/` can be repeated. Repeated single-valued prefixes are rejected, even if their values are identical.
* All four required prefixes must have valid values. Missing required prefixes or text before the first prefix produces an invalid-command-format message.
* Email alone identifies duplicates: surrounding whitespace is trimmed and the whole email is compared case-insensitively. For example, `Alice@Example.com` and `alice@example.com` belong to the same candidate. Adding a duplicate is rejected with `This person already exists in the address book.` Names, phones, addresses, statuses, tags, and notes do not affect duplicate detection. Candidates with the same name but different emails are allowed. Email capitalisation is preserved for display and storage.
* Use only the documented prefixes. Unknown prefixes such as `x/` are treated as part of the preceding value, not as separate fields. A recognised prefix preceded by whitespace also starts a new field, even if you intended it to be part of an address.
* `no/` is reserved for the `note` command and is rejected anywhere in `add`, including with empty text or after a tab. The error is `The add command does not support no/. Add the candidate first, then use note INDEX no/NOTE_TEXT.` Add the candidate without `no/`, then use their displayed index with `note` to record a note. Text such as `yes/no/feedback` inside an address word remains literal address text.

Examples:

* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe p/1234567 e/betsy@example.com a/Newgate Prison s/Interviewing t/backend`

### Listing all candidates: `list`

Shows a list of all persons in HRvest.

Format: `list`

### Editing a candidate: `edit`

The success message shows the candidate's updated details, including their current status.

Edits an existing person in HRvest.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [s/STATUS] [t/TAG]...`

* Edits the candidate at the specified `INDEX`. The index refers to the index number shown in the displayed candidate list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values. The current filter is kept; candidates that no longer match disappear from the displayed list.
* An email matching another candidate's email (ignoring case and surrounding whitespace) is rejected with `This person already exists in the address book.`, even if that candidate is hidden by a filter. The original candidate remains unchanged. Keeping your candidate's email, changing only its capitalisation, or editing other fields does not cause a self-duplicate error.
* When editing tags, all of the candidate's existing tags are removed; adding tags is not cumulative.
* To remove all of a candidate's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st candidate to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd candidate to be `Betsy Crower` and clears all existing tags.
*  `edit 3 s/Interviewing` Updates the status of the 3rd candidate to `Interviewing`.

After an ordinary `edit`, `note`, or `status` update, the current name or status filter remains active. Displayed indexes may change when a candidate stops matching. Expanding a candidate replaces that filter with a selection of the candidate: an update keeps them expanded at index `1`, even if their new name or status would not match the earlier filter. Check the displayed list before the next index command; use `list` to show all candidates.

### Adding, replacing, or clearing a candidate's note: `note`

Adds a note to the specified candidate, such as interview feedback or follow-up context.

Format: `note INDEX no/[NOTE_TEXT]`

* Adds the note to the candidate at the specified `INDEX`. The index refers to the index number shown in the displayed candidate list. The index **must be a positive integer** 1, 2, 3, ...
* Each candidate has one note. If the candidate already has a note, it is replaced by the new note.
* To clear the note, enter `note INDEX no/` with no text after `no/`. Whitespace-only text also clears it. The message is `Cleared note for NAME`. Clearing a candidate who already has no note succeeds and keeps that candidate unchanged.
* Non-empty `NOTE_TEXT` can contain at most 500 characters. Leading and trailing spaces are removed; spacing, capitalisation, and punctuation within the note are kept.
* Supply `no/` only once. Other prefixes, such as `n/`, are treated as part of the note.
* After the note is updated or cleared, the current filter is kept. When adding or replacing a note, the message is `Updated note for NAME: NOTE_TEXT`.
* Editing a candidate or updating their status keeps their note. Use `expand INDEX` to view the full note.

Examples:

* `note 2 no/Strong on system design, weak on SQL`
* `note 2 no/` clears the 2nd displayed candidate's note. The note icon disappears; expanding the candidate shows `No note recorded.`
* `find Betsy` followed by `note 1 no/Follow up next week` adds a note to the 1st candidate in the results of the `find` command.

<box type="warning" seamless>

**Caution:**
Replacing or removing a note deletes the previous note permanently. To keep earlier information, include it in the new note.
</box>

### Updating a candidate's recruitment status: `status`

Updates the candidate at the given index in the currently displayed list.

Format: `status INDEX s/STATUS`

* `INDEX` is a positive, 1-based index in the displayed list.
* Valid statuses are `Applied`, `Shortlisted`, `Interviewing`, `Offered`, `Accepted`, `Rejected`, and `Withdrawn`, regardless of letter case.
* Candidates created with `add` start with `Applied` unless an initial `s/STATUS` is supplied. Status is saved immediately, shown on the candidate card, and does not affect duplicate checking. Older records without a saved status load as `Applied`; explicitly saved statuses are preserved.
* After an update, the current filter is kept. Candidates that no longer match a status filter disappear from the displayed list. The message is `Updated status of NAME: OLD_STATUS -> NEW_STATUS`.
* A missing `s/` or non-integer index gives `Invalid command format!`; an index outside the displayed list gives `The candidate index provided is invalid.`
* An unknown status gives `Status must be one of: Applied, Shortlisted, Interviewing, Offered, Accepted, Rejected, Withdrawn`; repeated `s/` gives `Multiple values specified for the following single-valued field(s): s/`.

Examples:

* `status 3 s/Interviewing`
* `status 1 s/Accepted`

### Locating candidates by name: `find`

Finds candidates whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Candidates matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Filtering candidates by status: `filter`

Filters candidates by their hiring status.

Format: `filter s/STATUS`

* Displays all candidates with the specified status.
* Valid statuses: `Applied`, `Shortlisted`, `Interviewing`, `Offered`, `Accepted`, `Rejected`, `Withdrawn`.
* The status is case-insensitive.

Examples:

* `filter s/Interviewing` displays all candidates currently in the interviewing stage.
* `filter s/Offered` displays all candidates who have been made offers.

### Deleting a candidate: `delete`

The success message shows the deleted candidate's details, including their status.

Deletes the specified person from HRvest.

Format: `delete INDEX`

* Deletes the candidate at the specified `INDEX`.
* The index refers to the index number shown in the displayed candidate list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd person in HRvest.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Viewing a candidate's full record: `expand`

Shows the specified candidate's full record, including their full note.

Format: `expand INDEX`

* Shows the candidate at the specified `INDEX`.
* The index refers to the index number shown in the displayed candidate list.
* The index **must be a positive integer** 1, 2, 3, ... within the displayed list.
* The list shows only the selected candidate at index `1`, with their full note. If the candidate has no note, it shows `No note recorded.` Run `list` to show all candidates again.

Examples:

* `list` followed by `expand 1` shows the 1st candidate in HRvest.
* `filter s/Interviewing` followed by `expand 2` shows the 2nd candidate in the results of the `filter` command.

### Managing the role catalogue

Manage the roles available to candidates in the shared [role catalogue](#glossary).

* Each candidate has one or more roles. New candidates receive `No role`; `add` and `edit` do not assign other roles in this version.
* `No role` cannot be renamed or deleted and may coexist with other roles.
* Unused roles remain available, including after `clear` and restarts.
* Role command words and prefixes are case-insensitive. Put whitespace before each prefix and supply each required prefix exactly once.
* Changes save automatically. Invalid commands report an error without changing candidates or roles.

**Role names:**

* Names must not be blank or contain control characters such as tabs or newlines. Punctuation and Unicode characters are accepted.
* Matching ignores surrounding whitespace and letter case: ` Software Engineer ` matches `software engineer`. Capitalisation is preserved for display.
* Internal spaces matter: `Software  Engineer` (two spaces) is distinct from `Software Engineer`.
* A space followed by `n/`, `r/`, `p/`, `e/`, `a/`, `t/`, `no/`, or `s/` is reserved command syntax, ignoring case. It cannot appear within a role name; quotes do not escape it.

#### Creating an unused role: `create_role`

Creates a role without assigning it to a candidate.

Format: `create_role n/NAME`

* The name must not match an existing role, including `No role`.

Example: `create_role n/Software Engineer`

#### Renaming a role globally: `edit_role`

Renames a role for every assigned candidate, including those outside the displayed list.

Format: `edit_role r/EXISTING_NAME n/NEW_NAME`

* The role must exist and must not be `No role`.
* The new name must not match another role. Changing only capitalisation is allowed.
* Other role assignments stay unchanged.
* Exits expanded view and shows all candidates; other candidate filters remain active.

Example: `edit_role r/Software Engineer n/Senior Software Engineer`

#### Deleting a role globally: `delete_role`

Removes a role from the catalogue and all assigned candidates.

Format: `delete_role r/NAME`

* The role must exist and must not be `No role`.
* Candidates keep their other roles. Those losing their last role receive `No role`.
* Exits expanded view and shows all candidates; other candidate filters remain active.

Example: `delete_role r/Senior Software Engineer`

#### Listing the complete catalogue: `list_role`

Shows all role names in the command result, including unused roles and `No role`.

Format: `list_role`

* Names are sorted alphabetically, ignoring case.
* Accepts no arguments and keeps the candidate filter unchanged.

### Clearing all entries: `clear`

Clears all candidates from HRvest while preserving the role catalogue, including unused roles.

Format: `clear`

### Exporting candidates: `export`

Exports every candidate to `export.csv` inside the application's data folder, alongside the configured JSON data file (for example, `./data/data.json` gives `./data/export.csv`). Candidates are exported in address-book order, regardless of the current search or filter. Candidate data, the current filter, and expanded view remain unchanged.

Format: `export`

* Use the lowercase command; surrounding whitespace is accepted.
* The destination is always `export.csv` in the same directory as the application's configured JSON data file. You are not able to specify your own export path.
* The CSV has the columns `name,phone,email,address,status,tags,note`, in that order.
* For example, the tags `Java` and `backend` appear as `["Java","backend"]` in the tags column when the CSV is opened in a spreadsheet.
* Success displays `Exported successfully to PATH.`, with the absolute destination, only after writing and closing the file succeeds.
* If the address book is empty, `export` succeeds and writes only the header row, with no candidate rows.

**Overwrite warning:** Each successful `export` replaces any existing `export.csv` without asking for confirmation or keeping a backup. This is expected behaviour. It is your responsibility to keep a copy of the exported data: after exporting, copy `export.csv` to a separate location or rename it before running `export` again. Export writes directly to the destination: a failed or interrupted overwrite may destroy the previous export and leave an empty or incomplete CSV.

**For more technical details, you can refer to the** [Developer Guide](./DeveloperGuide.md)

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Candidate note indicator

A pinned sticky note icon beside a candidate's name indicates that a note exists. Hovering over it shows `Note available`. Collapsed cards keep the note text hidden; use `expand INDEX` to view the candidate's full note. Candidates without notes have no icon and reserve no icon space.

### Saving the data

HRvest automatically saves data after every successful command. Use [`export`](#exporting-candidates-export) to create a CSV copy.

* Keep both `data/HRvest.json` (candidates) and `data/roles.json` (roles) when backing up or restoring data.
* Run only one HRvest instance at a time. Your account needs permission to access the files and write to their folder.
* If saving fails, follow the error message before continuing. Do not edit or delete `roles-transaction.json`; restart HRvest to recover an interrupted save.

### Editing the data file

Advanced users may edit the JSON files. For a custom candidate data path, use that file and the `roles.json` beside it.

**Before editing:**

1. Complete any interrupted-save recovery, then close HRvest.
1. Back up both data files together.
1. Edit the files, then restart HRvest to check your changes.

**When editing records:**

* Keep candidate emails unique, ignoring case.
* Each candidate needs at least one valid role ID from `roles.json`, without duplicates. Use `no-role` for `No role`, rather than its display name.
* Preserve the JSON structure and use the same field values allowed by commands. To clear a note, set `note` to an empty string.

**If data cannot be loaded:**

<box type="warning" seamless>

Invalid edits can cause HRvest to start with an empty candidate list and the default role catalogue. HRvest backs up the existing files first and shows their location. If backup or recovery fails, follow the reported instructions and keep all original and recovery files.

1. Close HRvest and copy the backups shown in the warning.
1. Repair the copies and restore the corresponding candidate and role files together.
1. Restart HRvest and verify your data before deleting any backups.

HRvest does not repair or merge records automatically. See the [Developer Guide](DeveloperGuide.md#storage-component) for storage and recovery details.

</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install HRvest on the new computer, then:

1. If `roles-transaction.json` exists on the old computer, restart HRvest there to complete recovery first. If recovery fails, follow the reported instructions before transferring.
1. Close HRvest on both computers.
1. Copy `data/HRvest.json` and `data/roles.json` from the old HRvest folder to the new one, replacing both files together. For a custom candidate path, copy that file and its neighbouring `roles.json` instead.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Glossary

* **Role catalogue**: The shared collection of roles available in HRvest, including unused roles and the built-in `No role`.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [s/STATUS] [t/TAG]... ` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Create role** | `create_role n/NAME`<br> e.g., `create_role n/Software Engineer`
**Edit role** | `edit_role r/EXISTING_NAME n/NEW_NAME`<br> e.g., `edit_role r/Software Engineer n/Senior Engineer`
**Delete role** | `delete_role r/NAME`<br> e.g., `delete_role r/Software Engineer`
**List roles** | `list_role`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [s/STATUS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Export** | `export`
**Filter** | `filter s/STATUS`<br> e.g., `filter s/Interviewing`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list`
**Note**   | `note INDEX no/[NOTE_TEXT]`<br> e.g., `note 2 no/Passed round 2, schedule final interview` or `note 2 no/` to clear
**Status** | `status INDEX s/STATUS`<br> e.g., `status 3 s/Interviewing`
**Help**   | `help`
