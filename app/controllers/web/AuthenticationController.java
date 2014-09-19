package controllers.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Token;
import models.classes.User;
import models.cloud.models.AuthenticationViewModel;
import models.cloud.models.WishListViewModel;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.RegexUtil;
import utils.UserUtil;

/**
 * Controlador responsável pelas requisições mobile relacionadas a autenticação no sistema.
 */
public class AuthenticationController extends AbstractApplication {

    public static F.Promise<Result> authorizeView() {
        return F.Promise.pure(ok());
    }

    /**
     * Método responsável por realizar a autenticação do usuário no sistema.
     * Caso seja autenticado, um token único será gerado e deverá ser utilizado em
     * requisições seguintes a fim de validar sua sessão.
     * @return JSON
     */
    public static F.Promise<Result> authorize() {
        Form<AuthenticationViewModel> form = Form.form(AuthenticationViewModel.class).bindFromRequest(request());
        if (isValidForm(form)) {
            final AuthenticationViewModel model = form.get();
        }


        ObjectNode jsonResponse = Json.newObject();
        JsonNode body = request().body().asJson();
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
                            user = finder.selectUnique(new String[] { FinderKey.LOGIN, FinderKey.PASSWORD },
                                    new String[] { login, password });
                        }

                        if (user != null && UserUtil.isAvailable(user)) {
                            generateToken(user, Token.Target.MOBILE);

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "Usuário autenticado com sucesso.");
                            jsonResponse.put(ParameterKey.USER, Json.toJson(user));
                        } else {
                            throw new AuthenticationException();
                        }
                    } else {
                        throw new AuthenticationException();
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
            jsonResponse.put(ParameterKey.ERROR, e.getCode());
            jsonResponse.put(ParameterKey.MESSAGE, e.getMessage());
        }

        return F.Promise.pure(ok(jsonResponse));
    }

}
