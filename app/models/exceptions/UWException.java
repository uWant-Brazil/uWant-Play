package models.exceptions;

/**
 * Classe super de todas as exceções do sistema. Utilizado para a arquitetura de status, error e message.
 */
public class UWException extends Exception {

    private int code;
    private String message;

    public UWException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

}
