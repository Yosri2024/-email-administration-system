package emailapp;

/**
 * Represents company departments.
 * Each department has a numeric code and display name.
 */
public enum Department {
    SALES(1, "Sales"),
    DEVELOPMENT(2, "Development"),
    ACCOUNTING(3, "Accounting"),
    NONE(0, "None");

    private final int code;
    private final String displayName;

    Department(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolve department from numeric code.
     * Returns null if code is unknown.
     */
    public static Department fromCode(int code) {
        for (Department d : values()) {
            if (d.code == code) {
                return d;
            }
        }
        return null;
    }

    /**
     * @return lowercase name for email domain (e.g. "sales")
     */
    public String toEmailDomain() {
        return displayName.toLowerCase();
    }
}
