package models.cdn;

import models.classes.Multimedia;

/**
 * Created by Cleibson Gomes on 27/05/14.
 */
public interface ICDN {

    String getPassword();
    void put(Multimedia multimedia);

}
