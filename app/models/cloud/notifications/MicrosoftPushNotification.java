package models.cloud.notifications;

import models.classes.Action;
import models.classes.Mobile;

import java.util.List;

/**
 * Classe responsável por efetuar as chamadas ao Microsoft.
 * Esta integração deve ser feita de modo assíncrono, evitando que a pool de processos
 * seja ocupada com um processamento longo e duradouro.
 */
class MicrosoftPushNotification implements INotificationService {

    @Override
    public void push(String title, Action action, List<Mobile> mobiles) {
        // TODO Implementar...
    }

}
