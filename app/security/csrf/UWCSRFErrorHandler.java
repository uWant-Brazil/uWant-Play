package security.csrf;

import controllers.AbstractApplication;
import play.filters.csrf.CSRFErrorHandler;
import play.mvc.Result;

import java.util.concurrent.TimeUnit;

/**
 * Filtro responsável por verificar se os formulários possuem o CSRFToken, caso esteja configurado.
 */
public class UWCSRFErrorHandler implements CSRFErrorHandler {

    @Override
    public Result handle(String msg) {
        return AbstractApplication.invalidWebSession().get(5, TimeUnit.MINUTES);
    }

}
