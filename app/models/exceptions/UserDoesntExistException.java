package models.exceptions;

/**
 * Exceção quando o sistema não tem nenhum usuário cadastrado com os dados informados
 * ou então o usuário está bloqueado/excluído/etc.
 */
public class UserDoesntExistException extends UWException {

    private static final int CODE = 81;
    private static final String MESSAGE = "O usuario não consta em nossos registros.";

    public UserDoesntExistException() {
        super(CODE, MESSAGE);
    }

}
