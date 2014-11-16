package utils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.UserDoesntExistException;
import models.exceptions.WishListDoesntExistException;
import play.i18n.Messages;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe utilitária para ações relacionadas a ações tomadas pelos usuários.
 */
public abstract class ActionUtil {

    /**
     * SQL para obter os amigos do usuário logado para que seja listado o feed de notícias.
     */
    private static final String CONST_LIST_FRIENDS_FEEDS_SQL = "SELECT id AS target_id FROM users WHERE id IN (SELECT fc1.target_id FROM friends_circle fc1 INNER JOIN friends_circle fc2 ON fc2.target_id = fc1.requester_id AND fc2.requester_id = fc1.target_id INNER JOIN users u ON u.id = fc1.target_id WHERE fc1.requester_id = :user_id AND fc1.is_blocked = false) AND status IN (:status_1,:status_2)";

    private static final char CHAR_SPACE = ' ';
    private static final char CHAR_DOT = '.';
    private static final char CHAR_OPEN = '(';
    private static final char CHAR_CLOSE = ')';

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
        builder.append(Messages.get(AbstractApplication.MessageKey.ADDED));
        builder.append(CHAR_SPACE);
        builder.append(size);
        builder.append(CHAR_SPACE);
        builder.append(size > 1 ? Messages.get(AbstractApplication.MessageKey.WISHES) : Messages.get(AbstractApplication.MessageKey.WISH));
        builder.append(CHAR_SPACE);
        builder.append(Messages.get(AbstractApplication.MessageKey.IN_YOUR_LIST));
        builder.append(CHAR_SPACE);
        builder.append(wishList.getTitle());
        builder.append(CHAR_DOT);

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
        builder.append(CHAR_SPACE);
        builder.append(Messages.get(AbstractApplication.MessageKey.SHARE_YOUR_ACTION));

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
        builder.append(CHAR_SPACE);
        builder.append(Messages.get(AbstractApplication.MessageKey.MENTION_YOU));
        builder.append(CHAR_DOT);

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
        builder.append(CHAR_SPACE);
        builder.append(Messages.get(AbstractApplication.MessageKey.COMMENT));
        builder.append(CHAR_DOT);

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
        builder.append(CHAR_SPACE);
        builder.append(Messages.get(AbstractApplication.MessageKey.ADD_CIRCLE));

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
        builder.append(CHAR_SPACE);
        builder.append(Messages.get(AbstractApplication.MessageKey.ACCEPT_CIRCLE));

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
        builder.append(CHAR_SPACE);
        builder.append(Messages.get(AbstractApplication.MessageKey.REPORT_1));
        builder.append(CHAR_SPACE);
        builder.append(CHAR_OPEN);
        builder.append(action.getId());
        builder.append(CHAR_CLOSE);
        builder.append(CHAR_SPACE);
        builder.append(Messages.get(AbstractApplication.MessageKey.REPORT_2));
        builder.append(CHAR_SPACE);
        builder.append(user.getName());
        builder.append(CHAR_DOT);

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
        builder.append(CHAR_SPACE);
        builder.append(Messages.get(AbstractApplication.MessageKey.WANT));

