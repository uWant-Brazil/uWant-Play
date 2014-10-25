package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

public class SocialProfileAlreadyExistException extends UWException {

    private static final int CODE = 543;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.SOCIAL_PROFILE);

    public SocialProfileAlreadyExistException() {
        super(CODE, MESSAGE);
    }

}
