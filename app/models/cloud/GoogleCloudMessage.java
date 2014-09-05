package models.cloud;

import controllers.AbstractApplication;
import models.classes.Action;
import models.classes.Mobile;
import models.classes.Notification;
import models.classes.User;
import models.cloud.gcm.Message;
import models.cloud.gcm.MulticastResult;
import models.cloud.gcm.Result;
import models.cloud.gcm.Sender;
import play.libs.Akka;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private static final String API_KEY = "AIzaSyBD_yhEGdm6vqFgx6E20INv0IUo_h_cGk8";

    /**
     * Collapse Key default do web service.
     */
    private static final String DEFAULT_COLLAPSE_KEY = "1";

    /**
     * Constante para identificação de um regId inválido.
     */
    private static final String CONST_INVALID_REGISTRATION = "Invalid registration";

    /**
     * Delay para execução do schedule em minutos.
     */
    public static final int SCHEDULE_DELAY = 15;

    @Override
    public void push(final String title, final Action action, final List<Mobile> mobiles) {
        final String uniqueIdentifier = UUID.randomUUID().toString();
        final String message = action.toString();

        final Message cloudMessage = new Message.Builder()
                .timeToLive(MAX_TTL)
                .delayWhileIdle(true)
                .collapseKey(DEFAULT_COLLAPSE_KEY)
                .addData(AbstractApplication.ParameterKey.TITLE, title)
                .addData(AbstractApplication.ParameterKey.MESSAGE, message)
                .addData(AbstractApplication.ParameterKey.IDENTIFIER, uniqueIdentifier)
                .build();

        final Sender sender = new Sender(API_KEY);
        try {
            pushExecute(title, message, action, mobiles, uniqueIdentifier, cloudMessage, sender);
        } catch (IOException e) {
            e.printStackTrace();
            
            /** Caso o envio da notificação caia em alguma exceção,
                uma nova tentativa irá ser agendada após 15 minutos. **/
            Akka.system()
                    .scheduler()
                    .scheduleOnce(Duration.create(SCHEDULE_DELAY, TimeUnit.MINUTES),
                            new Runnable() {

                                @Override
                                public void run() {
                                    push(title, action, mobiles);
                                }

                            },
                            Akka.system().dispatcher());
        }
    }

    /**
     * Método responsável por encapsular o processo de envio do GCM.
     * Ele é utilizado de forma recursiva quando houver atualizações no identificador
     * do dispositivo móvel junto ao Google Inc.
     * @param title
     * @param message
     * @param action
     * @param mobiles
     * @param uniqueIdentifier
     * @param cloudMessage
     * @param sender
     * @throws IOException
     */
    private void pushExecute(String title, String message, Action action, List<Mobile> mobiles, String uniqueIdentifier, Message cloudMessage, Sender sender) throws IOException {
        List<String> identifiers = new ArrayList<String>(mobiles.size());
        for (Mobile mobile : mobiles) {
            String identifier = mobile.getIdentifier();
            identifiers.add(identifier);
        }

        List<Mobile> retryMobiles = null;
        MulticastResult multicast = sender.send(cloudMessage, identifiers, MAX_RETRIES);
        List<Result> results = multicast.getResults();
        for (int i = 0;i < mobiles.size();i++) {
            Mobile mobile = mobiles.get(i);
            Result result = results.get(i);

            if (result.getMessageId() != null) {
                // Success...
                User user = mobile.getUser();

                Notification notification = new Notification();
                notification.setDelivered(false); // TODO Route para saber que recebeu!
                notification.setTitle(title);
                notification.setMessage(message);
                notification.setUser(user);
                notification.setUniqueIdentifier(uniqueIdentifier);
                notification.setServiceIdentifier(result.getMessageId());
                notification.setExtra(String.format("MessageId=%s;", result.getMessageId()));
                notification.setAction(action);
                notification.save();
            } else if (result.getErrorCodeName() != null) {
                // Failure...
                if (result.getErrorCodeName().contains(CONST_INVALID_REGISTRATION)) {
                    mobile.delete();
                }
            } else if (result.getCanonicalRegistrationId() != null) {
                // Failure... Update and try again!
                Mobile mobileChanged = new Mobile();
                mobileChanged.setIdentifier(result.getCanonicalRegistrationId());
                mobileChanged.update(mobile.getId());
                mobile.refresh();

                if (retryMobiles == null) {
                    retryMobiles = new ArrayList<Mobile>(10);
                }

                retryMobiles.add(mobile);
            }
        }

        if (retryMobiles != null) {
            pushExecute(title, message, action, retryMobiles, uniqueIdentifier, cloudMessage, sender);
        }
    }

}
