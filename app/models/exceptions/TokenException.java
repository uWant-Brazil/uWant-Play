package models.exceptions;

/**
 * Exception - Problemas no token que está sendo enviado. Necessita uma nova autenticação no sistema.
 */
public class TokenException extends UWException {

    private static final int CODE = 3;
    private static final String MESSAGE = "O token é inválido ou inexistente.";

    public TokenException() {
        super(CODE, MESSAGE);
    }

}