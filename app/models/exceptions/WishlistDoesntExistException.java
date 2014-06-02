package models.exceptions;

/**
 * Created by Cleibson Gomes on 01/06/14.
 */
public class WishlistDoesntExistException extends UWException {

    private static final int CODE = 10;
    private static final String MESSAGE = "Essa lista não existe";

    public WishlistDoesntExistException() {
        super(CODE, MESSAGE);
    }

}
