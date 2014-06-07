package models.cdn;

import models.classes.Multimedia;

import java.io.File;

/**
 * Created by Cleibson Gomes on 27/05/14.
 */
public interface ICDN {

    Multimedia save(File file);

}
