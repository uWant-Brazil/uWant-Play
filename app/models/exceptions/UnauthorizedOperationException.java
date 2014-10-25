package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

public class UnauthorizedOperationException extends UWException {

    private static final int CODE = 501;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.UNAUTHORIZED_OPERATION);

    public UnauthorizedOperationException() {
        super(CODE, MESSAGE);
    }

}
