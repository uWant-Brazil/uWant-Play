package utils;

/**
 * Classe utilitária para ações relacionadas a utilização de Regex.
 */
public abstract class RegexUtil {

    /**
     * Regex responsável por validar uma String com formatação de e-mail.
     */
    private static final String MAIL_REGEX = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

    /**
     * Método responsável por validar uma String com formatação de e-mail.
     */
    public static boolean isValidMail(String mail) {
        return mail.matches(MAIL_REGEX);
    }

}
