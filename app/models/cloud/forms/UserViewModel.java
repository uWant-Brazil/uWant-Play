package models.cloud.forms;

import models.classes.User;
import play.data.format.Formats;
import play.data.validation.Constraints;
import utils.DateUtil;

import java.util.Date;

public class UserViewModel {

    @Constraints.Required
    @Constraints.MinLength(4)
    private String login;

    @Constraints.Required
    private String password;

    @Constraints.Required
    private String name;

    @Constraints.Required
    @Constraints.Email
    private String mail;

    @Constraints.Required
    @Formats.DateTime(pattern = DateUtil.DATE_PATTERN)
    private Date birthday;

    public UserViewModel() {
    }

    public UserViewModel(User user) {
        this.login = user.getLogin();
        this.name = user.getName();
        this.mail = user.getMail();
        this.birthday = user.getBirthday();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}
