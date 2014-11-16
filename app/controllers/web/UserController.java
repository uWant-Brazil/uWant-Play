package controllers.web;

import controllers.AbstractApplication;
import models.classes.Token;
import models.classes.User;
import models.classes.UserMailInteraction;
import models.cloud.forms.MultimediaViewModel;
import models.cloud.forms.ProductViewModel;
import models.cloud.forms.UserViewModel;
import models.cloud.forms.WishListViewModel;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.UWException;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Security;
import security.WebAuthenticator;
import utils.UserUtil;
import utils.WishListUtil;
import views.html.confirmMail;
import views.html.recoveryPassword;
import views.html.unauthorized;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Controlador responsável pelo tratamento de requisições web referentes a interações com o usuário.
 */
public class UserController extends AbstractApplication {

    /**
     * Tempo máximo para resposta de uma interação com o e-mail do usuário.
     * Caso o tempo seja excedido, o sistema deverá enviar uma nova interação
     * para o e-mail do usuário, em horas.
     */
    private static final long MAX_TIME_AVERAGE = 24;

    /**
     * Método responsável por exibir a View que irá informar se
     * o e-mail do usuário foi confirmado com sucesso.
     * @param h - Hash
     * @param m - Email
     * @return View
     */
    public static Result confirmMailView(String h, String m) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<UserMailInteraction> finder = factory.get(UserMailInteraction.class);
        UserMailInteraction umi = finder.selectUnique(
                new String[] { FinderKey.HASH, FinderKey.MAIL },
                new Object[] { h, m });

        if (umi != null) {
            if (umi.getStatus() == UserMailInteraction.Status.DONE || Hours.hoursBetween(new DateTime(umi.getCreatedAt()), DateTime.now()).getHours() <= MAX_TIME_AVERAGE) {
                if (umi.getStatus() == UserMailInteraction.Status.WAITING) {
                    UserMailInteraction userMailInteraction = new UserMailInteraction();
                    userMailInteraction.setStatus(UserMailInteraction.Status.DONE);
                    userMailInteraction.update(umi.getId());

                    User user = umi.getUser();
                    User userModified = new User();
                    userModified.setStatus(User.Status.ACTIVE);
                    userModified.update(user.getId());
                }

                User user = umi.getUser();
                return ok(confirmMail.render(Messages.get(MessageKey.User.CONFIRM_MAIL_SUCCESS, user.getName(), user.getMail())));
            } else {
                UserMailInteraction userMailInteraction = new UserMailInteraction();
                userMailInteraction.setStatus(UserMailInteraction.Status.CANCELED);
                userMailInteraction.update(umi.getId());

                UserUtil.confirmEmail(umi.getUser(), false);

                return unauthorized(unauthorized.render(Messages.get(MessageKey.User.CONFIRM_MAIL_EXPIRE)));
            }
        }

