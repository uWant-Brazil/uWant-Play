package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas quando o usuário tenta realizar algum envio de push e não possui aparelhos vinculados.
 */
public class UserWithoutMobileException extends UWException {

    private static final int CODE = 62;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.USER_WITHOUT_MOBILE);

    public UserWithoutMobileException() {
        super(CODE, MESSAGE);
    }

}
