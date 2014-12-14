package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas na formatação da data informada.
 */
public class InvalidEntityException extends UWException {

    private static final int CODE = 42;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.INVALID_ENTITY);

    public InvalidEntityException() {
        super(CODE, MESSAGE);
    }

}
