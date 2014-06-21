package models.cloud;

import models.AbstractFactory;
import models.classes.Mobile;

/**
 * Factory de Finder para serviços de notificações.
 */
public class NotificationServiceFactory extends AbstractFactory<Mobile.OS, INotificationService> {

    /**
     * Singleton para o factory.
     */
    private static NotificationServiceFactory INSTANCE;

    private NotificationServiceFactory() {
        // Do nothing...
    }

    public static NotificationServiceFactory getInstance() {
        return (INSTANCE == null ? (INSTANCE = new NotificationServiceFactory()) : INSTANCE);
    }

    @Override
    public INotificationService get(Mobile.OS id) {
        INotificationService service;
        switch (id) {
            case ANDROID:
                service = new GoogleCloudMessage();
                break;
            case IOS:
                service = new ApplePushNotification();
                break;
            case WINDOWS_PHONE:
                service = new MicrosoftPushNotification();
                break;
            default:
                service = null;
                break;
        }
        return service;
    }

}
