package models.cloud;

import models.classes.Action;
import models.classes.Mobile;

import java.util.List;

/**
 * Classe responsável por efetuar as chamadas a Apple.
 * Esta integração deve ser feita de modo assíncrono, evitando que a pool de processos
 * seja ocupada com um processamento longo e duradouro.
 */
class ApplePushNotification implements INotificationService {

    @Override
    public void push(String title, Action action, List<Mobile> mobiles) {
        // TODO Implementar...
    }

}
