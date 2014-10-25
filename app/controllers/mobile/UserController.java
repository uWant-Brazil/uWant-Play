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
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controlador responsável pelo tratamento de requisições mobile referentes a interações com o usuário.
 */
public class UserController extends AbstractApplication {

    /**
     * Método responsável por realizar o registro do usuário no sistema.
     * @return JSON
     */
    public static F.Promise<Result> register() {
        return F.Promise.<Result>promise(() -> {
            ObjectNode jsonResponse = Json.newObject();
            try {
                JsonNode body = request().body().asJson();
                if (body != null) {
                    if (body.hasNonNull(ParameterKey.LOGIN)
                            && body.hasNonNull(ParameterKey.PASSWORD)
                            && body.hasNonNull(ParameterKey.FULL_NAME)
                            && body.hasNonNull(ParameterKey.BIRTHDAY)
                            && body.hasNonNull(ParameterKey.GENDER)
                            && body.hasNonNull(ParameterKey.MAIL)) {
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
                                user.setPassword(SecurityUtil.md5(password));
                                user.setName(fullName);
                                user.setMail(mail);
                                user.setBirthday(birthday);
                                user.setGender(gender);
                                user.setStatus(User.Status.PARTIAL_ACTIVE);
                                user.setSince(new Date());
                                user.save();
                                user.refresh();

                                UserUtil.confirmEmail(user, false);

                                if (body.hasNonNull(ParameterKey.SOCIAL_PROFILE)) {
                                    JsonNode nodeSocial = body.get(ParameterKey.SOCIAL_PROFILE);

                                    if (nodeSocial.hasNonNull(ParameterKey.TOKEN)
                                            && nodeSocial.hasNonNull(ParameterKey.SOCIAL_PROVIDER)) {
                                        String accessToken = nodeSocial.get(ParameterKey.TOKEN).asText();
                                        int providerOrdinal = nodeSocial.get(ParameterKey.SOCIAL_PROVIDER).asInt();

                                        FinderFactory factory = FinderFactory.getInstance();
                                        IFinder<SocialProfile> finderProfile = factory.get(SocialProfile.class);
                                        SocialProfile socialProfile = finderProfile.selectUnique(
                                                new String[]{FinderKey.TOKEN, FinderKey.SOCIAL_PROVIDER},
                                                new Object[]{accessToken, providerOrdinal});

                                        if (socialProfile != null) {
                                            socialProfile.setStatus(SocialProfile.Status.ACTIVE);
                                            socialProfile.setUser(user);
                                            socialProfile.update();
                                        }
                                    }
                                }

                                generateToken(user, Token.Target.MOBILE);

                                jsonResponse.put(ParameterKey.STATUS, true);
                                jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.User.REGISTER_SUCCESS));
                                jsonResponse.put(ParameterKey.USER, Json.toJson(user));
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
        });
    }

    /**
     * Método responsável por 'excluir' a conta do usuário.
     * Na verdade, será adicionado uma flag de REMOVED no User.class
     * @return JSON
     */
    @Security.Authenticated(MobileAuthenticator.class)
    public static F.Promise<Result> exclude() {
        return F.Promise.<Result>promise(() -> {
            ObjectNode jsonResponse = Json.newObject();
            try {
                User user = authenticateToken();
                if (user != null) {
                    if (UserUtil.isAvailable(user)) {
                        User userChanged = new User();
                        userChanged.setStatus(User.Status.REMOVED);
                        userChanged.update(user.getId());

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.User.EXCLUDE_SUCCESS));
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
        });
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
    @Security.Authenticated(MobileAuthenticator.class)
    public static F.Promise<Result> search() {
        return F.Promise.<Result>promise(() -> {
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
                            expression = finder.where()
                                    .or(Expr.or(
                                            Expr.icontains(FinderKey.LOGIN, query),
                                            Expr.icontains(FinderKey.MAIL, query)),
                                        Expr.icontains(FinderKey.NAME, query));
                        }

                        if (body.hasNonNull(ParameterKey.START_INDEX)
                                && body.hasNonNull(ParameterKey.END_INDEX)) {
                            int startIndex = body.get(ParameterKey.START_INDEX).asInt();
                            int endIndex = body.get(ParameterKey.END_INDEX).asInt();

                            if (startIndex < endIndex || startIndex > endIndex) {
                                throw new IndexOutOfBoundException();
                            }

                            expression.setFirstRow(startIndex);
                            expression.setMaxRows(endIndex - startIndex);
                        }

                        List<User> users = expression.findList();
                        List<ObjectNode> userNodes = new ArrayList<>(users.size() + 5);
                        for (User userSearched : users) {
                            JsonNode node = Json.toJson(userSearched);

                            FriendsCircle.FriendshipLevel friendshipLevel = UserUtil.getFriendshipLevel(user.getId(), userSearched.getId());
                            ObjectNode userNode = Json.newObject();
                            userNode.put(ParameterKey.USER, node);
                            userNode.put(ParameterKey.FRIENDSHIP_LEVEL, friendshipLevel.ordinal());

                            userNodes.add(userNode);
                        }
                        JsonNode usersNode = Json.toJson(userNodes);

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.User.SEARCH_SUCCESS));
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
        });
    }

