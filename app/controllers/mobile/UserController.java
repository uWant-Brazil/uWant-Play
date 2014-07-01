package controllers.mobile;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.db.ebean.Model;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.DateUtil;
import utils.NotificationUtil;
import utils.RegexUtil;
import utils.UserUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Controlador responsável pelo tratamento de requisições mobile referentes a interações com o usuário.
 */
@Security.Authenticated(MobileAuthenticator.class)
public class UserController extends AbstractApplication {

    /**
     * Método responsável por realizar o registro do usuário no sistema.
     * @return JSON
     */
    public static Result register() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                if (body.hasNonNull(ParameterKey.LOGIN) && body.hasNonNull(ParameterKey.PASSWORD)
                        && body.hasNonNull(ParameterKey.FULL_NAME) && body.hasNonNull(ParameterKey.BIRTHDAY)
                        && body.hasNonNull(ParameterKey.GENDER) && body.hasNonNull(ParameterKey.MAIL)) {
                    String login = body.get(ParameterKey.LOGIN).asText();
                    String mail = body.get(ParameterKey.MAIL).asText();

                    if (login.isEmpty() || mail.isEmpty())
                        throw new JSONBodyException();

                    if (!RegexUtil.isValidMail(mail))
                        throw new InvalidMailException();

                    if (!UserUtil.alreadyExists(login, mail)) {
                        String password = body.get(ParameterKey.PASSWORD).asText();
                        String fullName = body.get(ParameterKey.FULL_NAME).asText();
                        String birthdayStr = body.get(ParameterKey.BIRTHDAY).asText();
                        int genderOrdinal = body.get(ParameterKey.GENDER).asInt();

                        if (password.isEmpty() || fullName.isEmpty() ||
                                (genderOrdinal != 0 && genderOrdinal != 1)
                                || birthdayStr.isEmpty())
                            throw new JSONBodyException();

                        User.Gender gender = User.Gender.values()[genderOrdinal];

                        Date birthday = null;
                        try {
                            birthday = DateUtil.parse(birthdayStr, DateUtil.DATE_PATTERN);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        boolean alreadyExists = UserUtil.alreadyExists(login, mail);
                        if (!alreadyExists) {
                            User user = new User();
                            user.setLogin(login);
                            user.setPassword(password);
                            user.setName(fullName);
                            user.setMail(mail);
                            user.setBirthday(birthday);
                            user.setGender(gender);
                            user.setStatus(User.Status.PARTIAL_ACTIVE);
                            user.setSince(new Date());
                            user.save();

                            UserUtil.confirmEmail(user, false);

                            if (body.hasNonNull(ParameterKey.SOCIAL_PROFILE)) {
                                JsonNode nodeSocial = body.get(ParameterKey.SOCIAL_PROFILE);

                                if (nodeSocial.hasNonNull(ParameterKey.TOKEN) && nodeSocial.hasNonNull(ParameterKey.SOCIAL_PROVIDER)) {
                                    String accessToken = nodeSocial.get(ParameterKey.TOKEN).asText();
                                    int providerOrdinal = nodeSocial.get(ParameterKey.SOCIAL_PROVIDER).asInt();

                                    FinderFactory factory = FinderFactory.getInstance();
                                    IFinder<SocialProfile> finderProfile = factory.get(SocialProfile.class);
                                    SocialProfile socialProfile = finderProfile.selectUnique(new String[] { FinderKey.TOKEN, FinderKey.SOCIAL_PROVIDER }, new Object[] { accessToken, providerOrdinal });
                                    if (socialProfile != null) {
                                        socialProfile.setStatus(SocialProfile.Status.ACTIVE);
                                        socialProfile.setUser(user);
                                        socialProfile.update();
                                    }
                                }
                            }

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "O usuário (" + login + ") foi registrado com sucesso.");
                        } else {
                            throw new UserAlreadyExistException();
                        }
                    } else {
                        throw new UserAlreadyExistException();
                    }
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new JSONBodyException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

    /**
     * Método responsável por 'excluir' a conta do usuário.
     * Na verdade, será adicionado uma flag de REMOVED no User.class
     * @return JSON
     */
    public static Result exclude() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null) {
                if (UserUtil.isAvailable(user)) {
                    User userChanged = new User();
                    userChanged.setStatus(User.Status.REMOVED);
                    userChanged.update(user.getId());

                    jsonResponse.put(ParameterKey.STATUS, true);
                    jsonResponse.put(ParameterKey.MESSAGE, "O usuário foi excluido com sucesso.");
                    jsonResponse.put(ParameterKey.EXCLUDE, true);
                } else {
                    throw new AuthenticationException();
                }
            } else {
                throw new TokenException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

    /**
     * Método responsável por buscar uma lista de usuários no qual
     * bate com a Query informada como parâmetro.
     * A busca ocorre para três campos do usuário que são:
     * 1. Nome
     * 2. E-mail
     * 3. Login
     * @return JSON
     */
    public static Result search() {
        ObjectNode jsonResponse = Json.newObject();
        JsonNode body = request().body().asJson();
        try {
            if (body != null) {
                User user = authenticateToken();
                if (body.hasNonNull(ParameterKey.QUERY)) {
                    FinderFactory factory = FinderFactory.getInstance();
                    IFinder<User> userFinder = factory.get(User.class);
                    Model.Finder<Long, User> finder = userFinder.getFinder();
                    
                    String query = body.get(ParameterKey.QUERY).asText();
                    ExpressionList<User> expression;
                    if (query.isEmpty()) {
                        expression = finder.where().ne(FinderKey.ID, user.getId());
                    } else {
                        expression = finder.where().or(Expr.or(Expr.like(FinderKey.LOGIN, query), Expr.like(FinderKey.MAIL, query)), Expr.like(FinderKey.NAME, query));
                    }
                    
                    if (body.hasNonNull(ParameterKey.START_INDEX) && body.hasNonNull(ParameterKey.END_INDEX)) {
                        int startIndex = body.get(ParameterKey.START_INDEX).asInt();
                        int endIndex = body.get(ParameterKey.END_INDEX).asInt();

                        if (startIndex < endIndex || startIndex > endIndex) {
                            throw new IndexOutOfBoundException();
                        }

                        expression.setFirstRow(startIndex);
                        expression.setMaxRows(endIndex - startIndex);
                    }

                    List<User> users = expression.findList();
                    JsonNode usersNode = Json.toJson(users);

                    jsonResponse.put(ParameterKey.STATUS, true);
                    jsonResponse.put(ParameterKey.MESSAGE, "A consulta foi realizada com sucesso.");
                    jsonResponse.put(ParameterKey.USERS, usersNode);
                } else {
                    throw new JSONBodyException();
                }
            } else {
                throw new JSONBodyException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

    /**
     * Método responsável por listar um usuário específico
     * baseado no parâmetro de LOGIN enviado na requisição.
     * @return JSON
     */
    public static Result list() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null && UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.hasNonNull(ParameterKey.LOGIN)) {
                    String login = body.get(ParameterKey.LOGIN).asText();

                    FinderFactory factory = FinderFactory.getInstance();
                    IFinder<User> finder = factory.get(User.class);
                    User userFounded = finder.selectUnique(
                            new String[] { FinderKey.LOGIN },
                            new Object[] { login });

                    if (userFounded != null && UserUtil.isAvailable(userFounded)) {
                        JsonNode usersNode = Json.toJson(userFounded);
                        ObjectNode perfilNode = Json.newObject();
                        perfilNode.put(ParameterKey.USER, usersNode);

                        if (user.getId() != userFounded.getId()) {
                            FriendsCircle.FriendshipLevel friendshipLevel = UserUtil.getFriendshipLevel(user, userFounded);

                            IFinder<WishList> wFinder = factory.get(WishList.class);
                            List<WishList> wishLists = wFinder.selectAll(
                                    new String[]{ FinderKey.USER_ID, FinderKey.STATUS },
                                    new Object[]{ userFounded.getId(), WishList.Status.ACTIVE.ordinal() });

                            JsonNode wishListsNode = Json.toJson(wishLists);

                            perfilNode.put(ParameterKey.FRIENDSHIP_LEVEL, friendshipLevel.ordinal());
                            perfilNode.put(ParameterKey.WISHLIST, wishListsNode);
                        }

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "A consulta do perfil foi realizada com sucesso.");
                        jsonResponse.put(ParameterKey.PERFIL, perfilNode);
                    } else {
                        throw new UserDoesntExistException();
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
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

    /**
     * Método responsável por adicionar ou aceitar o usuário
     * em seu círculo de amigos.
     * @return JSON
     */
    public static Result joinCircle() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null && UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.hasNonNull(ParameterKey.LOGIN)) {
                    String login = body.get(ParameterKey.LOGIN).asText();

                    FinderFactory factory = FinderFactory.getInstance();
                    IFinder<User> finder = factory.get(User.class);
                    User userTarget = finder.selectUnique(
                            new String[] { FinderKey.LOGIN },
                            new Object[] { login });

                    if (userTarget != null && UserUtil.isAvailable(userTarget)) {
                        IFinder<FriendsCircle> finderCircle = factory.get(FriendsCircle.class);
                        FriendsCircle friendsCircle = finderCircle.selectUnique(
                                new String[] { FinderKey.REQUESTER_ID, FinderKey.TARGET_ID},
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
                                    new String[] { FinderKey.REQUESTER_ID, FinderKey.TARGET_ID},
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

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "O usuário " + userTarget.getLogin() + " foi solicitado como amigo.");
                        jsonResponse.put(ParameterKey.FRIENDS, isFriends);
                    } else {
                        throw new UserDoesntExistException();
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
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

    /**
     * Método responsável por remover o usuário do seu
     * círculo de amigos. Além disso, também é capaz de desfazer
     * a solicitação de amizade que ainda não foi aceita pelo usuário.
     *
     * OBS: Uma vez desfeita a solicitação, o usuário que está com a
     * notificação de amizade não poderá mais aceita-la. Por isso, não
     * é interessante disponibilizar este recurso enquanto não vinculamos
     * uma ação de adição ao círculo de amigos.
     * @return JSON
     */
    public static Result leaveCircle() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null && UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.hasNonNull(ParameterKey.LOGIN)) {
                    String login = body.get(ParameterKey.LOGIN).asText();

                    FinderFactory factory = FinderFactory.getInstance();
                    IFinder<User> finder = factory.get(User.class);
                    User userTarget = finder.selectUnique(
                            new String[] { FinderKey.LOGIN },
                            new Object[] { login });

                    if (userTarget != null && UserUtil.isAvailable(userTarget)) {
                        IFinder<FriendsCircle> finderCircle = factory.get(FriendsCircle.class);
                        FriendsCircle friendsCircle = finderCircle.selectUnique(
                                new String[] { FinderKey.REQUESTER_ID, FinderKey.TARGET_ID},
                                new Object[] { user.getId(), userTarget.getId() });

                        if (friendsCircle != null) {
                            friendsCircle.delete();
                        }

                        friendsCircle = finderCircle.selectUnique(
                                new String[] { FinderKey.REQUESTER_ID, FinderKey.TARGET_ID},
                                new Object[] { userTarget.getId(), user.getId() });

                        if (friendsCircle != null) {
                            friendsCircle.delete();
                        }

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "O usuário " + userTarget.getLogin() + " foi removido como amigo.");
                    } else {
                        throw new UserDoesntExistException();
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
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
        }
        return ok(jsonResponse);
    }

}
