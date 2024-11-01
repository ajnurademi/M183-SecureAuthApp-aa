package ost;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Account extends DatabaseAPI {

    private static final String USER_TABLE = "User";
    
    private static final String USER_FIELDS = "id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, password_hash TEXT, signup_date TEXT";

    private String pepper;

    public Account() {
        EnvLoader envLoader = new EnvLoader(".env");
        pepper = envLoader.get("PEPPER");

        createTable(USER_TABLE, USER_FIELDS);
    }

    public void addAccount(String email, String password) throws Exception {
        StringBuilder errorMessage = new StringBuilder("Bitte stellen Sie sicher, dass Ihr Passwort die folgenden Anforderungen erfüllt:\n");

        boolean isStrong = true;

        if (!isValidEmail(email)) {
            throw new Exception("Bitte geben Sie eine gültige E-Mail-Adresse ein (z.B. example@domain.com).");
        }

        if (password.length() < 8) {
            errorMessage.append("- Mindestens 8 Zeichen lang.\n");
            isStrong = false;
        }
        if (!password.matches(".*[A-Z].*")) {
            errorMessage.append("- Mindestens ein Großbuchstabe.\n");
            isStrong = false;
        }
        if (!password.matches(".*[a-z].*")) {
            errorMessage.append("- Mindestens ein Kleinbuchstabe.\n");
            isStrong = false;
        }
        if (!password.matches(".*[0-9].*")) {
            errorMessage.append("- Mindestens eine Ziffer.\n");
            isStrong = false;
        }
        if (!password.matches(".*[§$%&!?].*")) {
            errorMessage.append("- Mindestens ein Sonderzeichen (z.B. §, $, %, &, !).\n");
            isStrong = false;
        }

        if (!isStrong) {
            throw new Exception(errorMessage.toString());
        }

        // Passwort mit Pepper kombinieren und hashen
        String passwordWithPepper = password + pepper;
        String hashedPassword = BCrypt.hashpw(passwordWithPepper, BCrypt.gensalt());

        String signupDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        insert(USER_TABLE, "email, password_hash, signup_date", "'" + email + "', '" + hashedPassword + "', '" + signupDate + "'");
    }

    public boolean verifyPassword(String email, String password) {
        String hashedPassword = getValue(USER_TABLE, "email", email, "password_hash");

        if (hashedPassword != null) {
            String passwordWithPepper = password + pepper;
            return BCrypt.checkpw(passwordWithPepper, hashedPassword);
        }

        return false;
    }

    public boolean verifyAccount(String email) {
        String userId = getValue(USER_TABLE, "email", email, "id");
        return userId != null;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(emailRegex);
    }
}
