package models.exceptions;

/**
 * Exception - Exceção caso você tente bloquear um usuário que não está no seu
 * círculo de amigos.
 */
public class UnavailableBlockFriend extends UWException {

    private static final int CODE = 75;
    private static final String MESSAGE = "Você não pode bloquear um usuário que não é nem seu amigo ainda.";

    public UnavailableBlockFriend() {
        super(CODE, MESSAGE);
    }

}
