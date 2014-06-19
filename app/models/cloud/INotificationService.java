package models.cloud;

import models.classes.Mobile;

import java.util.List;

/**
 * Created by felipebenezi on 18/06/14.
 */
public interface INotificationService {

    void push(List<Mobile> mobiles);

}
