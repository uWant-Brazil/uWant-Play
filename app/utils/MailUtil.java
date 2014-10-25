package utils;

import com.typesafe.config.ConfigFactory;
import models.exceptions.InvalidMailException;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.UUID;

/**
 * Classe utilitária para ações relacionadas ao envio de e-mail no sistema.
 */
public abstract class MailUtil {

    /**
     * Charset no envio de e-mails.
     */
    private static final String CHARSET = "utf-8";

    /**
     * Identificador dos conteúdos dos e-mails - envio de HTML.
     */
    private static final String CONTENT_TYPE = "text/html";

    private static final String DIGEST_MODE = "MD5";
    private static final String HASH_SECRET = "%1$032X";

    // Variáveis responsáveis pela configuração do envio de e-mail.
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_USER = "mail.user";
    private static final String MAIL_PASSWORD = "mail.password";
    private static final String HOST_SMTP = "smtp.gmail.com";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_SSL_PORT = "mail.smtp.socketFactory.port";
    private static final String MAIL_SMTP_SSL_CLASS = "mail.smtp.socketFactory.class";
    private static final String AUTH_TRUE = "true";
    private static final String SMTP_PORT = "465";
    private static final String SSL_CLASS = "javax.net.ssl.SSLSocketFactory";

    /**
     * E-mail remetente dos e-mails.
     */
    private static final String USERNAME = ConfigFactory.load().getString(MAIL_USER);

    /**
     * Senha do e-mail remetende dos e-mails.
     */
    private static final String PASSWORD = ConfigFactory.load().getString(MAIL_PASSWORD);

    /**
     * Classe que guardará as propriedades do envio do e-mail.
     */
    private static final Properties PROPERTIES;

    static {
        // Instanciação estática das propriedades do e-mail.
        PROPERTIES = new Properties();
        PROPERTIES.setProperty(MAIL_SMTP_HOST, HOST_SMTP);
        PROPERTIES.setProperty(MAIL_USER, USERNAME);
        PROPERTIES.setProperty(MAIL_PASSWORD, PASSWORD);
        PROPERTIES.setProperty(MAIL_SMTP_AUTH, AUTH_TRUE);
        PROPERTIES.setProperty(MAIL_SMTP_PORT, SMTP_PORT);
        PROPERTIES.setProperty(MAIL_SMTP_SSL_PORT, SMTP_PORT);
        PROPERTIES.setProperty(MAIL_SMTP_SSL_CLASS, SSL_CLASS);
    }

    /**
     * Envio do e-mail de forma sincronizada entre threads distintas.
     * @param to
     * @param subject
     * @param content
     */
    public static synchronized void send(final String to, final String subject, final String content) throws InvalidMailException {
        if (!RegexUtil.isValidMail(to))
            throw new InvalidMailException();

        Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();
                Session session = Session.getInstance(PROPERTIES, new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(USERNAME, PASSWORD);
                        }
                    });

                MimeMessage mimeMessage = new MimeMessage(session);
                try {
                    mimeMessage.setFrom(new InternetAddress(USERNAME));
                    mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                    mimeMessage.setSubject(subject, CHARSET);
                    mimeMessage.setContent(content, CONTENT_TYPE);

                    Transport.send(mimeMessage);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    /**
     * Geração de hash em criptografia MD5.
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String generateHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String hash = UUID.randomUUID().toString();
        try {
            MessageDigest m = MessageDigest.getInstance(DIGEST_MODE);
            m.update(hash.getBytes(), 0, hash.length());
            BigInteger i = new BigInteger(1, m.digest());
            hash = String.format(HASH_SECRET, i);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hash;
    }

}
