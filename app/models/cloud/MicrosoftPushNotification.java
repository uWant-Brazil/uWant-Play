package models.cloud;

import models.classes.Mobile;

import java.util.List;

/**
 * Classe responsável por efetuar as chamadas ao Microsoft.
 * Esta integração deve ser feita de modo assíncrono, evitando que a pool de processos
 * seja ocupada com um processamento longo e duradouro.
 */
class MicrosoftPushNotification implements INotificationService {

    @Override
    public void push(List<Mobile> mobiles) {
        // TODO Implementar...
    }

}
