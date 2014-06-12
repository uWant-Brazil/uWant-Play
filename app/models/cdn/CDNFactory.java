package models.cdn;

import models.AbstractFactory;

/**
 * Factory responsável por disponibilizar todos as CDN's baseado no tipo informado.
 */
public class CDNFactory extends AbstractFactory<CDNType, ICDN>{

    /**
     * Singleton para o factory.
     */
    private static CDNFactory INSTANCE;

    private CDNFactory() {
        // Do nothing...
    }

    public static CDNFactory getInstance() {
        return (INSTANCE == null ? (INSTANCE = new CDNFactory()) : INSTANCE);
    }

    @Override
    public ICDN get(CDNType id) {
        ICDN icdn;

        switch (id) {
            case AMAZON_S3:
                icdn = new AmazonS3CDN();
                break;

            default:
                icdn = null;
                break;
        }

        return icdn;
    }
}
