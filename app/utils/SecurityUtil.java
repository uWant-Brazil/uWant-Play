package utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Felipe Bonezi on 25/10/2014.
 */
public abstract class SecurityUtil {

    private static final String DIGEST_MODE = "MD5";
    private static final String HASH_SECRET = "%1$032X";
    private static final String CHARSET_NAME = "utf-8";

    /**
     * Geração de hash em criptografia utilizando MD5 e Hash Secret.
     * @return
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.UnsupportedEncodingException
     */
    public static String hash(String hash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        try {
            MessageDigest m = MessageDigest.getInstance(DIGEST_MODE);
            m.reset();
            m.update(hash.getBytes(CHARSET_NAME));
            BigInteger i = new BigInteger(1, m.digest());
            hash = String.format(HASH_SECRET, i);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    /**
     * Geração de hash em criptografia MD5.
     * @return
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.io.UnsupportedEncodingException
     */
    public static String md5(String hash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        try {
            MessageDigest m = MessageDigest.getInstance(DIGEST_MODE);
            m.reset();
            m.update(hash.getBytes(CHARSET_NAME));
            BigInteger bigInt = new BigInteger(1, m.digest());
            hash = bigInt.toString(16);
            hash = padding(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    private static String padding(String hash) {
        StringBuilder builder = new StringBuilder();
        while(hash.length() < 32){
            builder.append("0");
        }
        builder.append(hash);
        
        return builder.toString();
    }

}
