package models.exceptions;

/**
 * Created by felipebonezi on 24/05/14.
 */
public class UserDoesntExistException extends UWException {

    private static final int CODE = 81;
    private static final String MESSAGE = "O usuario não consta em nossos registros.";

    public UserDoesntExistException() {
        super(CODE, MESSAGE);
    }

}
