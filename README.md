[![CI Status](https://github.com/AY2627S1-CS2103-F10-1/tp/workflows/Java%20CI/badge.svg)](https://github.com/AY2627S1-CS2103-F10-1/tp/actions)

![Ui](docs/images/Ui.png)

# HRvest

HRvest is a desktop address book for hiring managers at startups who track candidates through the hiring pipeline. Every action is a short typed command, so an experienced user can work faster than they would in a mouse-driven applicant tracking system.

## What HRvest does

* Stores each candidate's contact details, applied role, current pipeline status, notes, and tags in HRvest.
* Supports commands to add, edit, delete, list, find, filter by status, add notes, update status, view a candidate, clear all records, exit, and open help.
* Saves data to a JSON file next to the jar. No cloud account, database, or network connection.

## Target user

A hiring manager at a tech startup who manages 10 to 50 candidates at a time for 1 to 3 open roles, prefers typing over clicking, and finds most enterprise applicant tracking systems too slow for day-to-day pipeline updates.

## Getting started

See [Setting up and getting started](docs/SettingUp.md) for prerequisites and local build instructions.

For end-user command reference and feature details, see the [HRvest User Guide](docs/UserGuide.md). For architecture and extension notes, see the [Developer Guide](docs/DeveloperGuide.md).

## Acknowledgements

HRvest is based on the [AddressBook-Level3 project](https://github.com/se-edu/addressbook-level3) by the [SE-EDU initiative](https://se-education.org). The AB3 codebase gives us the model, logic, and storage layers that HRvest extends.
