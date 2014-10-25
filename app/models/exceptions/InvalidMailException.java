package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas na formatação do e-mail informado.
 */
public class InvalidMailException extends UWException {

    private static final int CODE = 24;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.INVALID_MAIL);

    public InvalidMailException() {
        super(CODE, MESSAGE);
    }

}