        return builder.toString();
    }

    /**
     * Método responsável por gerar adicionar um novo feed para a lista de desejos.
     * @param wishList
     */
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

    /**
     * Método responsável por listar o feed de notícias de um determinado usuário.
     * @param user - Usuário logado
     * @param userId - Usuário a listar feed
     * @param startIndex
     * @param endIndex
     * @return feed em formato JSON.
     * @throws UserDoesntExistException
     */
    public static List<ObjectNode> listUserFeeds(final User user, final long userId,
                                                  final int startIndex, final int endIndex) throws UserDoesntExistException {
        if (UserUtil.getFriendshipLevel(user.getId(), userId) == FriendsCircle.FriendshipLevel.MUTUAL) {
            Object[] targetIds = new Object[1];
            targetIds[0] = userId;

            return getUserFeeds(targetIds, startIndex, endIndex, user);
        } else {
            throw new UserDoesntExistException();
        }
    }

    /**
     * Método responsável por listar o feed de notícias de uma determinada lista de desejos.
     * @param user - Usuário logado
     * @param wishListId - Lista de desejos a listar feed
     * @param startIndex
     * @param endIndex
     * @return feed em formato JSON.
     * @throws UserDoesntExistException
     */
    public static List<ObjectNode> listWishListFeeds(final User user, final long wishListId,
                                                      final int startIndex, final int endIndex) throws WishListDoesntExistException {
        final FinderFactory factory = FinderFactory.getInstance();
        final IFinder<WishList> finder = factory.get(WishList.class);
        final WishList wishList = finder.selectUnique(wishListId);
        final User owner = wishList.getUser();
        final Action action = wishList.getAction();

        if (WishListUtil.isOwner(wishList, user)
                || UserUtil.getFriendshipLevel(user.getId(), owner.getId()) == FriendsCircle.FriendshipLevel.MUTUAL) {
            List<ObjectNode> nodes = null;
            ObjectNode node = getFeed(factory, action, user);
            if (node != null) {
                nodes = new ArrayList<>();
                nodes.add(node);
            }
            return nodes;
        } else {
            throw new WishListDoesntExistException();
        }
    }

    /**
     * Método responsável por listar o feed de notícias do usuário logado.
     * @param user - Usuário logado
     * @param startIndex
     * @param endIndex
     * @return feed em formato JSON.
     * @throws UserDoesntExistException
     */
    public static List<ObjectNode> listFriendsFeeds(final User user, final int startIndex, final int endIndex) {
        SqlQuery query = Ebean.createSqlQuery(CONST_LIST_FRIENDS_FEEDS_SQL);
        query.setParameter(AbstractApplication.FinderKey.USER_ID, user.getId());
        query.setParameter(AbstractApplication.FinderKey.STATUS_1, User.Status.ACTIVE.ordinal());
        query.setParameter(AbstractApplication.FinderKey.STATUS_2, User.Status.PARTIAL_ACTIVE.ordinal());

        List<ObjectNode> actionsNode = null;
        List<SqlRow> rows = query.findList();
        if (rows != null && rows.size() > 0) {
            Object[] targetIds = new Object[rows.size()];
            for (int i = 0; i < rows.size(); i++) {
                SqlRow row = rows.get(i);
                Long targetId = row.getLong(AbstractApplication.FinderKey.TARGET_ID);
                targetIds[i] = targetId;
            }

            actionsNode = getUserFeeds(targetIds, startIndex, endIndex, user);
        }

        return actionsNode;
    }

    /**
     * Método responsável por listar os feeds de uma lista de usuários pré-selecionados.
     * @param targetIds - Usuários selecionados
     * @param startIndex
     * @param endIndex
     * @param user - Usuário logado
     * @return
     */
    private static List<ObjectNode> getUserFeeds(Object[] targetIds, int startIndex, int endIndex, User user) {
        List<ObjectNode> actionsNode = new ArrayList<>((endIndex - startIndex) + 5);
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<Action> finderActions = factory.get(Action.class);
        List<Action> actions = finderActions.getFinder()
                .where()
                .in(AbstractApplication.FinderKey.USER_ID, targetIds)
                .eq(AbstractApplication.FinderKey.TYPE, Action.Type.ACTIVITY.ordinal())
                .setFirstRow(startIndex)
                .setMaxRows(endIndex - startIndex)
                .orderBy(String.format("%s desc", AbstractApplication.FinderKey.CREATED_AT))
                .findList();

        for (Action action : actions) {
            ObjectNode node = getFeed(factory, action, user);
            actionsNode.add(node);
        }
        return actionsNode;
    }

    /**
     * Obtém o feed para o determinado usuário.
     * @param factory
     * @param action
     * @param user
     * @return
     */
    public static ObjectNode getFeed(FinderFactory factory, Action action, User user) {
        long id = action.getId();
        String message = action.toString();
        WishList wishList = action.getWishList();
        List<WishListProduct> wishListProducts = wishList.getWishLists();

        IFinder<Want> finderWants = factory.get(Want.class);
        int wantsCount = finderWants.getFinder()
                .where()
                .eq(AbstractApplication.FinderKey.ACTION_ID, id)
                .findRowCount();
        boolean uWant = finderWants.selectUnique(
                new String[] { AbstractApplication.FinderKey.ACTION_ID, AbstractApplication.FinderKey.USER_ID },
                new Object[] { action.getId(), user.getId() })
                != null;

        IFinder<Comment> finderComments = factory.get(Comment.class);
        int commentsCount = finderComments.getFinder()
                .where()
                .eq(AbstractApplication.FinderKey.ACTION_ID, id)
                .findRowCount();

        IFinder<ActionShare> finderShares = factory.get(ActionShare.class);
        int sharesCount = finderShares.getFinder()
                .where()
                .eq(AbstractApplication.FinderKey.ACTION_ID, id)
                .findRowCount();
        boolean uShare = finderShares.selectAll(
                new String[] { AbstractApplication.FinderKey.ACTION_ID, AbstractApplication.FinderKey.USER_ID },
                new Object[] { action.getId(), user.getId() })
                != null;

        List<Product> nodesProducts = new ArrayList<>();
        if (wishListProducts != null) {
            for (WishListProduct product : wishListProducts) {
                if (product.getStatus() == WishListProduct.Status.ACTIVE) {
                    Multimedia multimedia = product.getProduct().getMultimedia();
                    if (multimedia != null) {
                        nodesProducts.add(product.getProduct());
                    }
                }
            }
        }

        ObjectNode nodeWishList = Json.newObject();
        nodeWishList.put(AbstractApplication.ParameterKey.ID, wishList.getId());
        nodeWishList.put(AbstractApplication.ParameterKey.PRODUCTS, Json.toJson(nodesProducts));

        User actionUser = action.getUser();
        ObjectNode nodeUser = Json.newObject();
        nodeUser.put(AbstractApplication.ParameterKey.LOGIN, actionUser.getLogin());
        nodeUser.put(AbstractApplication.ParameterKey.PICTURE, Json.toJson(actionUser.getPicture()));

        ObjectNode nodeWant = Json.newObject();
        nodeWant.put(AbstractApplication.ParameterKey.COUNT, wantsCount);
        nodeWant.put(AbstractApplication.ParameterKey.UWANT, uWant);

        ObjectNode nodeShare = Json.newObject();
        nodeShare.put(AbstractApplication.ParameterKey.COUNT, sharesCount);
        nodeShare.put(AbstractApplication.ParameterKey.USHARE, uShare);

        ObjectNode node = Json.newObject();
        node.put(AbstractApplication.ParameterKey.ID, id);
        node.put(AbstractApplication.ParameterKey.TYPE, action.getType().ordinal());
        node.put(AbstractApplication.ParameterKey.WHEN, DateUtil.format(action.getCreatedAt(), DateUtil.DATE_HOUR_PATTERN));
        node.put(AbstractApplication.ParameterKey.MESSAGE, message);
        node.put(AbstractApplication.ParameterKey.EXTRA, action.getExtra());
        node.put(AbstractApplication.ParameterKey.WISHLIST, nodeWishList);
        node.put(AbstractApplication.ParameterKey.USER_FROM, nodeUser);
        node.put(AbstractApplication.ParameterKey.COMMENTS_COUNT, commentsCount);
        node.put(AbstractApplication.ParameterKey.WANT, nodeWant);
        node.put(AbstractApplication.ParameterKey.SHARE, nodeShare);
        return node;
    }

    /**
     * Método responsável por obter a lista de comentários de uma determinada ação.
     * @param user - Usuário logado
     * @param actionId - Id da Ação
     * @param startIndex
     * @param endIndex
     * @param factory
     * @return
     */
    public static List<ObjectNode> getActionComments(User user, long actionId, int startIndex, int endIndex, FinderFactory factory) {
        List<ObjectNode> nodeComments = new ArrayList<>();
        IFinder<Comment> finder = factory.get(Comment.class);
        IFinder<Want> finderWants = factory.get(WantComment.class);

        List<Comment> comments = finder.getFinder()
                .where()
                .eq(AbstractApplication.FinderKey.ACTION_ID, actionId)
                .setFirstRow(startIndex)
                .setMaxRows(endIndex - startIndex)
                .order(String.format("%s desc", AbstractApplication.FinderKey.SINCE))
                .findList();

        for (Comment comment : comments) {
            long id = comment.getId();

            int wantsCount = finderWants.getFinder()
                    .where()
                    .eq(AbstractApplication.FinderKey.COMMENT_ID, id)
                    .findRowCount();

            boolean uWant = finderWants.selectUnique(
                    new String[] { AbstractApplication.FinderKey.COMMENT_ID, AbstractApplication.FinderKey.USER_ID },
                    new Object[] { id, user.getId() })
                    != null;

            ObjectNode nodeComment = Json.newObject();
            nodeComment.put(AbstractApplication.ParameterKey.ID, comment.getId());
            nodeComment.put(AbstractApplication.ParameterKey.TEXT, comment.getText());
            nodeComment.put(AbstractApplication.ParameterKey.SINCE, DateUtil.format(comment.getSince(), DateUtil.DATE_HOUR_PATTERN));
            nodeComment.put(AbstractApplication.ParameterKey.USER, Json.toJson(comment.getUser()));

            ObjectNode node = Json.newObject();
            node.put(AbstractApplication.ParameterKey.COMMENT, nodeComment);
            node.put(AbstractApplication.ParameterKey.UWANT, uWant);
            node.put(AbstractApplication.ParameterKey.COUNT, wantsCount);

            nodeComments.add(node);
        }
        return nodeComments;
    }

}
