package models.exceptions;

/**
 * Exception - Problemas para criação ou verificação de um usuário no sistema.
 */
public class UserAlreadyExistException extends UWException {

    private static final int CODE = 8;
    private static final String MESSAGE = "Já existe um usuário com o login ou email informado cadastrado em nosso sistema.";

    public UserAlreadyExistException() {
        super(CODE, MESSAGE);
    }

}
