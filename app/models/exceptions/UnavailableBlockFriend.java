package models.exceptions;

public class UnavailableBlockFriend extends UWException {

    private static final int CODE = 75;
    private static final String MESSAGE = "Você não pode bloquear um usuário que não é nem seu amigo ainda.";

    public UnavailableBlockFriend() {
        super(CODE, MESSAGE);
    }

}
