package models.cloud.models;

import play.data.validation.Constraints;

public class AuthenticationViewModel {

    @Constraints.Required
    @Constraints.MinLength(4)
    private String login;

    @Constraints.Required
    @Constraints.MinLength(6)
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
