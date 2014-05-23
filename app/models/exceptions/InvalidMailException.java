package models.exceptions;

/**
 * Created by felipebonezi on 22/05/14.
 */
public class InvalidMailException extends UWException {

    private static final int CODE = 2;
    private static final String MESSAGE = "O e-mail informado é inválido.";

    public InvalidMailException() {
        super(CODE, MESSAGE);
    }

}
