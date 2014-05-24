package models.exceptions;

import models.classes.User;
import utils.UserUtil;

/**
 * Created by felipebonezi on 24/05/14.
 */
public class UnconfirmedMailException extends UWException {

    private static final int CODE = 3;
    private static final String MESSAGE1 = "O e-mail do usuario ainda nao foi confirmado.";
    private static final String MESSAGE2 = MESSAGE1 + " Uma nova confirmacao foi enviada!";

    public UnconfirmedMailException() {
        super(CODE, MESSAGE1);
    }

    public UnconfirmedMailException(User user) {
        super(CODE, MESSAGE2);
        UserUtil.confirmEmail(user);
    }

}
