package models.exceptions;

/**
 * Exception - Problemas no acesso a uma lista de desejos inexistente.
 */
public class WishListDontExistException extends UWException {

    private static final int CODE = 10;
    private static final String MESSAGE = "A lista de desejos não consta em nossos registros.";

    public WishListDontExistException() {
        super(CODE, MESSAGE);
    }

}
