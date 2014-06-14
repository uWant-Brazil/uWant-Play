package controllers.web;

import controllers.AbstractApplication;
import models.classes.User;
import models.classes.UserMailInteraction;
import models.database.FinderFactory;
import models.database.IFinder;
import play.mvc.Result;
import views.html.*;

import java.util.Date;
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
        umi.setStatus(UserMailInteraction.Status.DONE);
        umi.update();

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
            if (umi.getStatus() == UserMailInteraction.Status.WAITING
                    && (currentTimeInMilis - ts <= MAX_TIME_AVERAGE)) {
                umi.setStatus(UserMailInteraction.Status.DONE);
                umi.update();

                User user = umi.getUser();
                return ok(recoveryPassword.render(DEFAULT_CONFIRM_MAIL_MESSAGE, user.getId()));
            } else {
                umi.setStatus(UserMailInteraction.Status.CANCELED);
                umi.update();

                return unauthorized(unauthorized.render("A sua solicitação de alteração de senha expirou!"));
            }
        }
        return unauthorized(unauthorized.render("Não existe nenhuma solicitação para alteração de senha..."));
    }

    public static Result recoveryPassword(Long id) {
        Map<String, String[]> body = request().body().asFormUrlEncoded();

        if (body != null && body.containsKey(ParameterKey.PASSWORD)) {
            String password = body.get(ParameterKey.PASSWORD)[0];

            FinderFactory factory = FinderFactory.getInstance();
            IFinder<User> finder = factory.get(User.class);
            User user = finder.selectUnique(id);
            user.setToken(null);
            user.setPassword(password);
            user.update();

            return ok(recoveryPassword.render("A sua senha foi redefinida com sucesso!", (long) -1));
        } else {
            return unauthorized(unauthorized.render("Ocorreu um erro inesperado. Entre em contato com o suporte!"));
        }
    }

}
