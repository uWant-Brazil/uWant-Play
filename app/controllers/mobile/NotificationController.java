package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Action;
import models.classes.Mobile;
import models.classes.Token;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.AuthenticationException;
import models.exceptions.IndexOutOfBoundException;
import models.exceptions.JSONBodyException;
import models.exceptions.UWException;
import play.libs.Json;
import play.mvc.Result;
import utils.DateUtil;
import utils.UserUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public static Result listActions() {
        ObjectNode jsonResponse = Json.newObject();
        try {
            User user = authenticateToken();
            if (UserUtil.isAvailable(user)) {
                JsonNode body = request().body().asJson();
                if (body != null && body.has(ParameterKey.START_INDEX)
                        && body.has(ParameterKey.END_INDEX)) {
                    int startIndex = body.get(ParameterKey.START_INDEX).asInt(0);
                    int endIndex = body.get(ParameterKey.END_INDEX).asInt(Integer.MAX_VALUE);

                    if (startIndex < endIndex) {
                        FinderFactory factory = FinderFactory.getInstance();
                        IFinder<Action> finder = factory.get(Action.class);

                        List<Action> actions = finder.getFinder()
                                .where()
                                .eq(FinderKey.USER_ID, user.getId())
                                .setFirstRow(startIndex)
                                .setMaxRows(endIndex - startIndex)
                                .findList();

                        List<ObjectNode> arrayActions = new ArrayList<ObjectNode>(actions.size() + 5);
                        for (Action action : actions) {
                            String message = action.toString();
                            if (message != null) {
                                int typeOrdinal = action.getType().ordinal();
                                String extra = action.getExtra();
                                Date when = action.getCreatedAt();

                                User from = action.getFrom();
                                JsonNode jsonFrom = Json.toJson(from);

                                ObjectNode json = Json.newObject();
                                json.put(ParameterKey.TYPE, typeOrdinal);
                                json.put(ParameterKey.MESSAGE, message);
                                json.put(ParameterKey.WHEN, DateUtil.format(when, DateUtil.DATE_HOUR_PATTERN));
                                json.put(ParameterKey.EXTRA, extra);
                                json.put(ParameterKey.USER_FROM, jsonFrom);

                                arrayActions.add(json);
                            }
                        }

                        jsonResponse.put(ParameterKey.STATUS, true);
                        jsonResponse.put(ParameterKey.MESSAGE, "A lista de ações foram recuperadas com sucesso.");
                        jsonResponse.put(ParameterKey.ACTIONS, Json.toJson(arrayActions));
                    } else {
                        throw new IndexOutOfBoundException();
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
