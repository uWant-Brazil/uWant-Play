package controllers.mobile;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controlador responsável pelas requisições mobile relacionadas a ações realizadas por usuários no sistema.
 */
@Security.Authenticated(MobileAuthenticator.class)
public class ActionController extends AbstractApplication {

    /**
     * Método responsável por listar todos os feeds de ações realizadas
     * pelos amigos não-bloqueados, lista de desejos ou usuários espeíficios para o usuário autenticado.
     * @return JSON
     */
    public static F.Promise<Result> feeds() {
        return F.Promise.<Result>promise(() -> {
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

                        List<ObjectNode> actions;
                        if (body.hasNonNull(ParameterKey.USER_ID)) {
                            long userId = body.get(ParameterKey.USER_ID).asLong(Long.MIN_VALUE);
                            if (userId == 0)
                                userId = user.getId();

                            actions = ActionUtil.listUserFeeds(user, userId, startIndex, endIndex);
                        } else if (body.hasNonNull(ParameterKey.WISHLIST_ID)) {
                            long wishListId = body.get(ParameterKey.WISHLIST_ID).asLong(Long.MIN_VALUE);
                            actions = ActionUtil.listWishListFeeds(user, wishListId, startIndex, endIndex);
                        } else {
                            actions = ActionUtil.listFriendsFeeds(user, startIndex, endIndex);
                        }

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.Action.FEEDS_SUCCESS));
                        jsonResponse.put(ParameterKey.ACTIONS, Json.toJson(actions));
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
        });
    }

    /**
     * Método responsável por vincular um novo comentário a uma ação
     * compartilhada/criada por um usuário.
     * @return JSON
     */
    public static F.Promise<Result> comment() {
        return F.Promise.<Result>promise(() -> {
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

                            NotificationUtil.send(actionComment, userAction);

                            action.refresh();

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.Action.COMMENT_SUCCESS));
                            jsonResponse.put(ParameterKey.COMMENTS, Json.toJson(action.getComments()));
                            jsonResponse.put(ParameterKey.ACTION, ActionUtil.getFeed(factory, action, user));
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
        });
    }

    /**
     * Mëtodo responsável por listar todos os comentários de uma determinada ação realizada por algum usuário.
     * @return JSON
     */
    public static F.Promise<Result> listComments() {
        return F.Promise.promise(() -> {
            final ObjectNode jsonResponse = Json.newObject();
            try {
                User user = authenticateToken();
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null && body.has(ParameterKey.ACTION_ID)) {
                        final long actionId = body.get(ParameterKey.ACTION_ID).asLong(0);
                        if (actionId > 0) {
                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<Action> finder = factory.get(Action.class);

                            Action action = finder.selectUnique(actionId);
                            List<Comment> comments = action.getComments();

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.Action.COMMENTS_SUCCESS, (comments != null ? comments.size() : 0)));
                            jsonResponse.put(ParameterKey.COMMENTS, Json.toJson(comments));
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
            return ok(jsonResponse);
        });
    }

    /**
     * Método responsável por 'wantar' uma ação compartilhada/criada por um usuário.
     * @return JSON
     */
    public static F.Promise<Result> want() {
        return F.Promise.<Result>promise(() -> {
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

                                NotificationUtil.send(actionWant, userAction);
                            } else {
                                want.delete();
                            }

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.Action.WANT_SUCCESS));
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
        });
    }

    /**
     * Método responsável por 'reportar' uma ação compartilhada/criada por um usuário.
     * @return JSON
     */
    public static F.Promise<Result> report() {
        return F.Promise.<Result>promise(() -> {
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
                                users = new ArrayList<>(5);
                                users.add(user);

                                actionReport = new ActionReport();
                                actionReport.setAction(action);
                                actionReport.setUsers(users);
                                actionReport.setSince(new Date());
                                actionReport.save();
                            }

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.Action.REPORT_SUCCESS));
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
        });
    }

    /**
     * Método responsável por 'bloquear' as ações de um determinado usuário para não serem
     * exibidas no seu feed de atividades.
     * @return JSON
     */
    public static F.Promise<Result> toggleBlock() {
        return F.Promise.promise(() -> {
            final ObjectNode jsonResponse = Json.newObject();
            try {
                final User user = authenticateToken();
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null && body.hasNonNull(ParameterKey.LOGIN)) {
                        final String login = body.get(ParameterKey.LOGIN).asText();
                        if (!login.isEmpty()) {
                            final FinderFactory factory = FinderFactory.getInstance();
                            final IFinder<User> finder = factory.get(User.class);
                            final User userFriend = finder.selectUnique(
                                    new String[]{FinderKey.LOGIN},
                                    new Object[]{login});

                            if (userFriend != null && UserUtil.isAvailable(userFriend)) {
                                FriendsCircle.FriendshipLevel level = UserUtil.getFriendshipLevel(user.getId(), userFriend.getId());
                                if (level == FriendsCircle.FriendshipLevel.MUTUAL) {
                                    IFinder<FriendsCircle> finderFC = factory.get(FriendsCircle.class);
                                    FriendsCircle friendsCircle = finderFC.selectUnique(
                                            new String[]{FinderKey.REQUESTER_ID, FinderKey.TARGET_ID},
                                            new Object[]{user.getId(), userFriend.getId()});

                                    friendsCircle.setBlocked(!friendsCircle.isBlocked());
                                    friendsCircle.update();

                                    jsonResponse.put(ParameterKey.STATUS, true);
                                    jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.Action.TOGGLE_BLOCK_SUCCESS, login));
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

            return ok(jsonResponse);
        });
    }

    /**
     * Método responsável por contar os 'compartilhamentos' as ações de um determinado usuário.
     * @return JSON.
     */
    public static F.Promise<Result> share() {
        return F.Promise.<Result>promise(() -> {
            final ObjectNode jsonResponse = Json.newObject();
            try {
                final User user = authenticateToken();
                if (UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null && body.has(ParameterKey.ACTION_ID)) {
                        final long actionId = body.get(ParameterKey.ACTION_ID).asLong(0);
                        if (actionId > 0) {
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

                                NotificationUtil.send(actionShare, userAction);

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.Action.SHARE_SUCCESS));
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
        });
    }

}
