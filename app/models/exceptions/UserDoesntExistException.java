package models.exceptions;

/**
 * Created by felipebonezi on 24/05/14.
 */
public class UserDoesntExistException extends UWException {

    private static final int CODE = 8;
    private static final String MESSAGE = "Este usuario nao existe.";

    public UserDoesntExistException() {
        super(CODE, MESSAGE);
    }

}
