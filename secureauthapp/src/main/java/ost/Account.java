package ost;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Account extends DatabaseAPI {

    private static final String USER_TABLE = "User";
    private static final String SECURITY_TABLE = "Security";
    private static final String LOGIN_ATTEMPT_TABLE = "LoginAttempts";

    private static final String USER_FIELDS = "id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, password_hash TEXT, signup_date TEXT";
    private static final String SECURITY_FIELDS = "user_id INTEGER, salt TEXT, pepper TEXT, FOREIGN KEY(user_id) REFERENCES User(id)";
    private static final String LOGIN_ATTEMPT_FIELDS = "user_id INTEGER, attempt_count INTEGER DEFAULT 0, lock_time TEXT, FOREIGN KEY(user_id) REFERENCES User(id)";
    private static final String PASSWORD_RESET_TOKEN_FIELDS = "user_id INTEGER, token TEXT, expiration TEXT, FOREIGN KEY(user_id) REFERENCES User(id)";

    private String pepper;

    public Account() {
        EnvLoader envLoader = new EnvLoader(".env");
        pepper = envLoader.get("PEPPER");

        createTable(USER_TABLE, USER_FIELDS);
        createTable(SECURITY_TABLE, SECURITY_FIELDS);
        createTable(LOGIN_ATTEMPT_TABLE, LOGIN_ATTEMPT_FIELDS);
        createTable("PasswordResetTokens", PASSWORD_RESET_TOKEN_FIELDS); // Tabelle für Passwortzurücksetzungen erstellen
    }

    public void addAccount(String email, String password) throws Exception {
        StringBuilder errorMessage = new StringBuilder("Please ensure your password meets the following requirements:\n");

        boolean isStrong = true;

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
        insert(LOGIN_ATTEMPT_TABLE, "user_id", userId);
    }

    public boolean verifyPassword(String email, String password) {
        String userId = getValue(USER_TABLE, "email", email, "id");

        if (isAccountLocked(userId)) {
            return false;
        }

        String salt = getValue(SECURITY_TABLE, "user_id", userId, "salt");
        String hashedPassword = getValue(USER_TABLE, "email", email, "password_hash");

        if (hashedPassword != null && salt != null) {
            String passwordWithPepper = password + pepper;
            if (BCrypt.checkpw(passwordWithPepper, hashedPassword)) {
                resetLoginAttempts(userId);
                return true;
            }
        }

        incrementLoginAttempts(userId);
        return false;
    }

    public boolean verifyAccount(String email) {
        String userId = getValue(USER_TABLE, "email", email, "id");
        return userId != null;
    }

    public void requestPasswordReset(String email) throws Exception {
        String userId = getValue(USER_TABLE, "email", email, "id");
        if (userId == null) {
            throw new Exception("No account associated with this email.");
        }

        // Generiere einen Token für das Zurücksetzen des Passworts
        String token = UUID.randomUUID().toString();
        String expirationTime = LocalDateTime.now().plusMinutes(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Speichern Sie den Token und die Ablaufzeit in der Datenbank
        insert("PasswordResetTokens", "user_id, token, expiration", userId + ", '" + token + "', '" + expirationTime + "'");

        // Senden Sie eine E-Mail mit dem Token (hier sollte eine echte Implementierung erfolgen)
        System.out.println("Password reset link: http://example.com/reset?token=" + token);
    }

    public void resetPassword(String token, String newPassword) throws Exception {
        // Überprüfen Sie den Token und die Ablaufzeit
        String userId = getValue("PasswordResetTokens", "token", token, "user_id");
        String expiration = getValue("PasswordResetTokens", "token", token, "expiration");

        if (userId == null) {
            throw new Exception("Invalid or expired token.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDateTime = LocalDateTime.parse(expiration, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (now.isAfter(expirationDateTime)) {
            throw new Exception("Token has expired.");
        }

        // Überprüfen Sie die Passwortanforderungen
        if (!isPasswordStrong(newPassword)) {
            throw new Exception("Password does not meet strength requirements.");
        }

        // Passwort hashen und aktualisieren
        String salt = BCrypt.gensalt();
        String passwordWithPepper = newPassword + pepper;
        String hashedPassword = BCrypt.hashpw(passwordWithPepper, salt);

        // Passwort in der Benutzertabelle aktualisieren
        update(USER_TABLE, "password_hash = '" + hashedPassword + "'", "id = " + userId);
        // Löschen Sie den Token nach dem Zurücksetzen
        update("PasswordResetTokens", "user_id = NULL", "token = '" + token + "'");
    }

    private boolean isPasswordStrong(String password) {
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*[0-9].*") &&
               password.matches(".*[§$%&!?].*");
    }

    private void incrementLoginAttempts(String userId) {
        String currentAttempts = getValue(LOGIN_ATTEMPT_TABLE, "user_id", userId, "attempt_count");
        int attempts = currentAttempts == null ? 0 : Integer.parseInt(currentAttempts);
        attempts++;

        if (attempts >= 3) {
            String lockTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            update(LOGIN_ATTEMPT_TABLE, "attempt_count = " + attempts + ", lock_time = '" + lockTime + "'", "user_id = " + userId);
        } else {
            update(LOGIN_ATTEMPT_TABLE, "attempt_count = " + attempts, "user_id = " + userId);
        }
    }

    private boolean isAccountLocked(String userId) {
        String lockTime = getValue(LOGIN_ATTEMPT_TABLE, "user_id", userId, "lock_time");
        if (lockTime != null) {
            LocalDateTime lockDateTime = LocalDateTime.parse(lockTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime now = LocalDateTime.now();
            if (lockDateTime.plusSeconds(10).isAfter(now)) {
                return true;
            } else {
                resetLoginAttempts(userId);
                return false;
            }
        }
        return false;
    }

    private void resetLoginAttempts(String userId) {
        update(LOGIN_ATTEMPT_TABLE, "attempt_count = 0, lock_time = NULL", "user_id = " + userId);
    }
}