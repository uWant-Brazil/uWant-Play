package models.exceptions;

/**
 * Exception - Problemas no index acesso pela sistema (Paginação).
 */
public class UserWithoutMobileException extends UWException {

    private static final int CODE = 62;
    private static final String MESSAGE = "O usuário não possui nenhum dispositivo móvel vinculado ao seu usuário.";

    public UserWithoutMobileException() {
        super(CODE, MESSAGE);
    }

}
