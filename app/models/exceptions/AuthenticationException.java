package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas na autenticação do usuário por inconsistências.
 */
public class AuthenticationException extends UWException {

    private static final int CODE = 2;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.AUTHENTICATION);

    public AuthenticationException() {
        super(CODE, MESSAGE);
    }

}
