package models.cloud;

import models.classes.Mobile;
import models.cloud.gcm.Message;
import models.cloud.gcm.Sender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por efetuar as chamadas ao Google Cloud Message.
 * Esta integração deve ser feita de modo assíncrono, evitando que a pool de processos
 * seja ocupada com um processamento longo e duradouro.
 */
class GoogleCloudMessage implements INotificationService {

    /**
     * Time-to-live durante o envio dos pacotes.
     */
    private static final int MAX_TTL = 20;

    /**
     * Tentativas máximas de envio da notificação.
     */
    private static final int MAX_RETRIES = 10;

    /**
     * Chave de autenticação nos servidores do Google Inc.
     */
    private static final String API_KEY = "";

    @Override
    public void push(List<Mobile> mobiles) {
        Message message = new Message.Builder()
                .timeToLive(MAX_TTL)
                .delayWhileIdle(true)
                .build();

        List<String> identifiers = new ArrayList<String>(mobiles.size());
        for (Mobile mobile : mobiles) {
            String identifier = mobile.getIdentifier();
            identifiers.add(identifier);
        }

        Sender sender = new Sender(API_KEY);
        try {
            sender.send(message, identifiers, MAX_RETRIES);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO Tratamento do erro...
        }
    }

}
