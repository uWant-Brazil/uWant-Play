package models.exceptions;

/**
 * Exception - Problemas no index acesso pela sistema (Paginação).
 */
public class IndexOutOfBoundException extends UWException {

    private static final int CODE = 26;
    private static final String MESSAGE = "Os índices informados estão incorretos.";

    public IndexOutOfBoundException() {
        super(CODE, MESSAGE);
    }

}
