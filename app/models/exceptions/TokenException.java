package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas no token que está sendo enviado. Necessita uma nova autenticação no sistema.
 */
public class TokenException extends UWException {

    private static final int CODE = 3;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.TOKEN);

    public TokenException() {
        super(CODE, MESSAGE);
    }

}