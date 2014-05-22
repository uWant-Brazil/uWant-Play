package models.exceptions;

/**
 * Exception - Problemas na autenticação do usuário por inconsistências.
 */
public class AuthenticationException extends UWException {

    private static final int CODE = 2;
    private static final String MESSAGE = "O usuário ou senha informado é inválido.";

    public AuthenticationException() {
        super(CODE, MESSAGE);
    }

}
