package models.exceptions;

/**
 * Exception - Problemas na autenticação do usuário por inconsistências.
 */
public class IndexOutOfBoundException extends UWException {

    private static final int CODE = 23;
    private static final String MESSAGE = "Os índices informados estão incorretos.";

    public IndexOutOfBoundException() {
        super(CODE, MESSAGE);
    }

}
