package controllers.web;

import controllers.AbstractApplication;
import models.classes.Token;
import models.classes.User;
import models.classes.UserMailInteraction;
import models.database.FinderFactory;
import models.database.IFinder;
import play.mvc.Result;
import views.html.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserController extends AbstractApplication {

    private static final String DEFAULT_CONFIRM_MAIL_MESSAGE = "Não se esqueça da sua senha!";
    private static final long MAX_TIME_AVERAGE = 604800000;

    public static Result confirmMail(Long ts, String h, String m) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<UserMailInteraction> finder = factory.get(UserMailInteraction.class);
        UserMailInteraction umi = finder.selectUnique(
                new String[] { FinderKey.HASH, FinderKey.MAIL },
                new Object[] { h, m });

        UserMailInteraction userMailInteraction = new UserMailInteraction();
        userMailInteraction.setStatus(UserMailInteraction.Status.DONE);
        userMailInteraction.update(umi.getId());

        return ok("E-mail confirmado com sucesso!");
    }

    public static Result showRecoveryPassword(Long ts, String h, String m) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<UserMailInteraction> finder = factory.get(UserMailInteraction.class);
        UserMailInteraction umi = finder.selectUnique(
                new String[] { FinderKey.HASH, FinderKey.MAIL },
                new Object[] { h, m });

        Date now = new Date();
        long currentTimeInMilis = now.getTime();
        if (umi != null) {
            if ((currentTimeInMilis - ts > MAX_TIME_AVERAGE) || umi.getStatus() == UserMailInteraction.Status.CANCELED) {
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
                return ok(recoveryPassword.render(DEFAULT_CONFIRM_MAIL_MESSAGE, user.getId(), umi.getId()));
            }
        }

        return unauthorized(unauthorized.render("Não existe nenhuma solicitação para alteração de senha..."));
    }

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
