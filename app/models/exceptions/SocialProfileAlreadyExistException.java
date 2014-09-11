package models.exceptions;

public class SocialProfileAlreadyExistException extends UWException {

    private static final int CODE = 543;
    private static final String MESSAGE = "Já existe uma conta vinculada a essa rede social.";

    public SocialProfileAlreadyExistException() {
        super(CODE, MESSAGE);
    }

}
