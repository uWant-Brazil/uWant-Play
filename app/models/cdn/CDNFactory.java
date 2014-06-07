package models.cdn;

import models.AbstractFactory;

/**
 * Created by Cleibson Gomes on 27/05/14.
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
