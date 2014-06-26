package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Token;
import models.classes.User;
import models.classes.UserMailInteraction;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.RegexUtil;
import utils.UserUtil;

/**
 * Controlador responsável pelas requisições mobile relacionadas a autenticação no sistema.
 */
public class AuthenticationController extends AbstractApplication {

    /**
     * Método responsável por realizar a autenticação do usuário no sistema.
     * Caso seja autenticado, um token único será gerado e deverá ser utilizado em
     * requisições seguintes a fim de validar sua sessão.
     * @return JSON
     */
    public static Result authorize() {
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
                            user = finder.selectUnique(new String[] { FinderKey.LOGIN, FinderKey.PASSWORD }, new String[] { login, password });
                        }

                        if (user != null && UserUtil.isAvailable(user)) {
                            generateToken(user, Token.Target.MOBILE);

                            jsonResponse.put(ParameterKey.STATUS, true);
                            jsonResponse.put(ParameterKey.MESSAGE, "Usuário autenticado com sucesso.");
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

        return ok(jsonResponse);
    }

    /**
     * Método responsável por finalizar a sessão do usuário no sistema.
     * Após esse ato, nenhuma outra requisição que necessite do token de autenticação
     * deverá funcionar corretamente.
     * @return JSON
     */
    @Security.Authenticated(MobileAuthenticator.class)
    public static Result logoff() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (user != null && UserUtil.isAvailable(user)) {
                removeToken(user);

                jsonResponse.put(ParameterKey.STATUS, true);
                jsonResponse.put(ParameterKey.MESSAGE, "O usuário saiu do sistema com sucesso.");
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
     * Método responsável por iniciar o processo de recuperação da senha do usuário.
     * Note que não será redefinida a senha imediatamente, o usuário deverá receber um
     * link contendo o endereço para redefinição da senha através da web.
     * @return JSON
     */
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
                        final User user = finderUser.selectUnique(
                                new String[] { FinderKey.MAIL },
                                new Object[] { mail });

                        if (user == null) {
                            throw new UserDoesntExistException();
                        }

                        if (UserUtil.isAvailable(user)) {
                            if (UserUtil.isMailConfirmed(user)) {
                                UserUtil.recoveryPassword(user);
                            } else {
                                // Uma nova confirmação será enviada...
                                throw new UnconfirmedMailException(user);
                            }
                        } else {
                            throw new AuthenticationException();
                        }

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "Foi enviado um e-mail para voce redefinir a sua nova senha.");
                    } else {
                        throw new InvalidMailException();
                    }
                } else {
                    throw new JSONBodyException();
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
