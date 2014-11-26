package models.cloud.forms;

import play.data.validation.Constraints;

public class RecoveryPasswordViewModel {

    @Constraints.Required
    @Constraints.Email
    private String mail;

    public RecoveryPasswordViewModel() {
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
