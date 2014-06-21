package models.cloud;

import models.classes.Mobile;

import java.util.List;

/**
 * Interface utilizada para realizar ações nos serviços de
 * integração para envio de mensagens através da nuvem.
 */
public interface INotificationService {

    /**
     * Método responsável por inicializar o processo de envio.
     * @param mobiles
     */
    void push(List<Mobile> mobiles);

}
