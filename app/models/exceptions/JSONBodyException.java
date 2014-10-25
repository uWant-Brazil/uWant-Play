package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas no JSON durante o envio da requisição para o sistema.
 */
public class JSONBodyException extends UWException {

    private static final int CODE = 1;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.JSON_BODY);

    public JSONBodyException() {
        super(CODE, MESSAGE);
    }

}
