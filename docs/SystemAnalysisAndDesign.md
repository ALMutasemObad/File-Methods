# System Analysis and Design

## 1. Introduction
The File Menu Application is a console-based Java program that guides users through common file-system operations by presenting an interactive menu. This document captures the system analysis and design artifacts that complement the existing Blackboard submission documentation. It explains how the application is structured, the primary workflows it supports, and the design rationale behind its implementation.

## 2. Goals and Scope
- Deliver a lightweight learning tool that demonstrates file input/output concepts without requiring a graphical user interface.
- Provide a single executable (`FileMenuApp`) that can create, delete, rename, inspect, and list files or directories on the local machine.
- Offer an additional utility (`TextToPng`) that renders text files to PNG images for documentation or grading purposes.
- Emphasize safe operations that validate user input before altering the file system.

Out of scope: remote file-system access, concurrency, background monitoring, and persistence beyond the user's file system.

## 3. Stakeholders and Users
- **Students** use the tool to explore file-system APIs and practice command-line interactions.
- **Instructors** rely on predictable prompts and outputs to evaluate assignments quickly.
- **System Maintainers** need a code base that is easy to audit for platform compatibility and file-system safety.

## 4. Functional Requirements
1. Present a numbered menu and loop until the user exits.
2. Allow creation of new files, optionally scaffolding parent directories after confirmation.
3. Delete files or directories, including recursive deletion for non-empty directories with user confirmation.
4. Rename files or directories when the target name does not already exist.
5. Display metadata including path, type, size, creation, modification, and access timestamps.
6. List directory contents with simple type labeling.
7. Show last modified timestamps and file sizes independently for quick lookup.
8. Summarize read/write/execute permissions with optional POSIX permission details when supported.
9. Convert plain-text files to PNG images using a monospace font.

## 5. Use Cases
- **UC-01: Create File** – User selects option 1, supplies a valid path, optionally confirms directory scaffolding, and receives success or error feedback.
- **UC-02: Delete Entry** – User selects option 2, supplies a path, optionally confirms recursive deletion, and the system removes the entry when possible.
- **UC-03: Rename Entry** – User selects option 3, provides source and destination paths, and the system renames the entry when validation passes.
- **UC-04: Inspect Metadata** – User selects option 4 or 6–8 to retrieve detailed file information, including metadata, timestamps, size, and permissions.
- **UC-05: List Directory** – User selects option 5 to enumerate entries and distinguish directories from files.
- **UC-06: Render Text to Image** – User runs `TextToPng` with input and output paths to capture console transcripts as images.

## 6. System Architecture
The solution consists of two independent console applications:

- `FileMenuApp` orchestrates a command loop. Each menu option delegates to a helper method responsible for prompting, validation, and execution using Java NIO (`java.nio.file`). The design keeps state minimal by relying on local variables within each helper method and by reusing the shared `Scanner` instance.
- `TextToPng` is a utility entry point that reads plain text, measures layout using AWT `FontMetrics`, and renders the content into a buffered image before writing it to disk.

Both executables run as standalone processes with no shared runtime state. The applications interact exclusively with the host file system and standard input/output streams.

## 7. Data Design
- **Inputs**: User-entered file-system paths and confirmation responses captured through `Scanner`. `TextToPng` accepts command-line arguments and reads text file contents.
- **Outputs**: Console messages describing prompts, errors, and operation results. `TextToPng` produces PNG image files alongside console usage guidance.
- **File-System Entities**: Paths are represented using `java.nio.file.Path`, allowing cross-platform compatibility and robust validation. Metadata retrieval leverages `BasicFileAttributes` to avoid platform-specific code.

## 8. Error Handling and Validation
- All path inputs are sanitized by trimming whitespace and rejecting empty strings.
- Invalid or non-existent paths result in descriptive error messages without performing destructive operations.
- Potentially dangerous actions (creating parent directories, deleting non-empty directories) require affirmative confirmation.
- IOExceptions are caught and reported to prevent the application from crashing while giving users actionable feedback.

## 9. Non-Functional Requirements
- **Portability**: Uses standard Java SE APIs (Java 17+), making the application cross-platform.
- **Usability**: Provides clear prompts, menu-driven navigation, and descriptive output to guide novice users.
- **Maintainability**: Organizes logic into focused helper methods, making it easier to test and extend.
- **Reliability**: Validates input before performing file operations and reports errors without terminating unexpectedly.

## 10. Future Enhancements
- Add localization support for menu prompts and output messages.
- Provide dry-run previews for destructive actions.
- Introduce scripting support for batch operations or integration testing.
- Extend `TextToPng` with themes, fonts, or syntax highlighting options.
