package utils;

import models.classes.Mobile;
import models.cloud.INotificationService;
import models.cloud.NotificationServiceFactory;
import models.exceptions.UserWithoutMobileException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felipebenezi on 18/06/14.
 */
public abstract class NotificationUtil {

    public static void send(List<Mobile> mobiles) {
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
                pushAsync(os, mobiles);
            }
        }
    }

    private static void pushAsync(final Mobile.OS os, final List<Mobile> mobiles) {
        Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();
                NotificationServiceFactory factory = NotificationServiceFactory.getInstance();
                INotificationService service = factory.get(os);
                service.push(mobiles);
            }

        };
        thread.start();
    }

}
