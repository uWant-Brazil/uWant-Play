package models.cdn;

import models.classes.Multimedia;

import java.io.File;

/**
 * Interfade responsável por mapear CDN's.
 */
public interface ICDN {

    /**
     * Método responsável por salvar determinado arquivo na CDN.
     * @param file - Arquivo a ser salvo.
     * @param description - Descrição do arquivo.
     * @return multimedia
     */
    Multimedia save(File file, String description);

}
