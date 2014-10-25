package security;

import com.typesafe.config.ConfigFactory;
import controllers.AbstractApplication;
import play.Application;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.api.mvc.Handler;
import play.filters.csrf.CSRFFilter;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.lang.reflect.Method;

public class UWGlobal extends GlobalSettings {

    private static final String CONST_HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONST_APPLICATION_JSON = "application/json";
    private static final String DEFAULT_ERROR_MESSAGE = Messages.get(AbstractApplication.MessageKey.Global.MOBILE_SESSION_ERROR);

    @Override
    public void onStart(Application app) {
        super.onStart(app);
    }

    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[] { CSRFFilter.class }; // CROSS SITE REQUEST FORGERY
    }

    @Override
    public Handler onRouteRequest(Http.RequestHeader request) {
        return super.onRouteRequest(request);
    }

    @Override
    public Action onRequest(Http.Request request, Method actionMethod) {
        return super.onRequest(request, actionMethod);
    }

    @Override
    public F.Promise<Result> onBadRequest(Http.RequestHeader request, String error) {
        return invalidSession(request);
    }

    @Override
    public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        return error(request);
    }

    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        return invalidSession(request);
    }

    private F.Promise<Result> invalidSession(Http.RequestHeader request) {
        if (request.hasHeader(CONST_HEADER_CONTENT_TYPE)) {
            String contentType = request.getHeader(CONST_HEADER_CONTENT_TYPE);
            if (contentType.startsWith(CONST_APPLICATION_JSON)) {
                return AbstractApplication.invalidMobileSession();
            }
        }

        return AbstractApplication.invalidWebSession();
    }

    private F.Promise<Result> error(Http.RequestHeader request) {
        if (request.hasHeader(CONST_HEADER_CONTENT_TYPE)) {
            String contentType = request.getHeader(CONST_HEADER_CONTENT_TYPE);
            if (contentType.startsWith(CONST_APPLICATION_JSON)) {
                return AbstractApplication.invalidMobileSession(DEFAULT_ERROR_MESSAGE, -666);
            }
        }

        return AbstractApplication.invalidWebSession(DEFAULT_ERROR_MESSAGE);
    }

}
