package models.cdn;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

/**
 * Created by Cleibson Gomes on 27/05/14.
 */
public class AmazonS3CDN extends AbstractCDN implements ICDN {

    private static final String HOST = "";
    private static final String BUCKET = "uwant-cdn";

    /**
     * Chave de acesso à Amazon.
     */
    private static final String KEY = "AKIAIBOPUEVG4Z7Q7X4A";

    /**
     * Secret key de acesso à Amazon.
     */
    private static final String SECRET = "ryNYZWcKLjeIjH7jch+zBRXE1e+GLYwOyYyO5WgO";

    private AWSCredentials credentials;

    public AmazonS3CDN() {
        super(HOST, CDNType.AMAZON_S3);
        this.credentials = new AWSCredentials() {

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
    protected String preparePassword() {
        return null;
    }

    @Override
    public String getPassword() {
        return preparePassword();
    }

    @Override
    public String put(File multimediaFile) {
//        String password = preparePassword();

        String  key = multimediaFile.getName();
        AmazonS3 s3Client = new AmazonS3Client(this.credentials);
        s3Client.putObject(BUCKET, key, multimediaFile);

        String url = "http://" + BUCKET + ".s3.amazonaws.com/" + key;
        return url;
    }

}
