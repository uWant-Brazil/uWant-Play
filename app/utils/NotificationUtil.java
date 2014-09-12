package utils;

import models.classes.Action;
import models.classes.IMobileUser;
import models.classes.Mobile;
import models.cloud.notifications.INotificationService;
import models.cloud.notifications.NotificationServiceFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitária para ações relacionadas a notificações do sistema.
 */
public abstract class NotificationUtil {

    /**
     * Título padrão para envio de notificações.
     */
    private static final String DEFAULT_TITLE = "uWant";

    /**
     * Método responsável por enviar uma notificação para um usuário a partir
     * dos dispositivos registrados para o mesmo. Além disso, utiliza um
     * título padrão para envio da notificação - "uWant".
     * @param action
     * @param mobileUser - Usuário
     */
    public static void send(Action action, IMobileUser mobileUser) {
        send(action, mobileUser.getMobiles());
    }

    /**
     * Método responsável por enviar uma notificação para um usuário a partir
     * dos dispositivos registrados para o mesmo.
     * @param title
     * @param action
     * @param mobileUser - Usuário
     */
    public static void send(String title, Action action, IMobileUser mobileUser) {
        send(title, action, mobileUser.getMobiles());
    }

    /**
     * Método responsável por enviar uma notificação para uma lista de dispositivos móveis.
     * Além disso, utiliza um título padrão nas mensagem - "uWant".
     * @param action
     * @param mobiles - Dispositivos Móveis
     */
    public static void send(Action action, List<Mobile> mobiles) {
        send(DEFAULT_TITLE, action, mobiles);
    }

    /**
     * Método responsável por enviar uma notificação para uma lista de dispositivos móveis.
     * @param title
     * @param action
     * @param mobiles - Dispositivos Móveis
     */
    public static void send(String title, Action action, List<Mobile> mobiles) {
        if (mobiles == null || mobiles.size() == 0)
            return;

        final Mobile.OS[] oses = Mobile.OS.values();
        for (Mobile.OS os : oses) {
            List<Mobile> notificationMobiles = new ArrayList<Mobile>(10);
            for (Mobile mobile : mobiles) {
                if (os == mobile.getOS()) {
                    notificationMobiles.add(mobile);
                }
            }

            if (notificationMobiles.size() > 0) {
                pushAsync(title, action, os, mobiles);
            }
        }
    }

    /**
     * Método responsável por enviar uma notificação para uma lista de dispositivos móveis
     * de forma assíncrona para um determinado sistema operacional.
     * @param title
     * @param action
     * @param os - Sistema Operacional
     * @param mobiles - Dispositivos Móveis
     */
    private static void pushAsync(String title, Action action, final Mobile.OS os, final List<Mobile> mobiles) {
        final Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();
                NotificationServiceFactory factory = NotificationServiceFactory.getInstance();
                INotificationService service = factory.get(os);
                service.push(title, action, mobiles);
            }

        };
        thread.start();
    }

}
