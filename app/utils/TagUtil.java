package utils;

import controllers.AbstractApplication;
import models.classes.Action;
import models.classes.FriendsCircle;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe utilitária para auxiliar o envio de notificações caso o usuário do sistema tenha realizado marcações
 * em outros usuários, realizando uma conexão com o mesmo.
 */
public abstract class TagUtil {

    /**
     * Classe estática responsável pelo REGEX para identificação de citações nos usuários.
     */
    private static final Pattern USER_TAG = Pattern.compile("(<uwt id='\\d'>(?:(?!<\\/uwt>).)*.*?<\\/uwt>)");

    /**
     * Análise de citações nos usuários em um determinado texto. Caso exista, o usuário citado será notificado!
     * @param from - Quem citou
     * @param text
     */
    public static void verifyAndNotify(User from, String text) {
        List<User> taggedUsers = extractUsersFromTags(from, text);
        if (taggedUsers != null && taggedUsers.size() > 0) {
            for (User to : taggedUsers) {
                Action tagAction = ActionUtil.createTag(from, to);
                NotificationUtil.send(tagAction, to);
            }
        }
    }

    /**
     * Método responsável por extrair todos os usuários citados a partir de um determinado texto.
     * Note que apenas usuários com nível de AMIZADE MÚTUA seram mapeados para notificação!
     * @param text
     * @return
     */
    private static List<User> extractUsersFromTags(User from, String text) {
        List<User> taggedUsers = null;

        Matcher matcher = USER_TAG.matcher(text);
        if (matcher != null) {
            taggedUsers = new ArrayList<>(5);
            FinderFactory factory = FinderFactory.getInstance();
            IFinder<User> finder = factory.get(User.class);

            while (matcher.find()) {
                String tagged = matcher.group(1);
                long id = Long.parseLong(tagged.substring(tagged.indexOf("'") + 1, tagged.lastIndexOf("'")));
                String login = tagged.substring(tagged.indexOf("@") + 1, tagged.indexOf("</uwt>")).trim();

                User to = finder.selectUnique(
                        new String[] {AbstractApplication.FinderKey.ID, AbstractApplication.FinderKey.LOGIN},
                        new Object[] {id, login});

                if (to != null && UserUtil.isAvailable(to)) {
                    FriendsCircle.FriendshipLevel level = UserUtil.getFriendshipLevel(from.getId(), to.getId());
                    if (level == FriendsCircle.FriendshipLevel.MUTUAL) {
                        taggedUsers.add(to);
                    }
                }
            }
        }

        return taggedUsers;
    }

}
