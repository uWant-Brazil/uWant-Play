package controllers.web;

import controllers.AbstractApplication;
import models.classes.Token;
import models.classes.User;
import models.cloud.models.AuthenticationViewModel;
import models.database.FinderFactory;
import models.database.IFinder;
import play.data.Form;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Result;
import utils.SecurityUtil;

import java.util.concurrent.TimeUnit;

/**
 * Controlador responsável pelas requisições mobile relacionadas a autenticação no sistema.
 */
public class AuthenticationController extends AbstractApplication {

    public static F.Promise<Result> authorizeView() {
        return F.Promise.<Result>pure(ok(views.html.authentication.render(Form.form(AuthenticationViewModel.class))));
    }

    @RequireCSRFCheck
    public static F.Promise<Result> authorize() {
        Form<AuthenticationViewModel> form = Form.<AuthenticationViewModel>form(AuthenticationViewModel.class).bindFromRequest();
        if (isValidForm(form)) {
            final AuthenticationViewModel model = form.get();

            return F.Promise.<Result>promise(() -> {
                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(
                        new String[]{FinderKey.LOGIN, FinderKey.PASSWORD},
                        new Object[]{model.getLogin(), SecurityUtil.md5(model.getPassword())});

                if (user == null) {
                    return invalidWebSession(Messages.get(MessageKey.Authentication.AUTHORIZE_FAIL)).get(5, TimeUnit.MINUTES);
                } else {
                    generateToken(user, Token.Target.WEB);
                    return ok(views.html.perfil.render(user));
                }
            });
        } else {
            return invalidWebSession(Messages.get(MessageKey.Authentication.AUTHORIZE_FAIL));
        }
    }

}
