package models.exceptions;

/**
 * Exception - Problemas na formatação da data informada.
 */
public class InvalidDateException extends UWException {

    private static final int CODE = 42;
    private static final String MESSAGE = "A data informada é inválida.";

    public InvalidDateException() {
        super(CODE, MESSAGE);
    }

}
