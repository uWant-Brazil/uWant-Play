package utils;

import models.classes.Action;
import models.classes.User;

/**
 * Created by felipebenezi on 20/06/14.
 */
public abstract class ActionUtil {

    public static String generateMessage(Action action) throws IllegalAccessException {
        switch (action.getType()) {
            case ADD_FRIENDS_CIRCLE:
                return addFriendsCircleMessage(action);

            case COMMENT:
                return commentMessage(action);

            case MENTION:
                return mentionMessage(action);

            case SHARE:
                return shareMessage(action);

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

}
