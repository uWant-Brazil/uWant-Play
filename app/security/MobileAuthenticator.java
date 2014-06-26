package security;

import controllers.AbstractApplication;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Classe responsável por receber todas as requisições aos controladores
 * do package controllers.mobile a fim de validar a sessão do usuário.
 */
public class MobileAuthenticator extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context ctx) {
        return AbstractApplication.getToken(ctx.request());
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return AbstractApplication.invalidMobileSession();
    }

}
