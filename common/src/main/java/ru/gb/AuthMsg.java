package ru.gb;

public class AuthMsg extends AbstractMsg {

    private final String login;
    private final String password;
    private String userDir;
    private boolean loginSuccessful;

    public AuthMsg(String login, String password, AbstractMsgTypes type) {
        super(type);
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getUserDir() {
        return userDir;
    }

    public void setLoginSuccessful(String userDir) {
        if (userDir != null) {
            this.userDir = userDir;
            loginSuccessful = true;
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

}
