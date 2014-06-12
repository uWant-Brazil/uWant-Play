package utils;

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

    // Variáveis responsáveis pela configuração do envio de e-mail.
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_USER = "mail.user";
    private static final String MAIL_PASSWORD = "mail.password";
    private static final String HOST_SMTP = "smtp.uwant.com.br";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String AUTH_TRUE = "true";
    private static final String SMTP_PORT = "587";

    /**
     * E-mail remetente dos e-mails.
     */
    private static final String USERNAME = "no-reply@uwant.com.br";

    /**
     * Senha do e-mail remetende dos e-mails.
     */
    private static final String PASSWORD = "n@o-43$90nda";

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
                SecurityManager security = System.getSecurityManager();
                Session session;
                if (security == null) {
                    session = Session.getInstance(PROPERTIES, new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(USERNAME, PASSWORD);
                        }
                    });
                } else {
                    session = Session.getDefaultInstance(PROPERTIES, new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(USERNAME, PASSWORD);
                        }
                    });
                }

                MimeMessage mimeMessage = new MimeMessage(session);
                try {
                    mimeMessage.setFrom(USERNAME);
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
        String key = UUID.randomUUID().toString();
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(key.getBytes("utf-8"));

        String hash = "no-hash-provided";
        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
            m.update(hash.getBytes(), 0, hash.length());
            BigInteger i = new BigInteger(1, m.digest());
            hash = String.format("%1$032X", i);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hash;
    }

}
