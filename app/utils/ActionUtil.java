package utils;

import models.classes.Action;
import models.classes.User;

/**
 * Classe utilitária para ações relacionadas a ações tomadas pelos usuários.
 */
public abstract class ActionUtil {

    public static String generateMessage(Action action) throws IllegalAccessException {
        switch (action.getType()) {
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

    private static String shareMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("compartilhou a sua ação.");

        return builder.toString();
    }

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

    private static String addFriendsCircleMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("deseja adicionar você a seu círculo de amigos.");

        return builder.toString();
    }

    private static String acceptedFriendsCircleMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("acaba de aceitar que você participe do seu círculo de amigos.");

        return builder.toString();
    }

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

    private static String wantActionMessage(Action action) {
        StringBuilder builder = new StringBuilder();

        User from = action.getFrom();
        builder.append(from.getName());
        builder.append(" ");
        builder.append("acaba de 'wantar' sua ação!");

        return builder.toString();
    }

}
