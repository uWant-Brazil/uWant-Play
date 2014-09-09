package utils;

import models.classes.Action;
import models.classes.User;
import models.classes.WishList;
import models.classes.WishListProduct;

import java.util.Date;
import java.util.List;

/**
 * Classe utilitária para ações relacionadas a ações tomadas pelos usuários.
 */
public abstract class ActionUtil {

    private static final char CHAR_SPACE = ' ';

    /** Método responsável por gerar a mensagem baseado no tipo da Action.class
     *
     * @param action - Ação
     * @return mensagem
     * @throws IllegalAccessException
     */
    public static String generateMessage(Action action) throws IllegalAccessException {
        switch (action.getType()) {
            case ACTIVITY:
                return activityMessage(action);

            case MESSAGE:
                return notificationMessage(action);

            case ADD_FRIENDS_CIRCLE:
                return addFriendsCircleMessage(action);

            case ACCEPT_FRIENDS_CIRCLE:
                return acceptedFriendsCircleMessage(action);

            case COMMENT:
                return commentMessage(action);

            case MENTION:
                return mentionMessage(action);

            case SHARE:
                return shareMessage(action);

            case REPORT:
                return reportActionMessage(action);

            case WANT:
                return wantActionMessage(action);

            default:
                throw new IllegalAccessException("A ação não possui nenhum tipo definido.");
        }
    }

    /** Método responsável por gerar a mensagem do tipo ACTIVITY
     *
     * @param action - Ação
     * @return mensagem
     */
    private static String activityMessage(Action action) {
        User user = action.getUser();
        WishList wishList = action.getWishList();
        List<WishListProduct> products = wishList.getWishLists();
        int size = products != null ? products.size() : 0;

        StringBuilder builder = new StringBuilder();

        builder.append(user.getName());
        builder.append(CHAR_SPACE);
        builder.append("adicionou");
        builder.append(CHAR_SPACE);
        builder.append(size);
        builder.append(CHAR_SPACE);
        builder.append(size > 1 ? ("desejos") : "desejo");
        builder.append(CHAR_SPACE);
        builder.append("na sua lista");
        builder.append(CHAR_SPACE);
        builder.append(wishList.getTitle());
        builder.append(".");

        return builder.toString();
    }

    /** Método responsável por gerar a mensagem do tipo MESSAGE
     *
     * @param action - Ação
     * @return mensagem
     */
    private static String notificationMessage(Action action) {
        return action.getExtra();
    }

    /** Método responsável por gerar a mensagem do tipo SHARE
     *
     * @param action - Ação
     * @return mensagem
     */
    private static String shareMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("compartilhou a sua ação.");

        return builder.toString();
    }

    /** Método responsável por gerar a mensagem do tipo MENTION
     *
     * @param action - Ação
     * @return mensagem
     */
    private static String mentionMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("mencionou você no post ");
        builder.append("POST_NAME"); // TODO Entidade do post...
        builder.append(".");

        return builder.toString();
    }

    /** Método responsável por gerar a mensagem do tipo COMMENT
     *
     * @param action - Ação
     * @return mensagem
     */
    private static String commentMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("acaba de comentar no post ");
        builder.append("POST_NAME"); // TODO Entidade do post...
        builder.append(".");

        return builder.toString();
    }

    /** Método responsável por gerar a mensagem do tipo ADD FRIEND
     *
     * @param action - Ação
     * @return mensagem
     */
    private static String addFriendsCircleMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("deseja adicionar você a seu círculo de amigos.");

        return builder.toString();
    }

    /** Método responsável por gerar a mensagem do tipo ACCEPT FRIEND
     *
     * @param action - Ação
     * @return mensagem
     */
    private static String acceptedFriendsCircleMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("acaba de aceitar que você participe do seu círculo de amigos.");

        return builder.toString();
    }

    /** Método responsável por gerar a mensagem do tipo REPORT
     *
     * @param action - Ação
     * @return mensagem
     */
    private static String reportActionMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User user = action.getUser();
        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("acaba de reportar a ação");
        builder.append(" ");
        builder.append("(");
        builder.append(action.getId());
        builder.append(")");
        builder.append(" realizada pelo usuário ");
        builder.append(user.getName());
        builder.append(".");

        return builder.toString();
    }

    /** Método responsável por gerar a mensagem do tipo WANT
     *
     * @param action - Ação
     * @return mensagem
     */
    private static String wantActionMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("acaba de 'wantar' sua ação!");

        return builder.toString();
    }

    public static void feed(WishList wishList) {
        Action action = new Action();
        action.setCreatedAt(new Date());
        action.setType(Action.Type.ACTIVITY);
        action.setExtra(wishList.getDescription());
        action.setUser(wishList.getUser());
        action.setFrom(wishList.getUser());
        action.save();
        action.refresh();

        WishList wishListUpdated = new WishList();
        wishListUpdated.setAction(action);
        wishListUpdated.update(wishList.getId());
    }

}
