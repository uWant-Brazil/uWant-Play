package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exception - Problemas no acesso a uma lista de desejos inexistente.
 */
public class WishListDoesntExistException extends UWException {

    private static final int CODE = 10;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.WISHLIST_DONT_EXIST);

    public WishListDoesntExistException() {
        super(CODE, MESSAGE);
    }

}
