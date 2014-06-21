package controllers.mobile;

import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.SocialProfile;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.db.ebean.Model;
import play.libs.Json;
import play.mvc.Result;
import utils.DateUtil;
import utils.RegexUtil;
import utils.UserUtil;

import java.text.ParseException;
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
                    user.setStatus(User.Status.REMOVED);
                    user.update();

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

}
