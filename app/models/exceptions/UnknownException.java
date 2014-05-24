package models.exceptions;

/**
 * Exception - Problema desconhecido ou sem tratamento pelo sistema.
 */
public class UnknownException extends UWException {

    private static final int CODE = -999;
    private static final String MESSAGE = "Ocorreu um problema inesperado, entre em contato com o suporte.";

    public UnknownException() {
        super(CODE, MESSAGE);
    }

}
