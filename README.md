[![CI Status](https://github.com/AY2627S1-CS2103-F10-1/tp/workflows/Java%20CI/badge.svg)](https://github.com/AY2627S1-CS2103-F10-1/tp/actions)

![Ui](docs/images/Ui.png)

# HRvest

**HRvest** is a desktop application that helps hiring managers at fast-paced startups keep track of job candidates through the hiring pipeline. It is optimised for keyboard-first users: every action is available as a short, scriptable command, so an experienced user can move faster than a mouse-driven applicant tracking system.

## What HRvest does

* Stores candidate contact details, applied role, current pipeline status, notes, and tags in a single local address book.
* Supports commands to add, edit, delete, list, find, filter by status, add notes, update status, view a candidate's details, clear all records, exit, and open help.
* Persists data locally in a human-readable JSON file next to the jar; no cloud account, no database, no internet connection required.

## Target user

A hiring manager at a tech startup who manages 10-50 full-time applicants at a time for 1-3 open roles, prefers typing over clicking, and finds enterprise applicant tracking systems too slow and click-heavy for day-to-day pipeline updates.

## Getting started

See [Setting up and getting started](docs/SettingUp.md) for prerequisites and local build instructions.

For end-user command reference and feature details, see the [HRvest User Guide](docs/UserGuide.md). For architecture and extension notes, see the [Developer Guide](docs/DeveloperGuide.md).

## Acknowledgements

This project is based on the [AddressBook-Level3 project](https://github.com/se-edu/addressbook-level3) created by the [SE-EDU initiative](https://se-education.org). The upstream codebase provides the model, logic, and storage scaffolding on which HRvest is built.
