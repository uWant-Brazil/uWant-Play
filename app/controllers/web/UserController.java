package controllers.web;

import com.ning.http.client.FilePart;
import controllers.AbstractApplication;
import models.classes.Token;
import models.classes.User;
import models.classes.UserMailInteraction;
import models.classes.WishList;
import models.cloud.forms.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import play.data.Form;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import utils.DateUtil;
import utils.SecurityUtil;
import utils.UserUtil;
import utils.WishListUtil;
import views.html.recoveryPassword;
import views.html.unauthorized;
import views.html.confirmMail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Controlador responsável pelo tratamento de requisições web referentes a interações com o usuário.
 */
public class UserController extends AbstractApplication {

    /**
     * Mensagem default para aviso na alteração da senha.
     */
    private static final String DEFAULT_RECOVERY_PASSWORD_MESSAGE = "Não se esqueça da sua senha!";

    /**
     * Tempo máximo para resposta de uma interação com o e-mail do usuário.
     * Caso o tempo seja excedido, o sistema deverá enviar uma nova interação
     * para o e-mail do usuário, em horas.
     */
    private static final long MAX_TIME_AVERAGE = 24;

    /**
     * Mensagem default para aviso na confirmação do e-mail.
     */
    private static final String DEFAULT_CONFIRM_MAIL_MESSAGE = "Olá %s, o seu endereço de e-mail (%s) foi confirmado com sucesso!";

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
                return ok(confirmMail.render(String.format(DEFAULT_CONFIRM_MAIL_MESSAGE, user.getName(), user.getMail())));
            } else {
                UserMailInteraction userMailInteraction = new UserMailInteraction();
                userMailInteraction.setStatus(UserMailInteraction.Status.CANCELED);
                userMailInteraction.update(umi.getId());

                UserUtil.confirmEmail(umi.getUser(), false);

                return unauthorized(unauthorized.render("Esta ação expirou! Estaremos encaminhando uma nova confirmação para o e-mail cadastrado."));
            }
        }

        return unauthorized(unauthorized.render("Não existe nenhuma solicitação para confirmação de e-mail..."));
    }

    @RequireCSRFCheck
    public static F.Promise<Result> startRecoveryPassword() {
        Form<RecoveryPasswordViewModel> form = Form.form(RecoveryPasswordViewModel.class).bindFromRequest();
        if (isValidForm(form)) {
            RecoveryPasswordViewModel model = form.get();

            FinderFactory factory = FinderFactory.getInstance();
            IFinder<User> finder = factory.get(User.class);
            User user = finder.selectUnique(new String[] { FinderKey.MAIL }, new Object[] { model.getMail() });

            try {
                if (user == null) {
                    throw new UserDoesntExistException();
                }

                if (UserUtil.isAvailable(user)) {
                    if (UserUtil.isMailConfirmed(user)) {
                        UserUtil.recoveryPassword(user);
                    } else {
                        // Uma nova confirmação será enviada...
                        throw new UnconfirmedMailException(user);
                    }
                } else {
                    throw new AuthenticationException();
                }

                return F.Promise.<Result>pure(redirect(controllers.web.routes.AuthenticationController.authorize()));
            } catch (UWException e) {
                e.printStackTrace();

                return invalidWebSession(e.getMessage());
            }
        }

        return invalidMobileSession();
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

                return unauthorized(unauthorized.render("A sua solicitação de alteração de senha expirou!"));
            } else {
                if (umi.getStatus() == UserMailInteraction.Status.WAITING) {
                    UserMailInteraction userMailInteraction = new UserMailInteraction();
                    userMailInteraction.setStatus(UserMailInteraction.Status.DONE);
                    userMailInteraction.update(umi.getId());
                }

                User user = umi.getUser();
                return ok(recoveryPassword.render(DEFAULT_RECOVERY_PASSWORD_MESSAGE, user.getId(), umi.getId()));
            }
        }

        return unauthorized(unauthorized.render("Não existe nenhuma solicitação para alteração de senha..."));
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

                return ok(recoveryPassword.render("A sua senha foi redefinida com sucesso!", (long) -1, (long) -1));
            } else {
                return unauthorized(unauthorized.render("Esta sessão para alteração de senha expirou..."));
            }
        } else {
            return unauthorized(unauthorized.render("Ocorreu um erro inesperado. Entre em contato com o suporte!"));
        }
    }

    @RequireCSRFCheck
    public static F.Promise<Result> register() {
        Http.MultipartFormData data = request().body().asMultipartFormData();
        Form<UserViewModel> form = Form.form(UserViewModel.class).bindFromRequest(data.asFormUrlEncoded());
        if (isValidForm(form)) {
            UserViewModel model = form.get();
            try {
                if (!UserUtil.alreadyExists(model.getLogin(), model.getMail())) {
                    User.Gender gender = User.Gender.valueOf(model.getGender());

                    User user = new User();
                    user.setLogin(model.getLogin());
                    user.setPassword(SecurityUtil.md5(model.getPassword()));
                    user.setName(model.getName());
                    user.setMail(model.getMail());
                    user.setBirthday(model.getBirthday());
                    user.setGender(gender);
                    user.setStatus(User.Status.PARTIAL_ACTIVE);
                    user.setSince(new Date());
                    user.save();
                    user.refresh();

                    UserUtil.confirmEmail(user, false);

                    Http.MultipartFormData.FilePart picture = data.getFile("picture");
                    return F.Promise.<Result>pure(ok());
                }
            } catch (UserAlreadyExistException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return invalidWebSession();
    }

    public static F.Promise<Result> perfil(String login) {
        return F.Promise.<Result>promise(() -> {
            try {
                User user = authenticateSession();
                boolean isMe = user.getLogin().equalsIgnoreCase(login);

                UserViewModel userVM = UserUtil.getPerfilUser(user, login, isMe);
                List<WishListViewModel> wishlistsVM = WishListUtil.getPerfilWishList(user, login, isMe);

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
