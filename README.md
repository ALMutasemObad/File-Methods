# File Menu Application

A console-based Java program that demonstrates core file-system operations through a menu-driven interface. Students can explore how to create, delete, rename, and inspect files or directories while receiving clear validation and feedback for each action.

## Prerequisites
- Java 17 or newer

## Build and Run
```bash
javac src/FileMenuApp.java
java -cp src FileMenuApp
```

The application will display a numbered menu. Enter the corresponding number to perform an operation, or choose option 9 to exit. Paths may be absolute or relative to the directory in which you launch the application.

## Features
- File and directory creation with optional parent directory scaffolding
- Safe deletion with optional recursive cleanup after confirmation when directories are not empty
- Renaming with collision checks
- Metadata reporting (type, size, timestamps)
- Directory listing with entry classification
- Last modified timestamp and file size lookup
- Permission summary with optional POSIX details when supported

## Documentation
Refer to [`docs/BlackboardSubmission.md`](docs/BlackboardSubmission.md) for the introduction, deliverables, and checklist prepared for Blackboard submission. A sample execution transcript is available at [`docs/sample-execution.txt`](docs/sample-execution.txt).
