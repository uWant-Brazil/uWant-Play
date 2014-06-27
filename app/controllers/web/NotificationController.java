package controllers.web;

import controllers.AbstractApplication;
import models.classes.Action;
import models.classes.Mobile;
import models.classes.User;
import models.database.FinderFactory;
import models.database.IFinder;
import models.exceptions.InvalidDateException;
import models.exceptions.UWException;
import models.exceptions.UserWithoutMobileException;
import play.libs.Akka;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import utils.DateUtil;
import utils.NotificationUtil;
import utils.UserUtil;
import views.html.notification;
import views.html.unauthorized;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Controlador responsável pelo tratamento de requisições relacionadas a notificações do sistema
 * que serão gerenciadas por este controlador.
 */
public class NotificationController extends AbstractApplication {

    /**
     * Método responsável pela exibição da Scala View responsável por
     * definir os parâmetros que serão enviados na notificação pelo setor administrativo.
     * @param id - Id do usuário
     * @return View
     */
    public static Result sendView(long id) {
        // FIXME Por enquanto não temos restrição de usuários efetuarem essa ação.
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<User> finder = factory.get(User.class);
        User user = finder.selectUnique(id);
        if (user != null && UserUtil.isAvailable(user)) {
            return ok(notification.render(null, user));
        }

        return unauthorized(unauthorized.render("Você não tem permissão para realizar esta ação."));
    }

    /**
     * Método responsável pelo envio da notificação para um usuário específico.
     * @param id - Id do usuário
     * @return View
     */
    public static Result send(long id) {
        FinderFactory factory = FinderFactory.getInstance();
        IFinder<User> finder = factory.get(User.class);
        User user = finder.selectUnique(id);
        if (user != null && UserUtil.isAvailable(user)) {
            Map<String, String[]> body = request().body().asFormUrlEncoded();

            if (body != null && body.containsKey(ParameterKey.TITLE)
                    && body.containsKey(ParameterKey.MESSAGE) && body.containsKey(ParameterKey.WHEN)) {
                final String title = body.get(ParameterKey.TITLE)[0];
                final String message = body.get(ParameterKey.MESSAGE)[0];

                if (!title.isEmpty() && !message.isEmpty()) {
                    String whenStr = body.get(ParameterKey.WHEN)[0];

                    final List<Mobile> mobiles = user.getMobiles();
                    final Action action = new Action();
                    action.setType(Action.Type.MESSAGE);
                    action.setCreatedAt(new Date());
                    action.setUser(user);
                    action.setFrom(finder.selectUnique(Long.valueOf(1)));
                    action.setExtra("Mensagem enviada por ZEUS!");
                    action.save();

                    try {
                        if (mobiles == null || mobiles.size() == 0)
                            throw new UserWithoutMobileException();

                        if (!whenStr.isEmpty()) {
                            Date when = DateUtil.parse(whenStr, DateUtil.DATE_HOUR_WITHOUT_SECONDS_PATTERN);
                            Date now = new Date();

                            if (now.compareTo(when) == 1) {
                                throw new InvalidDateException();
                            }

                            long dateOffset = when.getTime() - now.getTime();
                            Akka.system().scheduler().scheduleOnce(
                                    Duration.create(dateOffset, TimeUnit.MILLISECONDS),
                                    new Runnable() {

                                        @Override
                                        public void run() {
                                            NotificationUtil.send(title, action, mobiles);
                                        }

                                    }, Akka.system().dispatcher()
                            );
                        } else {
                            NotificationUtil.send(title, action, mobiles);
                        }

                        return ok(notification.render("A notificação foi enviada com sucesso!", user));
                    } catch (UWException e) {
                        e.printStackTrace();
                        return unauthorized(unauthorized.render(e.getMessage()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return unauthorized(unauthorized.render(e.getMessage()));
                    }
                }
            }
        }

        return unauthorized(unauthorized.render("Ocorreu um erro inesperado. Entre em contato com o suporte"));
    }

}
