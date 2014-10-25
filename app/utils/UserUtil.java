package utils;

import controllers.AbstractApplication;
import models.classes.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.InvalidMailException;
import models.exceptions.UWException;
import models.exceptions.UserAlreadyExistException;
import play.i18n.Messages;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * Classe utilitária para ações relacionadas ao usuário.
 */
public abstract  class UserUtil {

    /**
     * Assunto do e-mail para confirmação do e-mail.
     */
    private static final String CONFIRM_MAIL_SUBJECT = Messages.get(AbstractApplication.MessageKey.MAIL_CONFIRMATION_SUBJECT);

    /**
     * Assunto do e-mail para confirmação do e-mail.
     */
    private static final String RECOVERY_PASSWORD_MAIL_SUBJECT = Messages.get(AbstractApplication.MessageKey.MAIL_RECOVERY_PASSWORD_SUBJECT);

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
                    new String[] { AbstractApplication.FinderKey.USER_ID, AbstractApplication.FinderKey.TYPE, AbstractApplication.FinderKey.STATUS },
                    new Object[] { user.getId(), UserMailInteraction.Type.MAIL_CONFIRMATION.ordinal(), UserMailInteraction.Status.WAITING.ordinal() });

            hash = confirmation.getHash();
        } else {
            try {
                hash = SecurityUtil.hash(UUID.randomUUID().toString());
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
                    userMailInteraction.setCreatedAt(new Date());
                    userMailInteraction.save();
            }
        }

        // TODO HTML para confirmação do e-mail do usuário.
        final String content = "Confirme seu email:<br /><br /> http://homologacao.uwant.com.br/user/confirmMail?h=" + hash + "&m=" + mail;

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
        user = finder.selectUnique(
                new String[] { AbstractApplication.FinderKey.LOGIN },
                new Object[] { login });

        if (user != null) {
            throw new UserAlreadyExistException();
        }

        user = finder.selectUnique(
                new String[] { AbstractApplication.FinderKey.MAIL },
                new Object[] { email });

        if (user != null) {
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
            hash = SecurityUtil.hash(UUID.randomUUID().toString());
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
            userMailInteraction.setCreatedAt(new Date());
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

    /**
     * Método responsável por verificar se o e-mail do usuário foi confirmado.
     * @param user - Usuário
     * @return true ou false
     */
    public static boolean isMailConfirmed(User user) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<UserMailInteraction> finder = factory.get(UserMailInteraction.class);
        UserMailInteraction confirmation = finder.selectUnique(
                new String[] { AbstractApplication.FinderKey.USER_ID, AbstractApplication.FinderKey.TYPE },
                new Object[] { user.getId(), UserMailInteraction.Type.MAIL_CONFIRMATION.ordinal() });

        return (confirmation != null && confirmation.getStatus() == UserMailInteraction.Status.DONE);
    }

    /**
     * Método responsável por informar se dois usuários são amigos.
     * Ou seja, se ambos estão em seu círculo de amigos.
     * @param meId - Usuário
     * @param youId - Usuário
     * @return level - FriendCircle.Level
     */
    public static FriendsCircle.FriendshipLevel getFriendshipLevel(long meId, long youId) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<FriendsCircle> finderCircle = factory.get(FriendsCircle.class);
        FriendsCircle friendsCircleMe = finderCircle.selectUnique(
                new String[] { AbstractApplication.FinderKey.REQUESTER_ID, AbstractApplication.FinderKey.TARGET_ID},
                new Object[] { meId, youId });

        FriendsCircle friendsCircleYou = finderCircle.selectUnique(
                new String[] { AbstractApplication.FinderKey.REQUESTER_ID, AbstractApplication.FinderKey.TARGET_ID},
                new Object[] { youId, meId });

        FriendsCircle.FriendshipLevel friendshipLevel;
        if (friendsCircleMe != null && friendsCircleYou != null) {
            // Ambos estão em seus respectivos círculo de amigos.
            friendshipLevel = FriendsCircle.FriendshipLevel.MUTUAL;
        } else if (friendsCircleMe != null) {
            // O usuário está aguardando que o outro aceite.
            friendshipLevel = FriendsCircle.FriendshipLevel.WAITING_YOU;
        } else if (friendsCircleYou != null) {
            // O outro usuário está aguardando que você aceite.
            friendshipLevel = FriendsCircle.FriendshipLevel.WAITING_ME;
        } else {
            // Nenhum dos dois são amigos.
            friendshipLevel = FriendsCircle.FriendshipLevel.NONE;
        }

        return friendshipLevel;
    }

    /** Método responsável por realizar o CRUD para que os usuários
     * possam ser adicionados aos seus círculos de amigos.
     *
     * @param user - Usuário adicionando/aceitando
     * @param factory - Fábrica de Finder's (BD)
     * @param userTarget - Usuário a ser adicionado/aceito.
     * @return true ou false, se foram amigos mútuos após essa ação.
     */
    public static boolean joinCircle(User user, FinderFactory factory, User userTarget) {
        IFinder<FriendsCircle> finderCircle = factory.get(FriendsCircle.class);
        FriendsCircle friendsCircle = finderCircle.selectUnique(
                new String[] { AbstractApplication.FinderKey.REQUESTER_ID, AbstractApplication.FinderKey.TARGET_ID},
                new Object[] { user.getId(), userTarget.getId() });

        boolean isFriends = false;
        if (friendsCircle == null) {
            FriendsCircle.Relation relation = new FriendsCircle.Relation();
            relation.setRequesterId(user.getId());
            relation.setTargetId(userTarget.getId());

            friendsCircle = new FriendsCircle();
            friendsCircle.setRelation(relation);
            friendsCircle.save();

            FriendsCircle inverseFriendsCircle = finderCircle.selectUnique(
                    new String[] { AbstractApplication.FinderKey.REQUESTER_ID, AbstractApplication.FinderKey.TARGET_ID},
                    new Object[] { userTarget.getId(), user.getId() });
            isFriends = (inverseFriendsCircle != null);
        }

        IMobileUser mobileUser;
        Action action = new Action();
        action.setCreatedAt(new Date());
        if (isFriends) {
            action.setType(Action.Type.ACCEPT_FRIENDS_CIRCLE);
            action.setFrom(user);
            action.setUser(userTarget);

            mobileUser = userTarget;
        } else {
            action.setType(Action.Type.ADD_FRIENDS_CIRCLE);
            action.setFrom(userTarget);
            action.setUser(user);

            mobileUser = user;
        }
        action.save();

        NotificationUtil.send(action, mobileUser);
        return isFriends;
    }

}
