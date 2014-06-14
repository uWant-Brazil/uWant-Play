package utils;

import controllers.AbstractApplication;
import models.classes.SocialProfile;
import models.classes.User;
import models.classes.UserMailInteraction;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.InvalidMailException;
import models.exceptions.UWException;
import models.exceptions.UserAlreadyExistException;

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
     * Assunto do e-mail para confirmação do e-mail.
     */
    private static final String RECOVERY_PASSWORD_MAIL_SUBJECT = "uWant @ Recuperação de senha";

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
    public static void confirmEmail(User user, boolean isRecurrent) {
        String hash = null;
        String mail = user.getMail();
        if (isRecurrent) {
            FinderFactory factory = FinderFactory.getInstance();
            IFinder<UserMailInteraction> finder = factory.get(UserMailInteraction.class);
            UserMailInteraction confirmation = finder.selectUnique(
                    new String[] { AbstractApplication.FinderKey.USER_ID, AbstractApplication.FinderKey.TYPE },
                    new Object[] { user.getId(), UserMailInteraction.Type.MAIL_CONFIRMATION.ordinal() });

            hash = confirmation.getHash();
        } else {
            try {
                hash = MailUtil.generateHash();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } finally {
                    UserMailInteraction userMailInteraction = new UserMailInteraction();
                    userMailInteraction.setStatus(UserMailInteraction.Status.WAITING);
                    userMailInteraction.setType(UserMailInteraction.Type.MAIL_CONFIRMATION);
                    userMailInteraction.setHash(hash == null ? String.valueOf(System.currentTimeMillis()) : hash);
                    userMailInteraction.setMail(mail);
                    userMailInteraction.setUser(user);
                    userMailInteraction.save();

            }
        }

        // TODO HTML para confirmação do e-mail do usuário.
        final String content = "Confirme seu email:<br /><br /> http://homologacao.uwant.com.br/user/confirmMail?ts=" + System.currentTimeMillis() + "&h=" + hash + "&m=" + mail;

        try {
            MailUtil.send(mail, CONFIRM_MAIL_SUBJECT, content);
        } catch (InvalidMailException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna se o usuário está apto para realizar determinada ação baseado em seu STATUS.
     * @param user
     * @return true
     * @throws UWException
     */
    public static boolean isAvailable(User user) {
        User.Status status = user.getStatus();
        switch (status) {
            case ACTIVE:
            case PARTIAL_ACTIVE:
                return true;

            case BLOCKED:
            case REMOVED:
            default:
                return false;
        }
    }

    /**
     * Método responsável por verificar se já existe algum usuário com o login ou e-mail informado.
     * @param login - Login do usuário
     * @param email - E-mail do usuário
     * @return true or false
     * @throws UserAlreadyExistException
     */
    public static boolean alreadyExists(String login, String email) throws UserAlreadyExistException {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<User> finder = factory.get(User.class);

        User user;
        user = finder.selectUnique(new String[] { AbstractApplication.FinderKey.LOGIN }, new Object[] { login });
        if (user != null) {
            throw new UserAlreadyExistException();
        }

        user = finder.selectUnique(new String[] { AbstractApplication.FinderKey.MAIL }, new Object[] { email });
        if (user != null) {
            throw new UserAlreadyExistException();
        }

        IFinder<SocialProfile.Login> finderSocial = factory.get(SocialProfile.Login.class);
        SocialProfile.Login socialLogin = finderSocial.selectUnique(new String[] { AbstractApplication.FinderKey.LOGIN }, new Object[] { email });
        if (socialLogin != null && socialLogin.getProfile().getStatus() == SocialProfile.Status.ACTIVE) {
            throw new UserAlreadyExistException();
        }

        return false;
    }

    /**
     * Método responsável por disparar o e-mail para efetuar a recuperação da senha do usuário.
     * @param user - Entidade do usuário
     */
    public static void recoveryPassword(User user) {
        String hash = null;
        final String mail = user.getMail();
        try {
            hash = MailUtil.generateHash();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            UserMailInteraction userMailInteraction = new UserMailInteraction();
            userMailInteraction.setStatus(UserMailInteraction.Status.WAITING);
            userMailInteraction.setType(UserMailInteraction.Type.RECOVERY_PASSWORD);
            userMailInteraction.setHash(hash == null ? String.valueOf(System.currentTimeMillis()) : hash);
            userMailInteraction.setMail(mail);
            userMailInteraction.setUser(user);
            userMailInteraction.save();

            // TODO HTML para confirmação do e-mail do usuário.
            final String content = "Recupere sua senha:<br /><br /> http://homologacao.uwant.com.br/user/recoveryPassword?ts=" + System.currentTimeMillis() + "&h=" + hash + "&m=" + mail;

            try {
                MailUtil.send(mail, RECOVERY_PASSWORD_MAIL_SUBJECT, content);
            } catch (InvalidMailException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isMailConfirmed(User user) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<UserMailInteraction> finder = factory.get(UserMailInteraction.class);
        UserMailInteraction confirmation = finder.selectUnique(
                new String[] { AbstractApplication.FinderKey.USER_ID, AbstractApplication.FinderKey.TYPE },
                new Object[] { user.getId(), UserMailInteraction.Type.MAIL_CONFIRMATION.ordinal() });

        return (confirmation != null && confirmation.getStatus() == UserMailInteraction.Status.DONE);
    }
}
