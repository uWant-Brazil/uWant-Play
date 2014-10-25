package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas para criação ou verificação de um usuário no sistema.
 */
public class UserAlreadyExistException extends UWException {

    private static final int CODE = 8;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.USER_EXIST);

    public UserAlreadyExistException() {
        super(CODE, MESSAGE);
    }

}
