package ost;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

import java.util.Timer;
import java.util.TimerTask;

public class AccountController {

    private Account account;
    private int loginAttempts = 0; 
    private Timer lockoutTimer; 

    @FXML
    private Button btLogin;

    @FXML
    private Button btLogout;

    @FXML
    private Button btSignUp;

    @FXML
    private Label lbLoginMessage;

    @FXML
    private Label lbSignUpMessage;

    @FXML
    private PasswordField pfLoginPassword;

    @FXML
    private PasswordField pfSignUpConfirmPassword;

    @FXML
    private PasswordField pfSignUpPassword;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField tfSignUpEmail;

    @FXML
    private TextField tfUsername;

    @FXML
    private void initialize() throws Exception {
        account = new Account();
    }   

    @FXML
    private void onSignUp(ActionEvent event) throws Exception {
        String email = tfSignUpEmail.getText();
        
        // E-Mail validieren
        if (!account.isValidEmail(email)) {
            lbSignUpMessage.setText("Bitte geben Sie eine gültige E-Mail-Adresse ein. Bsp: benutzername@domain.com");
            return;
        }

        if (email.isEmpty()) {
            lbSignUpMessage.setText("Bitte geben Sie eine E-Mail ein");
            return;
        }

        String password = pfSignUpPassword.getText().trim();
        if (password.equals("")) {
            lbSignUpMessage.setText("Bitte geben Sie ein Passwort ein");
            return;
        }

        if (!password.equals(pfSignUpConfirmPassword.getText())) {
            lbSignUpMessage.setText("Passwörter stimmen nicht überein");
            return;
        }

        if (account.verifyAccount(email)) {
            lbSignUpMessage.setText("Ein Konto mit dieser E-Mail-Adresse existiert bereits");
            return;
        }

        account.addAccount(email, password);
        resetLogin();
        resetSignup();
        tabPane.getSelectionModel().select(1); 
    }


    @FXML
    private void onLogin(ActionEvent event) {
        String email = tfUsername.getText();
        String password = pfLoginPassword.getText();

        if (loginAttempts >= 3) {
            lbLoginMessage.setText("Account is locked. Try again later.");
            return;
        }

        if (account.verifyPassword(email, password)) {
            resetLogin();
            resetSignup();
            loginAttempts = 0; 
            tabPane.getTabs().get(0).setDisable(true);
            tabPane.getTabs().get(1).setDisable(true);
            tabPane.getTabs().get(2).setDisable(false);
            tabPane.getSelectionModel().select(2); 
        } else {
            loginAttempts++;
            lbLoginMessage.setText("Invalid email or password. Attempt: " + loginAttempts);

            if (loginAttempts >= 3) {
                lbLoginMessage.setText("Too many failed attempts. Account locked for 20 seconds.");
                lockoutTimer = new Timer();
                lockoutTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        loginAttempts = 0; 
                        lbLoginMessage.setText("You can try logging in again.");
                    }
                }, 20000); 
            }
        }
    }

    @FXML
    private void onLogout(ActionEvent event) {
        tabPane.getTabs().get(0).setDisable(false);
        tabPane.getTabs().get(1).setDisable(false);
        tabPane.getTabs().get(2).setDisable(true);
        resetLogin();
        tabPane.getSelectionModel().select(1); 
    }

    private void resetLogin() {
        tfUsername.setText("");
        pfLoginPassword.setText("");
        lbLoginMessage.setText("Login with your account");
    } 

    private void resetSignup() {
        tfSignUpEmail.setText("");
        pfSignUpPassword.setText("");
        pfSignUpConfirmPassword.setText("");
        lbSignUpMessage.setText("Create Account");
    }
}
