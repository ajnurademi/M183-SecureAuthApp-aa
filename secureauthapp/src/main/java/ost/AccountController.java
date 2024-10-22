package ost;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

public class AccountController {

    private Account account;
    private int loginAttempts = 0;
    private long lockTime = 0;
    private static final long LOCK_DURATION = 20000; 

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
        if (email.isEmpty()) {
            lbSignUpMessage.setText("Please enter an email address.");
            return;
        }

        String password = pfSignUpPassword.getText().trim();
        if (password.equals("")) {
            lbSignUpMessage.setText("Please enter a password.");
            return;
        }

        if (!password.equals(pfSignUpConfirmPassword.getText())) {
            lbSignUpMessage.setText("The passwords do not match.");
            return;
        }

        if (account.verifyAccount(email)) {
            lbSignUpMessage.setText("An account with this email already exists.");
            return;
        }

        try {
            account.addAccount(email, password);
            resetLogin();
            resetSignup();
            tabPane.getSelectionModel().select(1);
        } catch (Exception e) {
            lbSignUpMessage.setText(e.getMessage());  
        }
    }

    @FXML
    private void onLogin(ActionEvent event) {
        if (isLocked()) {
            long timeRemaining = (lockTime + LOCK_DURATION) - System.currentTimeMillis();
            lbLoginMessage.setText("Your account is locked. Please wait " + (timeRemaining / 1000) + " seconds.");
            return;
        }

        String email = tfUsername.getText();
        String password = pfLoginPassword.getText();

        if (account.verifyPassword(email, password)) {
            loginAttempts = 0; 
            tabPane.getTabs().get(0).setDisable(true);
            tabPane.getTabs().get(1).setDisable(true);
            tabPane.getTabs().get(2).setDisable(false);
            tabPane.getSelectionModel().select(2);
        } else {
            loginAttempts++;
            int remainingAttempts = 3 - loginAttempts;
            
            if (remainingAttempts > 0) {
                lbLoginMessage.setText("Invalid email or password. You have " + remainingAttempts + " attempt(s) left.");
            } else {
                lockTime = System.currentTimeMillis();
                lbLoginMessage.setText("Too many failed login attempts. Please wait 20 seconds.");
            }
        }
    }

    private boolean isLocked() {
        return loginAttempts >= 3 && (System.currentTimeMillis() < (lockTime + LOCK_DURATION));
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
        lbLoginMessage.setText("Please log in with your account.");
    }

    private void resetSignup() {
        tfSignUpEmail.setText("");
        pfSignUpPassword.setText("");
        pfSignUpConfirmPassword.setText("");
        lbSignUpMessage.setText("Create an account");
    }
}
