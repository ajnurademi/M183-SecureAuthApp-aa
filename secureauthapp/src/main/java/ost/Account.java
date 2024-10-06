package ost;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Account extends DatabaseAPI {

    private static final String USER_TABLE = "User";
    private static final String SECURITY_TABLE = "Security";

    private static final String USER_FIELDS = "id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, password_hash TEXT, signup_date TEXT";
    private static final String SECURITY_FIELDS = "user_id INTEGER, salt TEXT, pepper TEXT, FOREIGN KEY(user_id) REFERENCES User(id)";

    private String pepper;

    public Account() {
        EnvLoader envLoader = new EnvLoader(".env");
        pepper = envLoader.get("PEPPER");

        createTable(USER_TABLE, USER_FIELDS);
        createTable(SECURITY_TABLE, SECURITY_FIELDS);
    }

    public void addAccount(String email, String password) throws Exception {
        StringBuilder errorMessage = new StringBuilder("Please ensure your password meets the following requirements:\n");

        boolean isStrong = true;

        // E-Mail-Validierung
        if (!isValidEmail(email)) {
            throw new Exception("Bitte geben Sie eine gültige E-Mail-Adresse ein (z.B. example@domain.com).");
        }

        if (password.length() < 8) {
            errorMessage.append("- At least 8 characters long.\n");
            isStrong = false;
        }
        if (!password.matches(".*[A-Z].*")) {
            errorMessage.append("- At least one uppercase letter.\n");
            isStrong = false;
        }
        if (!password.matches(".*[a-z].*")) {
            errorMessage.append("- At least one lowercase letter.\n");
            isStrong = false;
        }
        if (!password.matches(".*[0-9].*")) {
            errorMessage.append("- At least one digit.\n");
            isStrong = false;
        }
        if (!password.matches(".*[§$%&!?].*")) {
            errorMessage.append("- At least one special character (e.g., §, $, %, &, !).\n");
            isStrong = false;
        }

        if (!isStrong) {
            throw new Exception(errorMessage.toString());
        }

        String salt = BCrypt.gensalt();
        String passwordWithPepper = password + pepper;
        String hashedPassword = BCrypt.hashpw(passwordWithPepper, salt);

        String signupDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        insert(USER_TABLE, "email, password_hash, signup_date", "'" + email + "', '" + hashedPassword + "', '" + signupDate + "'");

        String userId = getValue(USER_TABLE, "email", email, "id");

        insert(SECURITY_TABLE, "user_id, salt, pepper", userId + ", '" + salt + "', '" + pepper + "'");
    }

    public boolean verifyPassword(String email, String password) {
        String userId = getValue(USER_TABLE, "email", email, "id");

        if (userId == null) {
            return false;
        }

        String salt = getValue(SECURITY_TABLE, "user_id", userId, "salt");
        String hashedPassword = getValue(USER_TABLE, "email", email, "password_hash");

        if (hashedPassword != null && salt != null) {
            String passwordWithPepper = password + pepper;
            return BCrypt.checkpw(passwordWithPepper, hashedPassword);
        }

        return false;
    }

    public boolean verifyAccount(String email) {
        String userId = getValue(USER_TABLE, "email", email, "id");
        return userId != null;
    }

    // E-Mail-Validierungsfunktion
    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(emailRegex);
    }
}
