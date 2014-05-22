package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.IMobileUser;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.AuthenticationException;
import models.exceptions.JSONBodyException;
import models.exceptions.UWException;
import play.libs.Json;
import play.mvc.Result;
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

}