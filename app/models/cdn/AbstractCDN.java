package models.cdn;

import models.classes.Multimedia;

/**
 * Classe abstrata para herança de todas as CDN's.
 */
abstract class AbstractCDN<K> {

    /**
     * URL para a CDN externa.
     */
    private String host;

    /**
     * Tipo da CDN.
     */
    private CDNType type;

    public AbstractCDN(String host, CDNType type) {
        this.host = host;
        this.type = type;
    }

    /**
     * Método responsável por disponibilizar entidade com credenciais de autenticação da CDN.
     * @return
     */
    protected abstract K prepareCredentials();

    /**
     * Método responsável por persistir o arquivo multimídia.
     * @param fileName
     * @param url
     * @param description
     * @return
     */
    protected Multimedia createMultimedia(String fileName, String url, String description) {
        Multimedia multimedia = new Multimedia();
        multimedia.setDescription(description);
        multimedia.setFileName(fileName);
        multimedia.setUrl(url);
        multimedia.setCdn(this.type);
        multimedia.save();
        multimedia.refresh();
        return multimedia;
    }

    /**
     * Getter para a URL da CDN;
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * Getter para o tipo da CDN.
     * @return
     */
    public CDNType getType() {
        return type;
    }

}
