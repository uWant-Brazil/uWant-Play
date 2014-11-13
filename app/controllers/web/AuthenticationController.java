package controllers.web;

import controllers.AbstractApplication;
import models.classes.Token;
import models.classes.User;
import models.classes.WishList;
import models.cloud.forms.MultimediaViewModel;
import models.cloud.forms.ProductViewModel;
import models.cloud.forms.UserViewModel;
import models.cloud.forms.WishListViewModel;
import models.database.FinderFactory;
import models.database.IFinder;
import play.api.mvc.Call;
import play.data.Form;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Result;
import utils.SecurityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Controlador responsável pelas requisições mobile relacionadas a autenticação no sistema.
 */
public class AuthenticationController extends AbstractApplication {

    @AddCSRFToken
    public static F.Promise<Result> authorizeView() {
        return F.Promise.<Result>pure(ok(views.html.authentication.render(Form.form(UserViewModel.class))));
    }

    @RequireCSRFCheck
    public static F.Promise<Result> authorize() {
        Form<UserViewModel> form = Form.<UserViewModel>form(UserViewModel.class).bindFromRequest();
        if (isValidForm(form)) {
            final UserViewModel model = form.get();

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

                    return redirect(controllers.web.routes.UserController.perfil(user.getLogin()));
                }
            });
        } else {
            return invalidWebSession(Messages.get(MessageKey.Authentication.AUTHORIZE_FAIL));
        }
    }

}
