package ost;

import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Account extends DatabaseAPI {

    private static final String USER_TABLE = "User";
    private static final String SECURITY_TABLE = "Security";

    // Added signup_date to User table
    private static final String USER_FIELDS = "id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, password_hash TEXT, signup_date TEXT";
    private static final String SECURITY_FIELDS = "user_id INTEGER, salt TEXT, pepper TEXT, FOREIGN KEY(user_id) REFERENCES User(id)";

    private String pepper; 

    public Account() {
        
        EnvLoader envLoader = new EnvLoader(".env");
        pepper = envLoader.get("PEPPER"); 

        
        createTable(USER_TABLE, USER_FIELDS);
        createTable(SECURITY_TABLE, SECURITY_FIELDS);
    }

    public void addAccount(String email, String password) {
        String salt = BCrypt.gensalt();
        String passwordWithPepper = password + pepper; 
        String hashedPassword = BCrypt.hashpw(passwordWithPepper, salt);

        String signupDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        insert(USER_TABLE, "email, password_hash, signup_date", "'" + email + "', '" + hashedPassword + "', '" + signupDate + "'");

        String userId = getValue(USER_TABLE, "email", email, "id");

        insert(SECURITY_TABLE, "user_id, salt, pepper", userId + ", '" + salt + "', '" + pepper + "'"); 
    }

    public boolean verifyAccount(String email) {
        return isKeyAvailable(USER_TABLE, "email", email);
    }

    public boolean verifyPassword(String email, String password) {

        String userId = getValue(USER_TABLE, "email", email, "id");
        String salt = getValue(SECURITY_TABLE, "user_id", userId, "salt");

        String hashedPassword = getValue(USER_TABLE, "email", email, "password_hash");

        if (hashedPassword != null && salt != null) {
            String passwordWithPepper = password + pepper; 
            return BCrypt.checkpw(passwordWithPepper, hashedPassword);
        }
        return false;
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
