package models.cloud.forms;

import models.classes.User;

public class UserViewModel {

    private final long id;
    private final String login;
    private final String name;
    private final MultimediaViewModel picture;

    public UserViewModel(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.name = user.getName();
        this.picture = new MultimediaViewModel(user.getPicture());
    }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getName() {
        return name;
    }

    public MultimediaViewModel getPicture() {
        return picture;
    }

}
