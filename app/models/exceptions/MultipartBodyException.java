package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas no form para content-type multipart-data.
 */
public class MultipartBodyException extends UWException {

    private static final int CODE = 23;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.MULTIPART_BODY);

    public MultipartBodyException() {
        super(CODE, MESSAGE);
    }

}
