package actions;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.SimpleResult;

/**
 * Action que irá tratar as requisições feitas por browsers (e.g. Chrome)
 * que ao efetuar um POST/OPTIONS solicita as permissões dos cabeçalhos.
 */
public class AjaxAction extends Action.Simple {

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    private static final String ORIGIN_VALUE = "*";
    private static final String HEADERS_VALUE = "X-Requested-With, Content-Type, X-Auth-Token, Authorization";
    private static final String EXPOSE_HEADER_VALUE = "Authorization";
    private static final String OPTIONS = "OPTIONS";
    private static final String METHODS_VALUE = "POST, GET, OPTIONS";
    private static final String MAX_AGE_VALUE = "3600";
    private static final String TRUE = "true";

    @Override
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable {
        Http.Response response = ctx.response();

        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN_VALUE);
        response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, HEADERS_VALUE);
        response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSE_HEADER_VALUE);

        if (ctx.request().method().toUpperCase().equals(OPTIONS)) {
            response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, METHODS_VALUE);
            response.setHeader(ACCESS_CONTROL_MAX_AGE, MAX_AGE_VALUE);
            response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, TRUE);
        }

        return delegate.call(ctx);
    }

}
