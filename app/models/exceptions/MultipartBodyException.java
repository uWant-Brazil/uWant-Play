package models.exceptions;

/**
 * Exception - Problemas no form para content-type multipart-data.
 */
public class MultipartBodyException extends UWException {

    private static final int CODE = 23;
    private static final String MESSAGE = "Ocorreu um problema no body do seu cabeçalho HTTP -> application/multipart.";

    public MultipartBodyException() {
        super(CODE, MESSAGE);
    }

}
