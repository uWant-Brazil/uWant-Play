package models.exceptions;

/**
 * Exception - Problemas no JSON durante o envio da requisição para o sistema.
 */
public class JSONBodyException extends UWException {

    private static final int CODE = 1;
    private static final String MESSAGE = "Ocorreu um problema no body do seu cabeçalho HTTP -> application/json.";

    public JSONBodyException() {
        super(CODE, MESSAGE);
    }

}
