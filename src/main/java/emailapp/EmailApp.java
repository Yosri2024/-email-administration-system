package emailapp;

import java.util.Scanner;

/**
 * Email Administration System - Entry point.
 *
 * <p>Improvements over original Udemy code:
 * <ul>
 *   <li>Single Scanner with try-with-resources (no resource leak)</li>
 *   <li>nextLine() everywhere - no InputMismatchException</li>
 *   <li>Input validation for names and menu choices</li>
 *   <li>Graceful handling of invalid menu input (e.g. "abc" instead of number)</li>
 *   <li>Clear exit handling</li>
 * </ul>
 */
public class EmailApp {

    private static final String MENU = """
            
            **********
            ENTER YOUR CHOICE
            1. Show Info
            2. Change Password
            3. Change Mailbox Capacity
            4. Set Alternate Email
            5. Store data in file
            6. Show file
            7. Exit
            **********""";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            String firstName = promptNonEmpty(scanner, "Enter First Name:");
            String lastName = promptNonEmpty(scanner, "Enter Last Name:");

            Email email = new Email(firstName, lastName, scanner);

            while (true) {
                System.out.println(MENU);
                System.out.print("Choice (1-7): ");
                String line = scanner.nextLine().trim();

                int choice;
                try {
                    choice = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    System.out.println("INVALID CHOICE! Please enter a number 1-7");
                    continue;
                }

                switch (choice) {
                    case 1 -> email.showInfo();
                    case 2 -> email.changePassword();
                    case 3 -> email.setMailboxCapacity();
                    case 4 -> email.setAlternateEmail();
                    case 5 -> email.storeToFile();
                    case 6 -> email.readFromFile();
                    case 7 -> {
                        System.out.println("\nTHANKS!!!");
                        return;
                    }
                    default -> System.out.println("INVALID CHOICE! ENTER AGAIN! (1-7)");
                }
            }
        }
    }

    private static String promptNonEmpty(Scanner scanner, String prompt) {
        while (true) {
            System.out.println(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty() && input.matches("[A-Za-z'\\- ]+")) {
                // Allow letters, apostrophe, hyphen, space - covers most names
                // For strictness we trim and take first token as in original?
                // We'll keep full line but validate
                return input.split("\\s+")[0]; // keep compatibility with old single-word logic
            }
            if (!input.isEmpty()) {
                // If it contains invalid chars but is not empty, still accept first word letters
                // Fallback: accept any non-empty single word
                String firstWord = input.split("\\s+")[0];
                if (firstWord.matches("[A-Za-z]+")) {
                    return firstWord;
                }
            }
            System.out.println("Invalid input! Please enter a valid name (letters only).");
        }
    }
}
