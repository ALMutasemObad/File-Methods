import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.InvalidPathException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * A console application that exposes a text menu covering common file operations.
 */
public class FileMenuApp {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== File Menu App ===");
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Choose an option: ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    createFile(scanner);
                    break;
                case "2":
                    deleteFile(scanner);
                    break;
                case "3":
                    renameFile(scanner);
                    break;
                case "4":
                    displayMetadata(scanner);
                    break;
                case "5":
                    listDirectory(scanner);
                    break;
                case "6":
                    showLastModified(scanner);
                    break;
                case "7":
                    showFileSize(scanner);
                    break;
                case "8":
                    showPermissions(scanner);
                    break;
                case "9":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 9.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1. Create a file");
        System.out.println("2. Delete a file");
        System.out.println("3. Rename a file");
        System.out.println("4. Display file metadata");
        System.out.println("5. List directory contents");
        System.out.println("6. Show last modified timestamp");
        System.out.println("7. Show file size");
        System.out.println("8. Show permissions");
        System.out.println("9. Exit");
    }

    private static void createFile(Scanner scanner) {
        Path path = promptForPath(scanner, "Enter the path of the file to create: ", true);
        if (path == null) {
            return;
        }
        if (Files.exists(path)) {
            System.out.printf("A file or directory already exists at %s%n", path);
            return;
        }
        try {
            Path parent = path.getParent();
            if (parent != null && Files.notExists(parent)) {
                System.out.printf("Parent directory %s does not exist.%n", parent);
                return;
            }
            Files.createFile(path);
            System.out.printf("File created at %s%n", path.toAbsolutePath());
        } catch (IOException e) {
            System.out.printf("Failed to create file: %s%n", e.getMessage());
        }
    }

    private static void deleteFile(Scanner scanner) {
        Path path = promptForPath(scanner, "Enter the path of the file or directory to delete: ", false);
        if (path == null) {
            return;
        }
        if (Files.notExists(path)) {
            System.out.printf("No file or directory found at %s%n", path);
            return;
        }
        try {
            if (Files.isDirectory(path)) {
                if (!isDirectoryEmpty(path)) {
                    System.out.printf("Directory %s is not empty.%n", path);
                    if (!confirm(scanner, "Delete it recursively? (y/N): ")) {
                        System.out.println("Deletion cancelled.");
                        return;
                    }
                    deleteRecursively(path);
                    System.out.printf("Deleted %s%n", path.toAbsolutePath());
                    return;
                }
            }
            Files.delete(path);
            System.out.printf("Deleted %s%n", path.toAbsolutePath());
        } catch (IOException e) {
            System.out.printf("Failed to delete: %s%n", e.getMessage());
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    private static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return !entries.findFirst().isPresent();
        }
    }

    private static void renameFile(Scanner scanner) {
        Path source = promptForPath(scanner, "Enter the current path: ", false);
        if (source == null) {
            return;
        }
        if (Files.notExists(source)) {
            System.out.printf("No file or directory found at %s%n", source);
            return;
        }
        Path target = promptForPath(scanner, "Enter the new path: ", true);
        if (target == null) {
            return;
        }
        if (Files.exists(target)) {
            System.out.printf("Cannot rename: target %s already exists.%n", target);
            return;
        }
        try {
            Files.move(source, target);
            System.out.printf("Renamed %s to %s%n", source.toAbsolutePath(), target.toAbsolutePath());
        } catch (IOException e) {
            System.out.printf("Failed to rename: %s%n", e.getMessage());
        }
    }

    private static void displayMetadata(Scanner scanner) {
        Path path = promptForSimplePath(scanner, "Enter the path to inspect: ");
        if (path == null) {
            return;
        }
        if (Files.notExists(path)) {
            System.out.printf("No file or directory found at %s%n", path);
            return;
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            System.out.println("=== Metadata ===");
            System.out.printf("Path: %s%n", path.toAbsolutePath());
            System.out.printf("Type: %s%n", attrs.isDirectory() ? "Directory" : attrs.isRegularFile() ? "Regular file" : "Other");
            System.out.printf("Size: %d bytes%n", attrs.size());
            System.out.printf("Creation time: %s%n", DATE_FORMATTER.format(attrs.creationTime().toInstant()));
            System.out.printf("Last modified: %s%n", DATE_FORMATTER.format(attrs.lastModifiedTime().toInstant()));
            System.out.printf("Last accessed: %s%n", DATE_FORMATTER.format(attrs.lastAccessTime().toInstant()));
        } catch (IOException e) {
            System.out.printf("Failed to read metadata: %s%n", e.getMessage());
        }
    }

    private static void listDirectory(Scanner scanner) {
        Path path = promptForSimplePath(scanner, "Enter the directory to list: ");
        if (path == null) {
            return;
        }
        if (Files.notExists(path)) {
            System.out.printf("Directory %s does not exist.%n", path);
            return;
        }
        if (!Files.isDirectory(path)) {
            System.out.printf("%s is not a directory.%n", path);
            return;
        }
        try {
            List<Path> entries = new ArrayList<>();
            try (var stream = Files.list(path)) {
                stream.forEach(entries::add);
            }
            if (entries.isEmpty()) {
                System.out.println("Directory is empty.");
                return;
            }
            System.out.println("Contents:");
            for (Path entry : entries) {
                String type = Files.isDirectory(entry) ? "<DIR>" : "     ";
                System.out.printf("%s %s%n", type, entry.getFileName());
            }
        } catch (IOException e) {
            System.out.printf("Failed to list directory: %s%n", e.getMessage());
        }
    }

    private static void showLastModified(Scanner scanner) {
        Path path = promptForSimplePath(scanner, "Enter the path to inspect: ");
        if (path == null) {
            return;
        }
        if (Files.notExists(path)) {
            System.out.printf("No file or directory found at %s%n", path);
            return;
        }
        try {
            Instant lastModified = Files.getLastModifiedTime(path).toInstant();
            System.out.printf("Last modified: %s%n", DATE_FORMATTER.format(lastModified));
        } catch (IOException e) {
            System.out.printf("Failed to retrieve last modified time: %s%n", e.getMessage());
        }
    }

    private static void showFileSize(Scanner scanner) {
        Path path = promptForSimplePath(scanner, "Enter the file path: ");
        if (path == null) {
            return;
        }
        if (Files.notExists(path)) {
            System.out.printf("No file found at %s%n", path);
            return;
        }
        if (Files.isDirectory(path)) {
            System.out.println("The specified path is a directory. Use option 5 to explore directories.");
            return;
        }
        try {
            long size = Files.size(path);
            System.out.printf("Size: %d bytes%n", size);
        } catch (IOException e) {
            System.out.printf("Failed to determine size: %s%n", e.getMessage());
        }
    }

    private static void showPermissions(Scanner scanner) {
        Path path = promptForSimplePath(scanner, "Enter the path to inspect: ");
        if (path == null) {
            return;
        }
        if (Files.notExists(path)) {
            System.out.printf("No file or directory found at %s%n", path);
            return;
        }
        System.out.println("Permissions:");
        System.out.printf("Readable: %s%n", Files.isReadable(path));
        System.out.printf("Writable: %s%n", Files.isWritable(path));
        System.out.printf("Executable: %s%n", Files.isExecutable(path));

        if (!FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            System.out.println("Detailed POSIX permissions are not supported on this platform.");
            return;
        }
        try {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
            System.out.printf("POSIX: %s%n", permissions);
        } catch (IOException e) {
            System.out.printf("Failed to retrieve POSIX permissions: %s%n", e.getMessage());
        }
    }

    private static Path promptForPath(Scanner scanner, String prompt, boolean allowCreateParents) {
        Path path = promptForSimplePath(scanner, prompt);
        if (path == null) {
            return null;
        }
        if (!allowCreateParents) {
            return path;
        }
        Path parent = path.getParent();
        if (parent == null || Files.exists(parent)) {
            return path;
        }
        if (!confirm(scanner, String.format("Parent directory %s is missing. Create it? (y/N): ", parent))) {
            System.out.println("Skipping creation of missing directories.");
            return null;
        }
        try {
            Files.createDirectories(parent);
            System.out.printf("Created missing directories up to %s%n", parent);
            return path;
        } catch (IOException e) {
            System.out.printf("Failed to create directories: %s%n", e.getMessage());
            return null;
        }
    }

    private static Path promptForSimplePath(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println("Path cannot be empty.");
            return null;
        }
        try {
            return Paths.get(input);
        } catch (InvalidPathException e) {
            System.out.printf("Invalid path: %s%n", e.getInput());
            return null;
        }
    }

    private static boolean confirm(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String response = scanner.nextLine().trim();
        return response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes");
    }
}
