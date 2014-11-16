package utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Classe utilitária para ações relacionadas a segurança do sistema (e.g. criptografias, etc, etc).
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

    /**
     * Padding para hash do md5 não ficar incompleto.
     * @param md5
     * @return
     */
    private static String padding(String md5) {
        StringBuilder builder = new StringBuilder();
        while(md5.length() < 32){
            builder.append("0");
        }
        builder.append(md5);
        
        return builder.toString();
    }

}
