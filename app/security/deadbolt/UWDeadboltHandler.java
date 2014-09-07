package security.deadbolt;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import controllers.AbstractApplication;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import utils.AdminUtil;

public class UWDeadboltHandler extends AbstractDeadboltHandler {

    private static final java.lang.String ERROR_MESSAGE = "Estão tentando acessar controladores fora da sua alçada/permissão - %s";
    private static final String WEB_ERROR_MESSAGE = "Você não tem permissão para acessar essa funcionalidade. Nossa equipe será notificada pela tentativa de acesso! :-)";

    @Override
    public F.Promise<Result> beforeAuthCheck(Http.Context context) {
        return null;
    }

    @Override
    public Subject getSubject(Http.Context context) {
        Subject subject = null;

        String token = context.request().username();
        if (token != null && !token.isEmpty()) {
            subject = AdminUtil.get(token);
        }

        return subject;
    }

    @Override
    public F.Promise<Result> onAuthFailure(Http.Context context, String content) {
        return AbstractApplication.invalidWebSession(WEB_ERROR_MESSAGE);
    }
}
