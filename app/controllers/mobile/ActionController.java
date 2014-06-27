package controllers.mobile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.AbstractApplication;
import models.classes.Action;
import models.classes.Comment;
import models.classes.IMobileUser;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.AuthenticationException;
import models.exceptions.JSONBodyException;
import models.exceptions.UWException;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.MobileAuthenticator;
import utils.NotificationUtil;
import utils.UserUtil;

import java.util.Date;

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

}
