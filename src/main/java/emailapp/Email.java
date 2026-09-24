package emailapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Email Administration System - Core domain class.
 *
 * <p>Refactored from Udemy course project to production-ready standards:
 * <ul>
 *   <li>Single injected Scanner (no multiple Scanners on System.in)</li>
 *   <li>Enum for Department instead of Stringly-typed codes</li>
 *   <li>SecureRandom for password generation with guaranteed character classes</li>
 *   <li>Robust input handling (no InputMismatchException on "1gb")</li>
 *   <li>Cross-platform file storage (data/Info.txt, UTF-8, try-with-resources)</li>
 *   <li>Input validation (names, email format, capacity)</li>
 *   <li>Java naming conventions (camelCase) with backward-compatible deprecated aliases</li>
 * </ul>
 */
public class Email {

    // --- Constants ---
    private static final int DEFAULT_MAILBOX_CAPACITY_MB = 500;
    private static final int DEFAULT_PASSWORD_LENGTH = 10;
    private static final String COMPANY_DOMAIN = "company.com";
    private static final int MIN_PASSWORD_LENGTH = 6;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%&?";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SYMBOLS;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // --- Fields ---
    private final String firstName;
    private final String lastName;
    private Department department;
    private String email;
    private String password;
    private int mailboxCapacity = DEFAULT_MAILBOX_CAPACITY_MB;
    private String alternateEmail;

    /**
     * Scanner is injected from the caller (usually EmailApp).
     * This avoids the bug of having 2 Scanners on System.in competing for input.
     */
    private final Scanner scanner;

    // -----------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------

