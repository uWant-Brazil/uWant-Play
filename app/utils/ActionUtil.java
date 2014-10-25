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
import models.exceptions.WishListDontExistException;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe utilitária para ações relacionadas a ações tomadas pelos usuários.
 */
public abstract class ActionUtil {

    private static final String CONST_LIST_FRIENDS_FEEDS_SQL = "SELECT id AS target_id FROM users WHERE id IN (SELECT fc1.target_id FROM friends_circle fc1 INNER JOIN friends_circle fc2 ON fc2.target_id = fc1.requester_id AND fc2.requester_id = fc1.target_id INNER JOIN users u ON u.id = fc1.target_id WHERE fc1.requester_id = :user_id AND fc1.is_blocked = false) AND status = 0";

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

    public static List<ObjectNode> listWishListFeeds(final User user, final long wishListId,
                                                      final int startIndex, final int endIndex) throws WishListDontExistException {
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
            throw new WishListDontExistException();
        }
    }

    public static List<ObjectNode> listFriendsFeeds(final User user, final int startIndex, final int endIndex) {
        SqlQuery query = Ebean.createSqlQuery(CONST_LIST_FRIENDS_FEEDS_SQL);
        query.setParameter(0, user.getId());

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
                .orderBy(AbstractApplication.FinderKey.CREATED_AT + " desc")
                .findList();

        for (Action action : actions) {
            ObjectNode node = getFeed(factory, action, user);
            actionsNode.add(node);
        }
        return actionsNode;
    }

    private static ObjectNode getFeed(FinderFactory factory, Action action, User user) {
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

        List<Multimedia> nodesProducts = new ArrayList<>();
        if (wishListProducts != null) {
            for (WishListProduct product : wishListProducts) {
                if (product.getStatus() == WishListProduct.Status.ACTIVE) {
                    Multimedia multimedia = product.getProduct().getMultimedia();
                    if (multimedia != null) {
                        nodesProducts.add(multimedia);
                    }
                }
            }
        }

        ObjectNode nodeWishList = Json.newObject();
        nodeWishList.put(AbstractApplication.ParameterKey.ID, wishList.getId());
        nodeWishList.put(AbstractApplication.ParameterKey.MULTIMEDIAS, Json.toJson(nodesProducts));

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


}
