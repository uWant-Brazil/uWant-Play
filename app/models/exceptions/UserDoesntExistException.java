package models.exceptions;

import controllers.AbstractApplication;
import play.i18n.Messages;

/**
 * Exceção quando o sistema não tem nenhum usuário cadastrado com os dados informados
 * ou então o usuário está bloqueado/excluído/etc.
 */
public class UserDoesntExistException extends UWException {

    private static final int CODE = 81;
    private static final String MESSAGE = Messages.get(AbstractApplication.MessageKey.Exception.USER_DONT_EXIST);

    public UserDoesntExistException() {
        super(CODE, MESSAGE);
    }

}
