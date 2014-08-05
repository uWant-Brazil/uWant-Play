package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.NotificationUtil;
import utils.UserUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controlador responsável pelas requisições mobile relacionadas a ações realizadas por usuários no sistema.
 */
@Security.Authenticated(MobileAuthenticator.class)
public class ActionController extends AbstractApplication {

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

                        Action action = finder.selectUnique(actionId);
                        User userAction = action.getUser();

                        Want want = new Want();
                        want.setUser(user);
                        want.setAction(action);
                        want.save();

                        Action actionComment = new Action();
                        actionComment.setCreatedAt(new Date());
                        actionComment.setFrom(user);
                        actionComment.setUser(userAction);
                        actionComment.setType(Action.Type.WANT);
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
