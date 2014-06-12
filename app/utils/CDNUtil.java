package utils;

import models.cdn.CDNFactory;
import models.cdn.CDNType;
import models.cdn.ICDN;
import models.classes.Multimedia;

import java.io.File;

/**
 * Classe utilitária para ações relacionadas a entidades enviadas a CDN para armanezamento.
 */
public abstract class CDNUtil {

    /**
     * CDN utilizada como default para o envio quando não especificado.
     */
    private static final CDNType DEFAULT_TYPE = CDNType.AMAZON_S3;

    /**
     * Método para envio de arquivo para a CDN padrão - Amazon S3.
     * @param file
     * @return multimedia
     */
    public static Multimedia sendFile(File file) {
        return sendFile(DEFAULT_TYPE, file);
    }

    /**
     * Método para envio de arquivo para CDN específicada.
     * @param type
     * @param file
     * @return multimedia
     */
    public static Multimedia sendFile(CDNType type, File file) {
        CDNFactory factory = CDNFactory.getInstance();
        ICDN icdn = factory.get(type);
        return icdn.save(file);
    }

}
