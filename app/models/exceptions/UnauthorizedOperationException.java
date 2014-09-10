package models.exceptions;

public class UnauthorizedOperationException extends UWException {

    private static final int CODE = 501;
    private static final String MESSAGE = "Você não tem permissão para realizar essa operação.";

    public UnauthorizedOperationException() {
        super(CODE, MESSAGE);
    }

}
