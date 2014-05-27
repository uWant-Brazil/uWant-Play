package models.cdn;

import models.AbstractFactory;

/**
 * Created by Cleibson Gomes on 27/05/14.
 */
public class CDNFactory extends AbstractFactory<AbstractCDN.Type, ICDN>{
    @Override
    public ICDN get(AbstractCDN.Type id) {
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
