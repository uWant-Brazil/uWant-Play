package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problema desconhecido ou sem tratamento pelo sistema.
 */
public class UnknownException extends UWException {

    private static final int CODE = -998;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.UNKNOWN);

    public UnknownException() {
        super(CODE, MESSAGE);
    }

}
