package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.User;
import models.classes.UserMailInteraction;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.libs.Json;
import play.mvc.Result;
import utils.RegexUtil;
import utils.UserUtil;

/**
 * Created by felipebonezi on 21/05/14.
 */
public class AuthenticationController extends AbstractApplication {

    public static Result authorize() {
        JsonNode body = request().body().asJson();

        ObjectNode jsonResponse = Json.newObject();
        try {
            if (body != null) {
                if (body.hasNonNull(ParameterKey.LOGIN) && body.hasNonNull(ParameterKey.PASSWORD)) {
                    String login = body.get(ParameterKey.LOGIN).asText();
                    String password = body.get(ParameterKey.PASSWORD).asText();

                    User user = null;
                    if ((!login.isEmpty() && !password.isEmpty()) || (user = authenticateToken()) != null) {
                        if (user == null) {
                            FinderFactory factory = FinderFactory.getInstance();
                            IFinder<User> finder = factory.get(User.class);
                            user = finder.selectUnique(new String[] { FinderKey.LOGIN, FinderKey.PASSWORD }, new String[] { login, password });
                        }

                        if (user != null && UserUtil.isAvailable(user)) {
                            generateToken(user);

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "Usuário autenticado com sucesso.");
                        } else {
                            throw new AuthenticationException();
                        }
                    } else {
                        throw new AuthenticationException();
                    }
                } else {
                    throw new AuthenticationException();
                }
            } else {
                throw new JSONBodyException();
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return ok(jsonResponse);
    }

    public static Result logoff() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user;
            if ((user = authenticateToken()) != null && UserUtil.isAvailable(user)) {
                removeToken(user);

                jsonResponse.put(ParameterKey.STATUS, true);
                jsonResponse.put(ParameterKey.MESSAGE, "O usuário saiu do sistema com sucesso.");
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return ok(jsonResponse);
    }

    public static Result recoveryPassword() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            JsonNode body = request().body().asJson();
            if (body != null) {
                if (body.hasNonNull(ParameterKey.MAIL)) {
                    String mail = body.get(ParameterKey.MAIL).asText();

                    if (!mail.isEmpty() && RegexUtil.isValidMail(mail)) {
                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<User> finderUser = factory.get(User.class);
                        User user = finderUser.selectUnique(new String[] { FinderKey.MAIL }, new Object[] { mail });
                        if (user == null) {
                            throw new UserDoesntExistException();
                        }

                        if (UserUtil.isAvailable(user)) {
                            UserMailInteraction confirmation = user.getConfirmation();
                            UserMailInteraction.Status confirmationStatus = confirmation.getStatus();
                            if (confirmationStatus == UserMailInteraction.Status.DONE) {
                                UserUtil.recoveryPassword(user);
                            } else {
                                throw new UnconfirmedMailException();
                            }
                        } else {
                            throw new AuthenticationException();
                        }

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "Foi enviado um e-mail para voce redefinir a sua nova senha.");
                    }
                }
            }
        } catch (UWException e) {
            e.printStackTrace();
            jsonResponse.put(ParameterKey.STATUS, false);
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return ok(jsonResponse);
    }

}
