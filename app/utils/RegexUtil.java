package utils;

import java.util.regex.Pattern;

/**
 * Created by felipebonezi on 22/05/14.
 */
public abstract class RegexUtil {

    private static final String MAIL_REGEX = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";

    public static boolean isValidMail(String mail) {
        return mail.matches(MAIL_REGEX);
    }

}
