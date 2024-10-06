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
            lbSignUpMessage.setText("Bitte geben Sie eine E-Mail-Adresse ein.");
            return;
        }

        String password = pfSignUpPassword.getText().trim();
        if (password.equals("")) {
            lbSignUpMessage.setText("Bitte geben Sie ein Passwort ein.");
            return;
        }

        if (!password.equals(pfSignUpConfirmPassword.getText())) {
            lbSignUpMessage.setText("Die Passwörter stimmen nicht überein.");
            return;
        }

        if (account.verifyAccount(email)) {
            lbSignUpMessage.setText("Ein Konto mit dieser E-Mail existiert bereits.");
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
        String email = tfUsername.getText();
        String password = pfLoginPassword.getText();

        if (account.verifyPassword(email, password)) {
            tabPane.getTabs().get(0).setDisable(true);
            tabPane.getTabs().get(1).setDisable(true);
            tabPane.getTabs().get(2).setDisable(false);
            tabPane.getSelectionModel().select(2);
        } else {
            lbLoginMessage.setText("Ungültige E-Mail oder Passwort.");
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
        lbLoginMessage.setText("Melden Sie sich mit Ihrem Konto an.");
    }

    private void resetSignup() {
        tfSignUpEmail.setText("");
        pfSignUpPassword.setText("");
        pfSignUpConfirmPassword.setText("");
        lbSignUpMessage.setText("Konto erstellen");
    }
}
