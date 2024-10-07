package ost;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    public void testWeakPassword() {
        Account account = new Account();
        String email = "test@example.com";
        String weakPassword = "pass123"; 

        Exception exception = assertThrows(Exception.class, () -> {
            account.addAccount(email, weakPassword);
        });

        String expectedMessage = "Please ensure your password meets the following requirements:";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testInvalidEmail() {
        Account account = new Account();
        String invalidEmail = "invalid-email";  
        String validPassword = "ValidPass123!"; 

        Exception exception = assertThrows(Exception.class, () -> {
            account.addAccount(invalidEmail, validPassword);
        });

        String expectedMessage = "Bitte geben Sie eine gültige E-Mail-Adresse ein (z.B. example@domain.com).";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), "Die Fehlermeldung sollte die E-Mail-Validierung erwähnen.");
    }
}
