package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Mobile;
import models.classes.Token;
import models.classes.User;
import models.exceptions.AuthenticationException;
import models.exceptions.JSONBodyException;
import models.exceptions.UWException;
import play.libs.Json;
import play.mvc.Result;
import utils.UserUtil;

/**
 * Controlador responsável pelo tratamento de requisições relacionadas a notificações do sistema
 * enviadas para os dispositivos móveis vinculados a usuários.
 */
public class NotificationController extends AbstractApplication {

    /**
     * Método responsável pelo registro/vínculo do identificador nos servidores
     * do Google/Microsoft/Apple para envio de notificações para os usuários.
     * @return JSON
     */
    public static Result register() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.hasNonNull(ParameterKey.MOBILE_IDENTIFIER) && body.hasNonNull(ParameterKey.OS)) {
                    String identifier = body.get(ParameterKey.MOBILE_IDENTIFIER).asText();
                    int osId = body.get(ParameterKey.OS).asInt();

                    Mobile.OS[] oses = Mobile.OS.values();
                    if (!identifier.isEmpty() && osId >= 0 && osId < oses.length) {
                        String tokenContent = request().getHeader(HeaderKey.HEADER_AUTHENTICATION_TOKEN);
                        Token token = listToken(tokenContent);
                        Mobile.OS os = oses[osId];

                        Mobile mobile = new Mobile();
                        mobile.setIdentifier(identifier);
                        mobile.setToken(token);
                        mobile.setUser(user);
                        mobile.setOS(os);
                        mobile.save();

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "O dispositivo móvel foi registrado para " + user.getLogin());
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

}