    /**
     * Método responsável por listar um usuário específico
     * baseado no parâmetro de LOGIN enviado na requisição.
     * @return JSON
     */
    @Security.Authenticated(MobileAuthenticator.class)
    public static F.Promise<Result> list() {
        return F.Promise.<Result>promise(() -> {
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
                                new String[]{FinderKey.LOGIN},
                                new Object[]{login});

                        if (userFounded != null && UserUtil.isAvailable(userFounded)) {
                            JsonNode usersNode = Json.toJson(userFounded);
                            ObjectNode perfilNode = Json.newObject();
                            perfilNode.put(ParameterKey.USER, usersNode);

                            if (user.getId() != userFounded.getId()) {
                                FriendsCircle.FriendshipLevel friendshipLevel = UserUtil.getFriendshipLevel(user.getId(), userFounded.getId());

                                IFinder<WishList> wFinder = factory.get(WishList.class);
                                List<WishList> wishLists = wFinder.selectAll(
                                        new String[]{FinderKey.USER_ID, FinderKey.STATUS},
                                        new Object[]{userFounded.getId(), WishList.Status.ACTIVE.ordinal()});

                                JsonNode wishListsNode = Json.toJson(wishLists);

                                perfilNode.put(ParameterKey.FRIENDSHIP_LEVEL, friendshipLevel.ordinal());
                                perfilNode.put(ParameterKey.WISHLIST, wishListsNode);
                            }

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.User.LIST_SUCCESS));
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
        });
    }

    /**
     * Método responsável por adicionar ou aceitar o usuário
     * em seu círculo de amigos.
     * @return JSON
     */
    @Security.Authenticated(MobileAuthenticator.class)
    public static F.Promise<Result> joinCircle() {
        return F.Promise.<Result>promise(() -> {
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
                                new String[]{FinderKey.LOGIN},
                                new Object[]{login});

                        if (userTarget != null && UserUtil.isAvailable(userTarget)) {
                            boolean isFriends = UserUtil.joinCircle(user, factory, userTarget);

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.User.JOIN_CIRCLE_SUCCESS, userTarget.getLogin()));
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
        });
    }

    /**
     * Método responsável por remover o usuário do seu
     * círculo de amigos. Além disso, também é capaz de desfazer
     * a solicitação de amizade que ainda não foi aceita pelo usuário.
     *
     * @return JSON
     */
    @Security.Authenticated(MobileAuthenticator.class)
    public static F.Promise<Result> leaveCircle() {
        return F.Promise.<Result>promise(() -> {
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
                                new String[]{FinderKey.LOGIN},
                                new Object[]{login});

                        if (userTarget != null && UserUtil.isAvailable(userTarget)) {
                            IFinder<FriendsCircle> finderCircle = factory.get(FriendsCircle.class);
                            FriendsCircle friendsCircle = finderCircle.selectUnique(
                                    new String[]{FinderKey.REQUESTER_ID, FinderKey.TARGET_ID},
                                    new Object[]{user.getId(), userTarget.getId()});

                            if (friendsCircle != null) {
                                friendsCircle.delete();
                            }

                            friendsCircle = finderCircle.selectUnique(
                                    new String[]{FinderKey.REQUESTER_ID, FinderKey.TARGET_ID},
                                    new Object[]{userTarget.getId(), user.getId()});

                            if (friendsCircle != null) {
                                friendsCircle.delete();
                            }

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.User.LEAVE_CIRCLE_SUCCESS, userTarget.getLogin()));
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
        });
    }

    /**
     * Método responsável por tratar o envio de amigos do usuário
     * que estavam na agenda ou em outro tipo de lugar para que sejam
     * adicionados ao círculo de amigos (caso eles possuam conta no uWant)
     * ou serem convidados a utilizar o sistema.
     * @return JSON
     */
    @Security.Authenticated(MobileAuthenticator.class)
    public static F.Promise<Result> analyzeContacts() {
        return F.Promise.<Result>promise(() -> {
            final ObjectNode jsonResponse = Json.newObject();
            try {
                final User user = authenticateToken();
                if (user != null && UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    if (body != null
                            && body.hasNonNull(ParameterKey.CONTACTS)) {
                        final JsonNode jsonContacts = body.get(ParameterKey.CONTACTS);
                        if (jsonContacts.isArray()) {
                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<User> finder = factory.get(User.class);
                            IFinder<SocialProfile> finderSP = factory.get(SocialProfile.class);

                            int toCircle = 0, toInvite = 0;
                            for (int i = 0; i < jsonContacts.size(); i++) {
                                User userTarget = null;

                                JsonNode jsonContact = jsonContacts.get(i);
                                if (jsonContact.hasNonNull(ParameterKey.MAIL)) {
                                    String email = jsonContact.get(ParameterKey.MAIL).asText();
                                    userTarget = finder.selectUnique(new String[]{FinderKey.MAIL}, new Object[]{email});
                                } else if (jsonContact.hasNonNull(ParameterKey.FACEBOOK_ID)) {
                                    String facebookId = jsonContact.get(ParameterKey.FACEBOOK_ID).asText();
                                    SocialProfile profile = finderSP.selectUnique(new String[]{FinderKey.FACEBOOK_ID}, new Object[]{facebookId});
                                    if (profile != null) {
                                        userTarget = profile.getUser();
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }

                                if (userTarget == null) {
                                    toInvite++;
                                    // TODO Convite através de envio de e-mail..
                                } else {
                                    UserUtil.joinCircle(user, factory, userTarget);
                                    toCircle++;
                                }
                            }

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.User.ANALYZE_CONTACTS_SUCCESS, toCircle, toInvite));
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
                jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
                jsonResponse.put(ParameterKey.ERROR, e.getCode());
            }

            return ok(jsonResponse);
        });
    }

    /**
     * Método responsável por listar todos os amigos que estão contidos
     * no círculo de amigos do usuário logado.
     * @return JSON
     */
    @Security.Authenticated(MobileAuthenticator.class)
    public static F.Promise<Result> listCircle() {
        return F.Promise.<Result>promise(() -> {
            final ObjectNode jsonResponse = Json.newObject();
            try {
                final User user = authenticateToken();
                if (user != null && UserUtil.isAvailable(user)) {
                    JsonNode body = request().body().asJson();
                    final long userId;
                    if (body != null && body.has(ParameterKey.ID)) {
                        userId = body.get(ParameterKey.ID).asLong();

                        if ((userId != user.getId())
                                && UserUtil.getFriendshipLevel(user.getId(), userId) != FriendsCircle.FriendshipLevel.MUTUAL) {
                            throw new UnauthorizedOperationException();
                        }
                    } else {
                        userId = user.getId();
                    }

                    FinderFactory factory = FinderFactory.getInstance();
                    IFinder<FriendsCircle> finder = factory.get(FriendsCircle.class);
                    IFinder<User> finderUser = factory.get(User.class);

                    List<FriendsCircle> requesterCircle = finder.selectAll(
                            new String[]{FinderKey.REQUESTER_ID},
                            new Object[]{userId});

                    List<User> circle = new ArrayList<>(requesterCircle.size() + 5);
                    for (FriendsCircle friendsCircle : requesterCircle) {
                        FriendsCircle.Relation relation = friendsCircle.getRelation();
                        User userTarget = finderUser.selectUnique(relation.getTargetId());

                        if (UserUtil.isAvailable(userTarget)) {
                            FriendsCircle.FriendshipLevel level = UserUtil.getFriendshipLevel(userId, userTarget.getId());
                            if (level == FriendsCircle.FriendshipLevel.MUTUAL) {
                                circle.add(userTarget);
                            }
                        }
                    }
                    jsonResponse.put(ParameterKey.STATUS, true);
                    jsonResponse.put(ParameterKey.MESSAGE, Messages.get(MessageKey.User.LIST_CIRCLE_SUCCESS));
                    jsonResponse.put(ParameterKey.FRIENDS, Json.toJson(circle));
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
        });
    }

}
