package utils;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import java.util.Date;

/**
 * Classe utilitária para ações relacionadas a entidades de data.
 */
public abstract class DateUtil {

    /**
     * Padrão de data brasileira.
     */
    public static final String DATE_PATTERN = "dd/MM/yyyy";

    /**
     * Padrão de hora brasileira.
     */
    public static final String HOUR_PATTERN = "HH:mm:ss";

    /**
     * Padrão de data/hora brasileira.
     */
    public static final String DATE_HOUR_PATTERN = "dd/MM/yyyy HH:mm:ss";

    /**
     * Lista de todos os padrões brasileiros.
     */
    private static final String[] PATTERNS = { DATE_PATTERN, HOUR_PATTERN, DATE_HOUR_PATTERN };

    /**
     * Responsável por transformar uma string em padrão brasileiro para uma entidade Date.
     * @param dateHour
     * @return
     * @throws DateParseException
     */
    public static Date parse(String dateHour) throws DateParseException {
        return DateUtils.parseDate(dateHour, PATTERNS);
    }

}
