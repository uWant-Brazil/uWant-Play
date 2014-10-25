package security;

import controllers.AbstractApplication;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import java.util.concurrent.TimeUnit;

/**
 * Classe responsável por receber todas as requisições aos controladores
 * do package controllers.mobile a fim de validar a sessão do usuário.
 */
public class MobileAuthenticator extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context ctx) {
        return AbstractApplication.getTokenAtHeader(ctx.request());
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return AbstractApplication.invalidMobileSession().get(5, TimeUnit.MINUTES);
    }

}
