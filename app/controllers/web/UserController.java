package controllers.web;

import controllers.AbstractApplication;
import models.classes.Token;
import models.classes.User;
import models.classes.UserMailInteraction;
import models.database.FinderFactory;
import models.database.IFinder;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import play.mvc.Result;
import utils.UserUtil;
import views.html.recoveryPassword;
import views.html.unauthorized;
import views.html.confirmMail;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * @param ts - Milisegundos
     * @param h - Hash
     * @param m - Email
     * @return View
     */
    public static Result confirmMailView(Long ts, String h, String m) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<UserMailInteraction> finder = factory.get(UserMailInteraction.class);
        UserMailInteraction umi = finder.selectUnique(
                new String[] { FinderKey.HASH, FinderKey.MAIL },
                new Object[] { h, m });

        if (umi != null) {
            if (Hours.hoursBetween(DateTime.now(), new DateTime(ts)).getHours() > MAX_TIME_AVERAGE || umi.getStatus() == UserMailInteraction.Status.CANCELED) {
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

    /**
     * Método responsável por exibir a View que irá ser responsável por
     * capturar os dados para realizar a recuperação da senha do usuário.
     * @param ts - Milisegundos
     * @param h - Hash
     * @param m - Email
     * @return View
     */
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

}