        return unauthorized(unauthorized.render(Messages.get(MessageKey.User.CONFIRM_MAIL_INVALID)));
    }

    /**
     * Método responsável por exibir a View que irá ser responsável por
     * capturar os dados para realizar a recuperação da senha do usuário.
     * @param ts - Milisegundos
     * @param h - Hash
     * @param m - Email
     * @return View
     */
    @AddCSRFToken
    public static Result showRecoveryPassword(Long ts, String h, String m) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<UserMailInteraction> finder = factory.get(UserMailInteraction.class);
        UserMailInteraction umi = finder.selectUnique(
                new String[] { FinderKey.HASH, FinderKey.MAIL },
                new Object[] { h, m });

        if (umi != null) {
            if ((Hours.hoursBetween(DateTime.now(), new DateTime(ts)).getHours() > MAX_TIME_AVERAGE) || umi.getStatus() == UserMailInteraction.Status.CANCELED) {
                if (umi.getStatus() != UserMailInteraction.Status.CANCELED) {
                    UserMailInteraction userMailInteraction = new UserMailInteraction();
                    userMailInteraction.setStatus(UserMailInteraction.Status.CANCELED);
                    userMailInteraction.update(umi.getId());
                }

                return unauthorized(unauthorized.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_EXPIRE)));
            } else {
                if (umi.getStatus() == UserMailInteraction.Status.WAITING) {
                    UserMailInteraction userMailInteraction = new UserMailInteraction();
                    userMailInteraction.setStatus(UserMailInteraction.Status.DONE);
                    userMailInteraction.update(umi.getId());
                }

                User user = umi.getUser();
                return ok(recoveryPassword.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_WARNING), user.getId(), umi.getId()));
            }
        }

        return unauthorized(unauthorized.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_INVALID)));
    }

    /**
     * Método responsável por realizar o processo de recuperação da senha
     * do usuário baseado na nova senha informada através do formulário web.
     * Caso seja realizada ou não, o método irá retornar uma View.
     * @param id - Id do Usuário
     * @param mailId - Id da Interação por Email
     * @return View
     */
    @RequireCSRFCheck
    public static Result recoveryPassword(Long id, Long mailId) {
        Map<String, String[]> body = request().body().asFormUrlEncoded();

        if (body != null && body.containsKey(ParameterKey.PASSWORD)) {
            String password = body.get(ParameterKey.PASSWORD)[0];

            FinderFactory factory = FinderFactory.getInstance();
            IFinder<UserMailInteraction> mailFinder = factory.get(UserMailInteraction.class);
            UserMailInteraction userMailInteraction = mailFinder.selectUnique(mailId);
            if (userMailInteraction.getStatus() == UserMailInteraction.Status.DONE) {
                IFinder<User> finder = factory.get(User.class);
                User user = finder.selectUnique(id);

                List<Token> tokens = user.getTokens();
                if (tokens != null) {
                    for (Token token : tokens) {
                        token.delete();
                    }
                }
                user.refresh();

                User userChanged = new User();
                userChanged.setPassword(password);
                userChanged.update(user.getId());

                return ok(recoveryPassword.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_SUCCESS), (long) -1, (long) -1));
            } else {
                return unauthorized(unauthorized.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_EXPIRE)));
            }
        } else {
            return unauthorized(unauthorized.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_INVALID)));
        }
    }

    /**
     * Método responsável por realizar o cadastro de novos usuários a partir do formulário
     * enviado pela página de autenticação/registro do sistema.
     * @return View
     */
    @RequireCSRFCheck
    public static F.Promise<Result> register() {
        return F.Promise.<Result>pure(ok());
    }

    /**
     * Método responsável por exibir todos os dados do perfil do usuário logado.
     * @param login
     * @return
     */
    @Security.Authenticated(WebAuthenticator.class)
    public static F.Promise<Result> perfil(String login) {
        return F.Promise.<Result>promise(() -> {
            try {
                User user = authenticateSession();
                UserViewModel userVM = UserUtil.getPerfilUser(user, login);
                List<WishListViewModel> wishlistsVM = WishListUtil.getPerfilWishList(user, login);

                List<MultimediaViewModel> randomAuxVM = new ArrayList<MultimediaViewModel>(10);
                for (WishListViewModel wlvm : wishlistsVM) {
                    List<ProductViewModel> psvm = wlvm.getProducts();
                    for (ProductViewModel pvm : psvm) {
                        randomAuxVM.add(pvm.getMultimedia());
                    }
                }

                Random random = new Random();
                int range = randomAuxVM.size() >= 8 ? 8 : randomAuxVM.size();
                List<MultimediaViewModel> randomVM = new ArrayList<MultimediaViewModel>(10);
                while (range > 0 && randomAuxVM.size() > 0) {
                    int randomIndex = random.nextInt(randomAuxVM.size() + 1);
                    MultimediaViewModel mvm = randomAuxVM.get(randomIndex);
                    randomAuxVM.remove(randomIndex);
                    randomVM.add(mvm);
                    range--;
                }

                return ok(views.html.perfil.render(userVM, randomVM, wishlistsVM));
            } catch (UWException e) {
                e.printStackTrace();
                return invalidWebSession().get(5, TimeUnit.MINUTES);
            }
        });
    }

}
