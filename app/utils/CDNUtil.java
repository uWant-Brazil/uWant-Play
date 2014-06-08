package utils;

import models.cdn.CDNFactory;
import models.cdn.CDNType;
import models.cdn.ICDN;
import models.classes.Multimedia;

import java.io.File;

/**
 * Created by felipebenezi on 08/06/14.
 */
public abstract class CDNUtil {

    private static final CDNType DEFAULT_TYPE = CDNType.AMAZON_S3;

    public static Multimedia sendFile(File file) {
        return sendFile(DEFAULT_TYPE, file);
    }

    public static Multimedia sendFile(CDNType type, File file) {
        CDNFactory factory = CDNFactory.getInstance();
        ICDN icdn = factory.get(type);
        return icdn.save(file);
    }

}
