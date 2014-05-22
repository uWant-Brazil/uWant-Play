package utils;

import models.classes.User;
import models.exceptions.AuthenticationException;
import models.exceptions.UWException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Classe utilitária para ações relacionadas ao usuário.
 */
public abstract  class UserUtil {

    /**
     * Assunto do e-mail para confirmação do e-mail.
     */
    private static final String CONFIRM_MAIL_SUBJECT = "uWant @ Confirmação de email";

    /**
     * Efetua a separação do nome completo em um array com três posições - Primeiro nome, nome do meio e último nome.
     * @param fullName
     * @return String[]
     */
    public static String[] partsOfName(String fullName) {
        String firstName, middleName, lastName;

        int firstIndex = fullName.indexOf(" ");
        int lastIndex = fullName.indexOf(" ");

        if (firstIndex != -1) {
            firstName = fullName.substring(0, firstIndex).trim();
            if (lastIndex != -1 && firstIndex != lastIndex) {
                lastName = fullName.substring(lastIndex + 1).trim();
                middleName = fullName.substring(firstIndex + 1, lastIndex).trim();
                if (middleName.equals(lastName))
                    middleName = null;
            } else {
                lastName = fullName.substring(firstIndex + 1).trim();
                middleName = null;
            }
        } else {
            firstName = fullName.trim();
            middleName = null;
            lastName = null;
        }

        return new String[] { firstName, middleName, lastName };
    }

    /**
     * Envia a solicitação de confirmação do e-mail do usuário de forma assíncrona.
     * @param user
     */
    public static void confirmEmail(User user) {
        String hash = null;
        final String mail = user.getMail();
//        try {
//            hash = MailUtil.generateHash();
//
//            UserConfirmMail userConfirmMail = new UserConfirmMail();
//            userConfirmMail.setStatus(UserConfirmMail.Status.WAITING_CONFIRMATION);
//            userConfirmMail.setHash(hash);
//            userConfirmMail.setEmail(mail);
//            userConfirmMail.setUser(user);
//            userConfirmMail.save();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } finally {
            // TODO HTML para confirmação do e-mail do usuário.
//            final String content = "Confirme seu email: http://192.168.1.36:9000/user/confirmMail?h=" + hash + "&m=" + mail;
//
//            Thread thread = new Thread() {
//
//                @Override
//                public void run() {
//                    super.run();
//                    MailUtil.send(mail, CONFIRM_MAIL_SUBJECT, content);
//                }
//            };
//            thread.start();
//        }
    }

    /**
     * Retorna se o usuário está apto para realizar determinada ação baseado em seu STATUS.
     * @param user
     * @return true
     * @throws UWException
     */
    public static boolean isAvailable(User user) throws UWException {
        User.Status status = user.getStatus();
        switch (status) {
            case ACTIVE:
            case PARTIAL_ACTIVE:
                return true;

            case BLOCKED:
            case REMOVED:
            default:
                throw new AuthenticationException();
        }
    }

}
