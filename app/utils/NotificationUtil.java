package utils;

import models.classes.Mobile;
import models.cloud.INotificationService;
import models.cloud.NotificationServiceFactory;
import models.exceptions.UserWithoutMobileException;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitária para ações relacionadas a notificações do sistema.
 */
public abstract class NotificationUtil {

    /**
     * Método responsável por enviar uma notificação para uma lista de usuários.
     * @param title
     * @param message
     * @param mobiles - Dispositivos Móveis
     */
    public static void send(String title, String message, List<Mobile> mobiles) {
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
                pushAsync(title, message, os, mobiles);
            }
        }
    }

    /**
     * Método responsável por enviar uma notificação para uma lista de usuários
     * de forma assíncrona para um determinado sistema operacional.
     * @param title
     * @param message
     * @param os - Sistema Operacional
     * @param mobiles - Dispositivos Móveis
     */
    private static void pushAsync(String title, String message, final Mobile.OS os, final List<Mobile> mobiles) {
        Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();
                NotificationServiceFactory factory = NotificationServiceFactory.getInstance();
                INotificationService service = factory.get(os);
                service.push(title, message, mobiles);
            }

        };
        thread.start();
    }

}
