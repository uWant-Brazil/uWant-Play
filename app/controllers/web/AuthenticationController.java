package controllers.web;

import controllers.AbstractApplication;
import models.classes.Token;
import models.classes.User;
import models.classes.WishList;
import models.cloud.forms.MultimediaViewModel;
import models.cloud.forms.UserAuthenticationViewModel;
import models.cloud.forms.UserRegisterViewModel;
import models.cloud.forms.WishListViewModel;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.TokenException;
import play.data.Form;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Security;
import security.WebAuthenticator;
import utils.SecurityUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Controlador responsável pelas requisições mobile relacionadas a autenticação no sistema.
 */
public class AuthenticationController extends AbstractApplication {

    /**
     * Método responsável por exibir a tela de autenticação ou registro do usuário.
     * P.S.: Note que como essa tela terá form's no formato x-formurl-encoded, então precisamos adicionar os CSRFToken's.
     * @return View
     */
    @AddCSRFToken
    public static F.Promise<Result> authorizeView() {
        return F.Promise.<Result>pure(ok(views.html.authentication.render(Form.form(UserRegisterViewModel.class), Form.form(UserAuthenticationViewModel.class))));
    }

    /**
     * Método responsável por autenticar um usuário no sistema.
     * P.S.: Note que para entrar nesse método é necessário que o form encaminhe o CSRFToken gerado anteriormente.
     * @return View
     */
    @RequireCSRFCheck
    public static F.Promise<Result> authorize() {
        Form<UserAuthenticationViewModel> form = Form.form(UserAuthenticationViewModel.class).bindFromRequest();
        if (isValidForm(form)) {
            final UserAuthenticationViewModel model = form.get();

            return F.Promise.<Result>promise(() -> {
                String login = model.getLogin();
                String password = SecurityUtil.md5(model.getPassword());

                FinderFactory factory = FinderFactory.getInstance();
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(
                        new String[]{FinderKey.LOGIN, FinderKey.PASSWORD},
                        new Object[]{login, password});

                if (user == null) {
                    return invalidWebSession(Messages.get(MessageKey.Authentication.AUTHORIZE_FAIL)).get(5, TimeUnit.MINUTES);
                } else {
                    generateToken(user, Token.Target.WEB);

                    return redirect(controllers.web.routes.UserController.perfil(user.getLogin()));
                    //return ok(views.html.success.render(String.format("Olá %s, você registrou o seu usuário e autenticou em nosso sistema, mas os nossos serviços ainda está em fase ALPHA. Aguarde que em breve iremos liberar o acesso a todos!", user.getName())));
                }
            });
        } else {
            return invalidWebSession(Messages.get(MessageKey.Authentication.AUTHORIZE_FAIL));
        }
    }

    @Security.Authenticated(WebAuthenticator.class)
    public static F.Promise<Result> logoff() {
        try {
            User user = authenticateSession();
            if (user != null) {
                removeSession(user);
            }
        } catch (TokenException e) {
            e.printStackTrace();
        }

        return F.Promise.pure(redirect(controllers.routes.AbstractApplication.index()));
    }

}
