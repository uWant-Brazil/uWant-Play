package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas no index acesso pela sistema (Paginação).
 */
public class IndexOutOfBoundException extends UWException {

    private static final int CODE = 26;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.INDEX_OUT_OF_BOUNDS);

    public IndexOutOfBoundException() {
        super(CODE, MESSAGE);
    }

}
