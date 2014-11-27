package models.cloud.forms;

import play.data.validation.Constraints;

public class UserAuthenticationViewModel {

    @Constraints.Required
    @Constraints.MinLength(4)
    private String login;

    @Constraints.Required
    private String password;

    public UserAuthenticationViewModel() {
    }

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
