package emailapp;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    private Scanner scannerWithInput(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testEmailGeneration_SalesDepartment() {
        Scanner sc = scannerWithInput("1\n");
        Email email = new Email("Yosri", "King", sc);
        assertEquals("yosri.king@sales.company.com", email.getEmail());
        assertEquals(Department.SALES, email.getDepartment());
        sc.close();
    }

    @Test
    void testEmailGeneration_NoneDepartment() {
        Scanner sc = scannerWithInput("0\n");
        Email email = new Email("John", "Doe", sc);
        assertEquals("john.doe@company.com", email.getEmail());
        assertEquals(Department.NONE, email.getDepartment());
        sc.close();
    }

    @Test
    void testPasswordGeneration_LengthAndCharacterClasses() {
        Scanner sc = scannerWithInput("1\n");
        Email email = new Email("Test", "User", sc);
        String pwd10 = email.generatePasswordForTest(10);
        assertEquals(10, pwd10.length());
        String pwd12 = email.generatePasswordForTest(12);
        assertEquals(12, pwd12.length());

        // 8-char default password should contain at least 4 classes over many generations
        // We do a statistical check: generate 20 passwords, each should have reasonable entropy
        for (int i = 0; i < 20; i++) {
            String p = email.generatePasswordForTest(8);
            assertTrue(p.length() == 8);
            // At least one digit/symbol/upper/lower will be present due to construction
            assertTrue(p.matches(".*[A-Z].*"), "should contain uppercase");
            assertTrue(p.matches(".*[a-z].*"), "should contain lowercase");
            assertTrue(p.matches(".*[0-9].*"), "should contain digit");
        }
        sc.close();
    }

    @Test
    void testMailboxCapacity_AcceptsGbAndMb() {
        Scanner sc = scannerWithInput("1\n1gb\n500mb\n1000\n");
        Email email = new Email("A", "B", sc);
        assertEquals(500, email.getMailboxCapacity());

        email.setMailboxCapacity(); // reads "1gb" -> 1024
        assertEquals(1024, email.getMailboxCapacity());

        email.setMailboxCapacity(); // reads "500mb" -> 500
        assertEquals(500, email.getMailboxCapacity());

        email.setMailboxCapacity(); // reads "1000" -> 1000
        assertEquals(1000, email.getMailboxCapacity());
        sc.close();
    }

    @Test
    void testMailboxCapacity_InvalidInputKeepsOldValue() {
        Scanner sc = scannerWithInput("1\ninvalid\n");
        Email email = new Email("A", "B", sc);
        int before = email.getMailboxCapacity();
        email.setMailboxCapacity(); // "invalid" -> should keep old
        assertEquals(before, email.getMailboxCapacity());
        sc.close();
    }

    @Test
    void testAlternateEmailValidation() {
        // New loop behavior: invalid -> retry until valid, or quit to cancel
        Scanner sc1 = scannerWithInput("1\nnot-an-email\nquit\n");
        Email email1 = new Email("A", "B", sc1);
        email1.setAlternateEmail(); // "not-an-email" -> invalid, then "quit" -> cancelled => null
        assertNull(email1.getAlternateEmail());
        sc1.close();

        Scanner sc2 = scannerWithInput("1\nnot-an-email\ntest@example.com\n");
        Email email2 = new Email("A", "B", sc2);
        email2.setAlternateEmail(); // "not-an-email" -> retry -> "test@example.com" -> valid
        assertEquals("test@example.com", email2.getAlternateEmail());
        sc2.close();

        Scanner sc3 = scannerWithInput("1\na\nkk\ntest@gmail.com\n");
        Email email3 = new Email("A", "B", sc3);
        email3.setAlternateEmail(); // "a","kk" invalid -> finally "test@gmail.com"
        assertEquals("test@gmail.com", email3.getAlternateEmail());
        sc3.close();
    }

    @Test
    void testDepartmentFromCode() {
        assertEquals(Department.SALES, Department.fromCode(1));
        assertEquals(Department.DEVELOPMENT, Department.fromCode(2));
        assertEquals(Department.ACCOUNTING, Department.fromCode(3));
        assertEquals(Department.NONE, Department.fromCode(0));
        assertNull(Department.fromCode(99));
    }

    @Test
    void testShowInfoDoesNotThrow() {
        Scanner sc = scannerWithInput("2\n");
        Email email = new Email("Yosri", "King", sc);
        assertDoesNotThrow(email::showInfo);
        sc.close();
    }
}
