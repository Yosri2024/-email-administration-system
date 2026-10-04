# Email Administration System — Java Core

 *Email Administration System* (`8.1 JAVA EMAIL ADMINISTRATION CODE`).

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![CI](https://github.com/yosriii/email-administration-system/actions/workflows/ci.yml/badge.svg)](.github/workflows/ci.yml)

A CLI-based employee email provisioning system. Give it a first name / last name + department, it generates a secure company email, random password, and lets you manage mailbox capacity, alternate email and persistence.

**Original tutorial quirks fixed:** `InputMismatchException` on `1gb`, hardcoded `C:\Users\Dell\Desktop\Info.txt` (fails on Linux/Mac), multiple `Scanner(System.in)` leaks, `Random` vs `SecureRandom`, missing validation, unreadable concatenated file, snake_case naming, no tests/build-system.

---

## ✨ Features

- **Email generation:** `firstname.lastname@<dept>.company.com` (or `@company.com` for `None`)
- **Department selection** via `Department` enum (`SALES`, `DEVELOPMENT`, `ACCOUNTING`, `NONE`) with code validation
- **Secure password:** `SecureRandom`, 10 chars default, guaranteed upper/lower/digit/symbol + shuffle
- **Mailbox capacity:** accepts `500`, `500mb`, `1gb`, `2gb` etc. with validation (`>0`, `<=100gb`)
- **Alternate email** with RFC-like regex validation
- **Cross-platform persistence:** `data/Info.txt` (UTF-8, `try-with-resources`, creates `data/` on demand)
- **Robust CLI:** single injected `Scanner`, `nextLine()` everywhere — no crashes on `abc` or `1gb`
- **Backward compatible:** deprecated `set_password()`, `set_mailCap()`, `storefile()` aliases still work

## 🏗️ Project Structure

```
email-administration-system/
├── pom.xml                           # Maven build (Java 17, JUnit 5, exec/assembly plugins)
├── src/
│   ├── main/java/emailapp/
│   │   ├── Department.java           # Enum with codes 0-3
│   │   ├── Email.java                # Domain + business logic (~250 LOC)
│   │   └── EmailApp.java             # CLI entry point
│   └── test/java/emailapp/
│       └── EmailTest.java            # 8 JUnit 5 tests
├── data/.gitkeep                     # Runtime data (gitignored, created automatically)
├── .github/workflows/ci.yml          # GitHub Actions (JDK 17 + mvn verify)
├── .gitignore
└── LICENSE (MIT)
```

## 🚀 Getting Started

### Prerequisites
- JDK 17+ (`java -version`)
- Maven 3.9+ *or* use `javac` directly

### 1) Clone
```bash
git clone https://github.com/<your-username>/email-administration-system.git
cd email-administration-system
```

### 2) Build
```bash
# With Maven
mvn verify

# Without Maven (using your JDK)
mkdir -p out
javac -d out $(find src/main/java -name "*.java")
java -cp out emailapp.EmailApp
```

### 3) Run
```bash
# Maven
mvn exec:java

# Fat JAR
mvn package
java -jar target/email-administration-system-1.0.0-jar-with-dependencies.jar

# Plain javac (if no Maven)
java -cp out emailapp.EmailApp
```

### Example Session
```
Enter First Name:
yosri
Enter Last Name:
king
NEW EMPLOYEE: yosri king

DEPARTMENT CODES
1 for Sales
2 for Development
3 for Accounting
0 for None
Enter Department Code: 1
Generated Password: aB3$k9LpQ2
Generated Email: yosri.king@sales.company.com

**********
ENTER YOUR CHOICE
1. Show Info
...
Choice (1-7): 1

========== EMPLOYEE INFO ==========
NAME            : yosri king
DEPARTMENT      : Sales
EMAIL           : yosri.king@sales.company.com
PASSWORD        : aB3$k9LpQ2
MAILBOX CAPACITY: 500mb
ALTERNATE EMAIL : Not Set
===================================

Choice (1-7): 3
Current capacity = 500mb
Enter new capacity (e.g. 1000, 500mb, 1gb): 1gb
MAILBOX CAPACITY CHANGED SUCCESSFULLY! New = 1024mb

Choice (1-7): 5
Stored to /home/yosriii/IdeaProjects/email-administration-system/data/Info.txt

Choice (1-7): 6
--- File Content (/.../data/Info.txt) ---
First Name: yosri
Last Name: king
Department: Sales
Email: yosri.king@sales.company.com
Password: aB3$k9LpQ2
Capacity: 1024mb
Alternate Email: Not Set
--- End of File ---
```

## 🔧 Configuration

| Constant | Default | Location |
|---|---|---|
| `DEFAULT_MAILBOX_CAPACITY_MB` | `500` | `Email.java:17` |
| `DEFAULT_PASSWORD_LENGTH` | `10` | `Email.java:18` |
| `COMPANY_DOMAIN` | `company.com` | `Email.java:19` |
| Storage path | `data/Info.txt` | `Email.java:getFilePath()` |

## 🧪 Tests

```bash
mvn test
# 8 tests: email generation, password guarantees, capacity parsing, alternate email validation, etc.
```
Tests use `ByteArrayInputStream` to simulate user input without manual typing.

## 📦 What was fixed vs Original

| Original (`Project Code,Files/Email.java`) | Refactored |
|---|---|
| `public Scanner s = new Scanner(System.in)` + second Scanner in `EmailApp` -> competing inputs | Single injected `Scanner` with `try-with-resources` |
| `s.nextInt()` crashes on `1gb` | `nextLine()` + parse `gb`/`mb`, `NumberFormatException` handled |
| `Random` + string concat | `SecureRandom` + `StringBuilder` + shuffle + class guarantees |
| `C:\Users\Dell\Desktop\Info.txt` (Windows only) | `data/Info.txt` (cross-platform, `Files.createDirectories`, UTF-8) |
| `append()` without newline -> unreadable file | Each field on new line, `BufferedWriter.newLine()` |
| `snake_case` methods | `camelCase` with `@Deprecated` aliases for compatibility |
| No validation (blank names, `null` alternate, negative capacity) | `IllegalArgumentException`, regex, `>0` & `<=100gb` checks |
| `@None.company.com` for no dept | `@company.com` for `NONE` (cleaner) |
| No tests, no build file | Maven `pom.xml`, JUnit 5, CI workflow |

## 🛣️ Roadmap

- [ ] Argon2/BCrypt password hashing instead of plaintext display
- [ ] JSON persistence (Jackson) + multiple employees
- [ ] Spring Boot REST API
- [ ] Docker `openjdk:17-slim` image

## 📄 License

MIT — see [LICENSE](LICENSE).

