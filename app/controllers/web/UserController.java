package controllers.web;

import controllers.AbstractApplication;
import models.classes.Multimedia;
import models.classes.Token;
import models.classes.User;
import models.classes.UserMailInteraction;
import models.cloud.forms.*;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.*;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import play.cache.Cache;
import play.data.Form;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import security.WebAuthenticator;
import utils.CDNUtil;
import utils.SecurityUtil;
import utils.UserUtil;
import utils.WishListUtil;
import views.html.confirmMail;
import views.html.recoveryPassword;
import views.html.unauthorized;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
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
                new String[] { FinderKey.HASH, FinderKey.MAIL, FinderKey.TYPE },
                new Object[] { h, m, UserMailInteraction.Type.MAIL_CONFIRMATION.ordinal() });

        if (umi != null) {
            if (Hours.hoursBetween(new DateTime(umi.getCreatedAt()), DateTime.now()).getHours() <= MAX_TIME_AVERAGE) {
                switch (umi.getStatus()) {
                    case WAITING:
                    case DONE:
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

                    default:
                        UserUtil.confirmEmail(umi.getUser(), false);
                        return unauthorized(unauthorized.render(Messages.get(MessageKey.User.CONFIRM_MAIL_EXPIRE), null));
                }
            } else {
                UserMailInteraction userMailInteraction = new UserMailInteraction();
                userMailInteraction.setStatus(UserMailInteraction.Status.CANCELED);
                userMailInteraction.update(umi.getId());

                UserUtil.confirmEmail(umi.getUser(), false);

                return unauthorized(unauthorized.render(Messages.get(MessageKey.User.CONFIRM_MAIL_EXPIRE), null));
            }
        }

        return unauthorized(unauthorized.render(Messages.get(MessageKey.User.CONFIRM_MAIL_INVALID), null));
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
     * @param h - Hash
     * @param m - Email
     * @return View
     */
    @AddCSRFToken
    public static Result showRecoveryPassword(String h, String m) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<UserMailInteraction> finder = factory.get(UserMailInteraction.class);
        UserMailInteraction umi = finder.selectUnique(
                new String[] { FinderKey.HASH, FinderKey.MAIL, FinderKey.TYPE },
                new Object[] { h, m, UserMailInteraction.Type.RECOVERY_PASSWORD.ordinal() });

        if (umi != null
                && (Hours.hoursBetween(DateTime.now(), new DateTime(umi.getCreatedAt())).getHours() <= MAX_TIME_AVERAGE)) {
            switch (umi.getStatus()) {
                case WAITING:
                    User user = umi.getUser();
                    return ok(recoveryPassword.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_WARNING), user.getId(), umi.getId()));

                default:
                    if (umi.getStatus() != UserMailInteraction.Status.CANCELED) {
                        UserMailInteraction userMailInteraction = new UserMailInteraction();
                        userMailInteraction.setStatus(UserMailInteraction.Status.CANCELED);
                        userMailInteraction.update(umi.getId());
                    }
                    return unauthorized(unauthorized.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_EXPIRE), null));
            }
        }

        return unauthorized(unauthorized.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_INVALID), null));
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
            UserMailInteraction umi = mailFinder.selectUnique(mailId);
            if (umi.getStatus() == UserMailInteraction.Status.WAITING) {
                UserMailInteraction userMailInteraction = new UserMailInteraction();
                userMailInteraction.setStatus(UserMailInteraction.Status.DONE);
                userMailInteraction.update(umi.getId());

                IFinder<User> finder = factory.get(User.class);
                IFinder<Token> finderT = factory.get(Token.class);
                User user = finder.selectUnique(id);

                List<Token> tokens = finderT.selectAll(
                        new String[] {FinderKey.USER_ID},
                        new Object[] {user.getId()});
                if (tokens != null) {
                    for (Token token : tokens) {
                        Cache.remove(token.getContent());
                        token.delete();
                    }
                }
                user.refresh();

                try {
                    password = SecurityUtil.md5(password);

                    User userChanged = new User();
                    userChanged.setPassword(password);
                    userChanged.update(user.getId());

                    return ok(recoveryPassword.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_SUCCESS), (long) -1, (long) -1));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                return unauthorized(unauthorized.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_EXPIRE), null));
            }
        }
        return unauthorized(unauthorized.render(Messages.get(MessageKey.User.RECOVERY_PASSWORD_INVALID), null));
    }

    /**
     * Método responsável por realizar o cadastro de novos usuários a partir do formulário
     * enviado pela página de autenticação/registro do sistema.
     * @return View
     */
    @RequireCSRFCheck
    public static F.Promise<Result> register() {
        Http.MultipartFormData data = request().body().asMultipartFormData();
        Form<UserRegisterViewModel> form = Form.form(UserRegisterViewModel.class).bindFromRequest(data.asFormUrlEncoded());
        if (isValidForm(form)) {
            final UserRegisterViewModel model = form.get();

            return F.Promise.<Result>promise(() -> {
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
                        if (picture != null) {
                            Multimedia multimedia = CDNUtil.sendFile(picture.getFile());

                            User u = new User();
                            u.setPicture(multimedia);
                            u.update(user.getId());
                        }

                        return ok(views.html.success.render("Parabéns, o seu usuário foi cadastrado com sucesso! Acabamos de enviar um e-mail para confirmação de sua conta. Por favor, verifique seu provedor de e-mails!"));
                    }
                } catch (UserAlreadyExistException e) {
                    e.printStackTrace();
                    return invalidWebSession("Já existe um usuário cadastrado com este login/e-mail. Tente novamente!").get(5, TimeUnit.MINUTES);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return invalidWebSession().get(5, TimeUnit.MINUTES);
            });
        }

        return invalidWebSession();
    }

    /**
     * Método responsável por exibir todos os dados do perfil do usuário logado.
     * @param login
     * @return
     */
    @AddCSRFToken
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
                    int randomIndex = random.nextInt(randomAuxVM.size());
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