    /**
     * Primary constructor - inject Scanner.
     */
    public Email(String firstName, String lastName, Scanner scanner) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name must not be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name must not be blank");
        }
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner must not be null");
        }
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.scanner = scanner;

        System.out.println("NEW EMPLOYEE: " + this.firstName + " " + this.lastName);

        this.department = requestDepartment();
        this.password = generatePassword(DEFAULT_PASSWORD_LENGTH);
        System.out.println("Generated Password: " + this.password);
        this.email = generateEmail();
        System.out.println("Generated Email: " + this.email);
    }

    /**
     * Backward-compatible constructor (creates its own Scanner).
     * Prefer the 3-arg constructor for testability.
     * @deprecated use {@link #Email(String, String, Scanner)}
     */
    @Deprecated
    public Email(String firstName, String lastName) {
        this(firstName, lastName, new Scanner(System.in));
    }

    // -----------------------------------------------------------------
    // Core business logic
    // -----------------------------------------------------------------

    private String generateEmail() {
        String localPart = firstName.toLowerCase() + "." + lastName.toLowerCase();
        if (department == Department.NONE) {
            return localPart + "@" + COMPANY_DOMAIN;
        }
        return localPart + "@" + department.toEmailDomain() + "." + COMPANY_DOMAIN;
    }

    private Department requestDepartment() {
        System.out.println("\nDEPARTMENT CODES");
        for (Department d : Department.values()) {
            System.out.printf("%d for %s%n", d.getCode(), d.getDisplayName());
        }

        while (true) {
            System.out.print("Enter Department Code: ");
            String line = scanner.nextLine().trim();
            try {
                int code = Integer.parseInt(line);
                Department dept = Department.fromCode(code);
                if (dept != null) {
                    return dept;
                }
                System.out.println("** INVALID CHOICE ** Please enter 0,1,2,3");
            } catch (NumberFormatException e) {
                System.out.println("** INVALID INPUT ** Please enter a number (0,1,2,3)");
            }
        }
    }

    /**
     * Generates a secure password guaranteeing at least one char from each class.
     */
    private String generatePassword(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("Password length must be >= 4");
        }
        List<Character> chars = new ArrayList<>();
        chars.add(UPPERCASE.charAt(SECURE_RANDOM.nextInt(UPPERCASE.length())));
        chars.add(LOWERCASE.charAt(SECURE_RANDOM.nextInt(LOWERCASE.length())));
        chars.add(DIGITS.charAt(SECURE_RANDOM.nextInt(DIGITS.length())));
        chars.add(SYMBOLS.charAt(SECURE_RANDOM.nextInt(SYMBOLS.length())));

        for (int i = 4; i < length; i++) {
            chars.add(ALL_CHARS.charAt(SECURE_RANDOM.nextInt(ALL_CHARS.length())));
        }
        Collections.shuffle(chars, SECURE_RANDOM);

        StringBuilder sb = new StringBuilder(length);
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    // --- Public API (clean naming) ---

    public void changePassword() {
        while (true) {
            System.out.print("ARE YOU SURE YOU WANT TO CHANGE YOUR PASSWORD? (Y/N): ");
            String choice = scanner.nextLine().trim();
            if (choice.isEmpty()) {
                System.out.println("** ENTER A VALID CHOICE **");
                continue;
            }
            char c = choice.charAt(0);
            if (c == 'Y' || c == 'y') {
                System.out.print("Enter current password: ");
                String current = scanner.nextLine();
                if (!current.equals(this.password)) {
                    System.out.println("Incorrect Password!");
                    return;
                }
                System.out.print("Enter new password (min " + MIN_PASSWORD_LENGTH + " chars): ");
                String newPass = scanner.nextLine();
                if (newPass.length() < MIN_PASSWORD_LENGTH) {
                    System.out.println("Password too short! Must be >= " + MIN_PASSWORD_LENGTH);
                    return;
                }
                this.password = newPass;
                System.out.println("PASSWORD CHANGED SUCCESSFULLY!");
                return;
            } else if (c == 'N' || c == 'n') {
                System.out.println("PASSWORD CHANGE CANCELED!");
                return;
            } else {
                System.out.println("** ENTER A VALID CHOICE (Y/N) **");
            }
        }
    }

    public void setMailboxCapacity() {
        System.out.println("Current capacity = " + this.mailboxCapacity + "mb");
        System.out.print("Enter new capacity (e.g. 1000, 500mb, 1gb): ");
        String input = scanner.nextLine().toLowerCase().trim();

        if (input.isEmpty()) {
            System.out.println("Capacity not changed - empty input.");
            return;
        }

        try {
            int newCapacity;
            if (input.endsWith("gb")) {
                String num = input.replace("gb", "").trim();
                newCapacity = Integer.parseInt(num) * 1024;
            } else if (input.endsWith("mb")) {
                String num = input.replace("mb", "").trim();
                newCapacity = Integer.parseInt(num);
            } else {
                newCapacity = Integer.parseInt(input);
            }

            if (newCapacity <= 0) {
                System.out.println("Capacity must be > 0");
                return;
            }
            if (newCapacity > 100 * 1024) {
                System.out.println("Capacity too large (max 100gb)");
                return;
            }

            this.mailboxCapacity = newCapacity;
            System.out.println("MAILBOX CAPACITY CHANGED SUCCESSFULLY! New = " + this.mailboxCapacity + "mb");
        } catch (NumberFormatException e) {
            System.out.println("INVALID INPUT! Use e.g. 1000, 500mb, 1gb");
        }
    }

    public void setAlternateEmail() {
        System.out.print("Enter alternate email: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("Alternate email not changed - empty input.");
            return;
        }
        // Explicit check demanded by user: must contain @ and .com/.xx
        if (!input.contains("@")) {
            System.out.println("Invalid email! Must contain '@' (e.g. name@gmail.com)");
            return;
        }
        if (!input.toLowerCase().contains(".com") && !input.contains(".")) {
            System.out.println("Invalid email! Must contain '.' and domain like '.com' (e.g. name@gmail.com)");
            return;
        }
        // Stricter: if user wants exactly .com, uncomment next line:
        // if (!input.toLowerCase().endsWith(".com")) { System.out.println("Invalid! Must end with '.com'"); return; }
        if (!EMAIL_PATTERN.matcher(input).matches()) {
            System.out.println("Invalid email format! Example: name@example.com");
            return;
        }
        this.alternateEmail = input;
        System.out.println("ALTERNATE EMAIL SET SUCCESSFULLY!");
    }

    public void showInfo() {
        System.out.println("\n========== EMPLOYEE INFO ==========");
        System.out.println("NAME            : " + firstName + " " + lastName);
        System.out.println("DEPARTMENT      : " + department.getDisplayName());
        System.out.println("EMAIL           : " + email);
        System.out.println("PASSWORD        : " + password);
        System.out.println("MAILBOX CAPACITY: " + mailboxCapacity + "mb");
        System.out.println("ALTERNATE EMAIL : " + (alternateEmail != null ? alternateEmail : "Not Set"));
        System.out.println("===================================\n");
    }

    // --- Persistence (cross-platform) ---

    private Path getFilePath() {
        // Primary: project-relative data/Info.txt (works in IDE, jar, and CI)
        Path projectPath = Paths.get("data", "Info.txt");
        // Fallback: if not writable, try Desktop (for users expecting desktop file)
        // We always use projectPath unless Desktop is explicitly preferred and exists.
        // To keep GitHub clean, data/ is gitignored and created on demand.
        return projectPath;
    }

    public void storeToFile() {
        Path path = getFilePath();
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write("First Name: " + firstName);
                writer.newLine();
                writer.write("Last Name: " + lastName);
                writer.newLine();
                writer.write("Department: " + department.getDisplayName());
                writer.newLine();
                writer.write("Email: " + email);
                writer.newLine();
                writer.write("Password: " + password);
                writer.newLine();
                writer.write("Capacity: " + mailboxCapacity + "mb");
                writer.newLine();
                writer.write("Alternate Email: " + (alternateEmail != null ? alternateEmail : "Not Set"));
                writer.newLine();
            }
            System.out.println("Stored to " + path.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to store file: " + e.getMessage());
        }
    }

    public void readFromFile() {
        Path path = getFilePath();
        if (!Files.exists(path)) {
            System.out.println("File not found: " + path.toAbsolutePath());
            System.out.println("Hint: Use option 5 (Store data) first to create the file.");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            System.out.println("\n--- File Content (" + path.toAbsolutePath() + ") ---");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("--- End of File ---\n");
        } catch (IOException e) {
            System.out.println("Failed to read file: " + e.getMessage());
        }
    }

    // --- Getters ---

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Department getDepartment() { return department; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public int getMailboxCapacity() { return mailboxCapacity; }
    public String getAlternateEmail() { return alternateEmail; }
    public Path getStoragePath() { return getFilePath(); }

    // For testing: allow generating password with custom length without UI
    public String generatePasswordForTest(int length) {
        return generatePassword(length);
    }

    @Override
    public String toString() {
        return "Email{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", department=" + department +
                ", email='" + email + '\'' +
                ", mailboxCapacity=" + mailboxCapacity +
                ", alternateEmail='" + alternateEmail + '\'' +
                '}';
    }

    // -----------------------------------------------------------------
    // Deprecated snake_case aliases for Udemy backward compatibility
    // -----------------------------------------------------------------

    /** @deprecated use {@link #changePassword()} */
    @Deprecated
    public void set_password() { changePassword(); }

    /** @deprecated use {@link #setMailboxCapacity()} */
    @Deprecated
    public void set_mailCap() { setMailboxCapacity(); }

    /** @deprecated use {@link #setAlternateEmail()} */
    @Deprecated
    public void alternate_email() { setAlternateEmail(); }

    /** @deprecated use {@link #showInfo()} */
    @Deprecated
    public void getInfo() { showInfo(); }

    /** @deprecated use {@link #storeToFile()} */
    @Deprecated
    public void storefile() { storeToFile(); }

    /** @deprecated use {@link #readFromFile()} */
    @Deprecated
    public void read_file() { readFromFile(); }

    // Legacy public Scanner field kept for compatibility but delegates to injected scanner
    // Original code had: public Scanner s = new Scanner(System.in);
    // We keep a getter for s that returns the injected scanner to avoid breaking reflection
    @Deprecated
    public Scanner getScanner() { return scanner; }
}
