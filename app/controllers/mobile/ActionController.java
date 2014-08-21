package controllers.mobile;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.db.ebean.Model;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.DateUtil;
import utils.NotificationUtil;
import utils.UserUtil;

import java.util.*;

/**
 * Controlador responsável pelas requisições mobile relacionadas a ações realizadas por usuários no sistema.
 */
@Security.Authenticated(MobileAuthenticator.class)
public class ActionController extends AbstractApplication {

    /**
     * Método responsável por listar todos os feeds de ações realizadas
     * pelos amigos não-bloqueados do usuário autenticado.
     * @return JSON
     */
    public static F.Promise<Result> feeds() {
        final ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.hasNonNull(ParameterKey.START_INDEX)) {
                    final int startIndex = body.get(ParameterKey.START_INDEX).asInt(0);
                    final int endIndex;
                    if (body.hasNonNull(ParameterKey.END_INDEX)) {
                        endIndex = body.get(ParameterKey.END_INDEX).asInt(startIndex + 10);
                    } else {
                        endIndex = startIndex + 10;
                    }

                    F.Promise<List<ObjectNode>> promise = F.Promise.promise(new F.Function0<List<ObjectNode>>() {

                        @Override
                        public List<ObjectNode> apply() throws Throwable {
                            // FIXME Como utilizar inner join com o Finder?
                            SqlQuery query = Ebean.createSqlQuery("SELECT id AS target_id " +
                                    "FROM users " +
                                    "WHERE id IN " +
                                    "(SELECT fc1.target_id FROM friends_circle fc1 " +
                                    "INNER JOIN friends_circle fc2 ON fc2.target_id = fc1.requester_id AND fc2.requester_id = fc1.target_id " +
                                    "INNER JOIN users u ON u.id = fc1.target_id " +
                                    "WHERE fc1.requester_id = " + user.getId() + " AND fc1.is_blocked = false) " +
                                    "AND status = 0");

                            List<ObjectNode> actionsNode = null;
                            List<SqlRow> rows = query.findList();
                            if (rows != null && rows.size() > 0) {
                                actionsNode = new ArrayList<ObjectNode>(rows.size() + 5);

                                Object[] targetIds = new Object[rows.size()];
                                for (int i = 0; i < rows.size(); i++) {
                                    SqlRow row = rows.get(i);
                                    Long targetId = row.getLong(FinderKey.TARGET_ID);
                                    targetIds[i] = targetId;
                                }

                                FinderFactory factory = FinderFactory.getInstance();
                                IFinder<Action> finderActions = factory.get(Action.class);
                                List<Action> actions = finderActions.getFinder()
                                        .where()
                                        .in(FinderKey.USER_ID, targetIds)
                                        .eq(FinderKey.TYPE, Action.Type.ACTIVITY.ordinal())
                                        .setFirstRow(startIndex)
                                        .setMaxRows(endIndex - startIndex)
                                        .orderBy(FinderKey.CREATED_AT + " desc")
                                        .findList();

                                for (Action action : actions) {
                                    long id = action.getId();
                                    String message = action.toString();
                                    WishList wishList = action.getWishList();
                                    List<WishListProduct> wishListProducts = wishList.getWishLists();

                                    IFinder<Want> finderWants = factory.get(Want.class);
                                    int wantsCount = finderWants.getFinder()
                                            .where()
                                            .eq(FinderKey.ACTION_ID, id)
                                            .findRowCount();
                                    boolean uWant = finderWants.selectUnique(
                                            new String[] { FinderKey.ACTION_ID, FinderKey.USER_ID },
                                            new Object[] { action.getId(), user.getId() })
                                            != null;

                                    IFinder<Comment> finderComments = factory.get(Comment.class);
                                    int commentsCount = finderComments.getFinder()
                                            .where()
                                            .eq(FinderKey.ACTION_ID, id)
                                            .findRowCount();

                                    IFinder<ActionShare> finderShares = factory.get(ActionShare.class);
                                    int sharesCount = finderShares.getFinder()
                                            .where()
                                            .eq(FinderKey.ACTION_ID, id)
                                            .findRowCount();
                                    boolean uShare = finderShares.selectAll(
                                            new String[] { FinderKey.ACTION_ID, FinderKey.USER_ID },
                                            new Object[] { action.getId(), user.getId() })
                                            != null;

                                    List<Multimedia> nodesProducts = new ArrayList<Multimedia>();
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
                                    nodeWishList.put(ParameterKey.ID, wishList.getId());
                                    nodeWishList.put(ParameterKey.MULTIMEDIAS, Json.toJson(nodesProducts));

                                    User actionUser = action.getUser();
                                    ObjectNode nodeUser = Json.newObject();
                                    nodeUser.put(ParameterKey.LOGIN, actionUser.getLogin());
                                    nodeUser.put(ParameterKey.PICTURE, Json.toJson(actionUser.getPicture()));

                                    ObjectNode nodeWant = Json.newObject();
                                    nodeWant.put(ParameterKey.COUNT, wantsCount);
                                    nodeWant.put(ParameterKey.UWANT, uWant);

                                    ObjectNode nodeShare = Json.newObject();
                                    nodeShare.put(ParameterKey.COUNT, sharesCount);
                                    nodeShare.put(ParameterKey.USHARE, uShare);

                                    ObjectNode node = Json.newObject();
                                    node.put(ParameterKey.ID, id);
                                    node.put(ParameterKey.TYPE, action.getType().ordinal());
                                    node.put(ParameterKey.WHEN, DateUtil.format(action.getCreatedAt(), DateUtil.DATE_HOUR_PATTERN));
                                    node.put(ParameterKey.MESSAGE, message);
                                    node.put(ParameterKey.EXTRA, action.getExtra());
                                    node.put(ParameterKey.WISHLIST, nodeWishList);
                                    node.put(ParameterKey.USER_FROM, nodeUser);
                                    node.put(ParameterKey.COMMENTS_COUNT, commentsCount);
                                    node.put(ParameterKey.WANT, nodeWant);
                                    node.put(ParameterKey.SHARE, nodeShare);

                                    actionsNode.add(node);
                                }
                            }

                            return actionsNode;
                        }

                    });

                    return promise.map(new F.Function<List<ObjectNode>, Result>() {

                        @Override
                        public Result apply(List<ObjectNode> actions) throws Throwable {
                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "As ações dos seus amigos foram listadas com sucesso.");
                            jsonResponse.put(ParameterKey.ACTIONS, Json.toJson(actions));

                            return ok(jsonResponse);
                        }

                    });
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new AuthenticationException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return F.Promise.promise(new F.Function0<Result>() {

            @Override
            public Result apply() throws Throwable {
                return ok(jsonResponse);
            }

        });
    }

    /**
     * Método responsável por vincular um novo comentário a uma ação
     * compartilhada/criada por um usuário.
     * @return JSON
     */
    public static Result comment() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.has(ParameterKey.ACTION_ID)
                        && body.has(ParameterKey.COMMENT)) {
                    long actionId = body.get(ParameterKey.ACTION_ID).asLong(0);
                    String commentText = body.get(ParameterKey.COMMENT).asText();

                    if (actionId > 0 && !commentText.isEmpty()) {
                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<Action> finder = factory.get(Action.class);

                        Action action = finder.selectUnique(actionId);
                        User userAction = action.getUser();

                        Comment comment = new Comment();
                        comment.setSince(new Date());
                        comment.setAction(action);
                        comment.setText(commentText);
                        comment.setUser(user);
                        comment.save();

                        Action actionComment = new Action();
                        actionComment.setCreatedAt(new Date());
                        actionComment.setFrom(user);
                        actionComment.setUser(userAction);
                        actionComment.setType(Action.Type.COMMENT);
                        actionComment.save();

                        IMobileUser mobileUser = userAction;
                        NotificationUtil.send(actionComment, mobileUser);

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "O comentário foi adicionado com sucesso.");
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new AuthenticationException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return ok(jsonResponse);
    }

    /**
     * Mëtodo responsável por listar todos os comentários de uma determinada ação realizada por algum usuário.
     * @return JSON
     */
    public static F.Promise<Result> listComments() {
        final ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.has(ParameterKey.ACTION_ID)) {
                    long actionId = body.get(ParameterKey.ACTION_ID).asLong(0);
                    if (actionId > 0) {
                        F.Promise<List<Comment>> promise = F.Promise.promise(new F.Function0<List<Comment>>() {

                            @Override
                            public List<Comment> apply() throws Throwable {
                                FinderFactory factory = FinderFactory.getInstance();
                                IFinder<Action> finder = factory.get(Action.class);

                                Action action = finder.selectUnique(actionId);
                                List<Comment> comments = action.getComments();
                                return comments;
                            }

                        });

                        return promise.map(new F.Function<List<Comment>, Result>() {

                            @Override
                            public Result apply(List<Comment> comments) throws Throwable {
                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "Foram listados " + (comments != null ? comments.size() : 0) + " comentários com sucesso.");
                                jsonResponse.put(ParameterKey.COMMENTS, Json.toJson(comments));

                                return ok(jsonResponse);
                            }

                        });
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new UserDoesntExistException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }
        return F.Promise.promise(new F.Function0<Result>() {

            @Override
            public Result apply() throws Throwable {
                return ok(jsonResponse);
            }

        });
    }

    /**
     * Método responsável por 'wantar' uma ação compartilhada/criada por um usuário.
     * @return JSON
     */
    public static Result want() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.has(ParameterKey.ACTION_ID)) {
                    long actionId = body.get(ParameterKey.ACTION_ID).asLong(0);

                    if (actionId > 0) {
                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<Action> finder = factory.get(Action.class);
                        IFinder<Want> finderWant = factory.get(Want.class);

                        Action action = finder.selectUnique(actionId);
                        User userAction = action.getUser();

                        Want want = finderWant.selectUnique(
                                new String[] { FinderKey.ACTION_ID, FinderKey.USER_ID },
                                new Object[] { actionId, user.getId() });

                        if (want == null) {
                            want = new Want();
                            want.setUser(user);
                            want.setAction(action);
                            want.save();

                            Action actionWant = new Action();
                            actionWant.setCreatedAt(new Date());
                            actionWant.setFrom(user);
                            actionWant.setUser(userAction);
                            actionWant.setType(Action.Type.WANT);
                            actionWant.save();

                            IMobileUser mobileUser = userAction;
                            NotificationUtil.send(actionWant, mobileUser);
                        } else {
                            want.delete();
                        }

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "O feed foi 'wantado ou não' com sucesso.");
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new AuthenticationException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return ok(jsonResponse);
    }

    /**
     * Método responsável por 'reportar' uma ação compartilhada/criada por um usuário.
     * @return JSON
     */
    public static Result report() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.hasNonNull(ParameterKey.ACTION_ID)) {
                    long actionId = body.get(ParameterKey.ACTION_ID).asLong(0);

                    if (actionId > 0) {
                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<Action> finder = factory.get(Action.class);
                        IFinder<ActionReport> finderReport = factory.get(ActionReport.class);

                        Action action = finder.selectUnique(actionId);
                        ActionReport actionReport = finderReport.selectUnique(
                                new String[] { FinderKey.ACTION_ID },
                                new Object[] { actionId });

                        List<User> users;
                        if (actionReport != null) {
                            users = actionReport.getUsers();
                            users.add(user);

                            ActionReport report = new ActionReport();
                            report.setUsers(users);
                            report.update(actionId);
                        } else {
                            users = new ArrayList<User>(5);
                            users.add(user);

                            actionReport = new ActionReport();
                            actionReport.setAction(action);
                            actionReport.setUsers(users);
                            actionReport.setSince(new Date());
                            actionReport.save();
                        }

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "A denúncia foi registrada com sucesso.");
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new AuthenticationException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return ok(jsonResponse);
    }

    /**
     * Método responsável por 'bloquear' as ações de um determinado usuário para não serem
     * exibidas no seu feed de atividades.
     * @return JSON
     */
    public static F.Promise<Result> toggleBlock() {
        final ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.hasNonNull(ParameterKey.LOGIN)) {
                    String login = body.get(ParameterKey.LOGIN).asText();
                    if (!login.isEmpty()) {
                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<User> finder = factory.get(User.class);
                        User userFriend = finder.selectUnique(
                                new String[] { FinderKey.LOGIN },
                                new Object[] { login });

                        if (userFriend != null && UserUtil.isAvailable(userFriend)) {
                            FriendsCircle.FriendshipLevel level = UserUtil.getFriendshipLevel(user, userFriend);
                            if (level == FriendsCircle.FriendshipLevel.MUTUAL) {
                                F.Promise<String> promise = F.Promise.promise(new F.Function0<String>() {

                                    @Override
                                    public String apply() throws Throwable {
                                        IFinder<FriendsCircle> finderFC = factory.get(FriendsCircle.class);
                                        FriendsCircle friendsCircle = finderFC.selectUnique(
                                                new String[] { FinderKey.REQUESTER_ID, FinderKey.TARGET_ID },
                                                new Object[] { user.getId(), userFriend.getId() });

                                        friendsCircle.setBlocked(!friendsCircle.isBlocked());
                                        friendsCircle.update();

                                        return login;
                                    }

                                });

                                return promise.map(new F.Function<String, Result>() {

                                    @Override
                                    public Result apply(String s) throws Throwable {
                                        jsonResponse.put(ParameterKey.STATUS, true);
                                        jsonResponse.put(ParameterKey.MESSAGE, "O usuário (" + login + ") foi bloqueado com sucesso!");

                                        return ok(jsonResponse);
                                    }

                                });
                            } else {
                                throw new UnavailableBlockFriend();
                            }
                        } else {
                            throw new UserDoesntExistException();
                        }
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new UserDoesntExistException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }
        return F.Promise.promise(new F.Function0<Result>() {

            @Override
            public Result apply() throws Throwable {
                return ok(jsonResponse);
            }

        });
    }

    /**
     * Método responsável por contar os 'compartilhamentos' as ações de um determinado usuário.
     * @return JSON.
     */
    public static F.Promise<Result> share() {
        final ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.has(ParameterKey.ACTION_ID)) {
                    long actionId = body.get(ParameterKey.ACTION_ID).asLong(0);
                    if (actionId > 0) {
                        F.Promise<JsonNode> promise = F.Promise.promise(new F.Function0<JsonNode>() {

                            @Override
                            public JsonNode apply() throws Throwable {
                                FinderFactory factory = FinderFactory.getInstance();
                                IFinder<Action> finder = factory.get(Action.class);

                                Action action = finder.selectUnique(actionId);
                                User userAction = action.getUser();

                                ActionShare share = new ActionShare();
                                share.setUser(user);
                                share.setAction(action);
                                share.save();

                                Action actionShare = new Action();
                                actionShare.setCreatedAt(new Date());
                                actionShare.setFrom(user);
                                actionShare.setUser(userAction);
                                actionShare.setType(Action.Type.SHARE);
                                actionShare.save();

                                IMobileUser mobileUser = userAction;
                                NotificationUtil.send(actionShare, mobileUser);

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, "O compartilhamento foi efetuado com sucesso.");
                                return jsonResponse;
                            }

                        });

                        return promise.map(new F.Function<JsonNode, Result>() {

                            @Override
                            public Result apply(JsonNode jsonNode) throws Throwable {
                                return ok(jsonNode);
                            }

                        });
                    } else {
                        throw new JSONBodyException();
                    }
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new AuthenticationException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return F.Promise.promise(new F.Function0<Result>() {

            @Override
            public Result apply() throws Throwable {
                return ok(jsonResponse);
            }

        });
    }

}
