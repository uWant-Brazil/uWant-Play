package models.exceptions;

import controllers.AbstractApplication;
import models.classes.User;
import play.i18n.Messages;
import utils.UserUtil;

/**
 * Exception - Problemas no e-mail do usuário que ainda não foi confirmado.
 * O sistema poderá reenviar a confirmação caso seja solicitado.
 */
public class UnconfirmedMailException extends UWException {

    private static final int CODE = 31;
    private static final String MESSAGE1 = Messages.get(AbstractApplication.MessageKey.Exception.UNCONFIRMED_MAIL_1);
    private static final String MESSAGE2 = Messages.get(AbstractApplication.MessageKey.Exception.UNCONFIRMED_MAIL_2);

    public UnconfirmedMailException() {
        super(CODE, MESSAGE1);
    }

    public UnconfirmedMailException(User user) {
        super(CODE, MESSAGE2);
        UserUtil.confirmEmail(user, true);
    }

}
