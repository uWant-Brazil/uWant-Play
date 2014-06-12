package models.exceptions;

/**
 * Exception - Problemas na formatação do e-mail informado.
 */
public class InvalidMailException extends UWException {

    private static final int CODE = 24;
    private static final String MESSAGE = "O e-mail informado é inválido.";

    public InvalidMailException() {
        super(CODE, MESSAGE);
    }

}
