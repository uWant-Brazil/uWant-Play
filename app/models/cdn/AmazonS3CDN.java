package models.cdn;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

/**
 * Created by Cleibson Gomes on 27/05/14.
 */
public class AmazonS3CDN extends AbstractCDN<AWSCredentials> implements ICDN {

    private static final String BUCKET = "uwant-cdn";
    private static final String HOST = "http://" + BUCKET + ".s3.amazonaws.com/";

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
    public String asyncPut(File multimediaFile) {
        AWSCredentials credentials = prepareCredentials();

        String  key = multimediaFile.getName();
        AmazonS3 s3Client = new AmazonS3Client(credentials);
        s3Client.putObject(BUCKET, key, multimediaFile);

        String url = HOST + key;
        return url;
    }

}
