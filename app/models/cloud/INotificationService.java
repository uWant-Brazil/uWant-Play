package models.cloud;

import models.classes.Action;
import models.classes.Mobile;
import models.classes.Notification;

import java.util.List;

/**
 * Interface utilizada para realizar ações nos serviços de
 * integração para envio de mensagens através da nuvem.
 */
public interface INotificationService {

    /**
     * Método responsável por inicializar o processo de envio.
     * @param title - Título da mensagem
     * @param action - Ação a ser enviada
     * @param mobiles - Dispositivos Móveis
     */
    void push(String title, Action action, List<Mobile> mobiles);

}
