package models.cdn;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import models.classes.Multimedia;

import java.io.File;

/**
 * Classe responsável pela configuração da CDN da Amazon S3.
 */
public class AmazonS3CDN extends AbstractCDN<AWSCredentials> implements ICDN {

    private static final String BUCKET = "uwant-cdn";
    private static final String HOST = String.format("https://s3-sa-east-1.amazonaws.com/%s/images/", BUCKET);

    /**
     * Chave de acesso à Amazon.
     */
    private static final String KEY = "AKIAIBOPUEVG4Z7Q7X4A";

    /**
     * Secret key de acesso à Amazon.
     */
    private static final String SECRET = "ryNYZWcKLjeIjH7jch+zBRXE1e+GLYwOyYyO5WgO";

    public AmazonS3CDN() {
        super(HOST, CDNType.AMAZON_S3);
    }

    @Override
    protected AWSCredentials prepareCredentials() {
        return new AWSCredentials() {

            @Override
            public String getAWSAccessKeyId() {
                return KEY;
            }

            @Override
            public String getAWSSecretKey() {
                return SECRET;
            }

        };
    }

    @Override
    public Multimedia save(File file) {
        AWSCredentials credentials = prepareCredentials();

        String  fileName = file.getName();
        AmazonS3 s3Client = new AmazonS3Client(credentials);

        try {
            s3Client.putObject(BUCKET, String.format("images/%s", fileName), file);

            String url = HOST + fileName;
            return super.createMultimedia(fileName, url);
        } catch (AmazonClientException e) {
            e.printStackTrace();
            return null;
        }
    }

}
