package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Exceção caso você tente bloquear um usuário que não está no seu
 * círculo de amigos.
 */
public class UnavailableBlockFriend extends UWException {

    private static final int CODE = 75;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.UNAVAILABLE_BLOCK_FRIEND);

    public UnavailableBlockFriend() {
        super(CODE, MESSAGE);
    }

}
