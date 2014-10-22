package security.csrf;

import controllers.AbstractApplication;
import play.filters.csrf.CSRFErrorHandler;
import play.mvc.Result;

import java.util.concurrent.TimeUnit;

public class UWCSRFErrorHandler implements CSRFErrorHandler {

    @Override
    public Result handle(String msg) {
        return AbstractApplication.invalidWebSession().get(5, TimeUnit.MINUTES);
    }

}
