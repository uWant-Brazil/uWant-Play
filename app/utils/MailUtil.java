package utils;

import com.typesafe.config.ConfigFactory;
import models.exceptions.InvalidMailException;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

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
    private static final String MAIL_PROTOCOL = "mail.transport.protocol";
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_USER = "mail.smtp.user";
    private static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    private static final String MAIL_USER = "mail.user";
    private static final String MAIL_PASSWORD = "mail.password";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_SSL_PORT = "mail.smtp.socketFactory.port";
    private static final String MAIL_SMTP_SSL_CLASS = "mail.smtp.socketFactory.class";
    private static final String AUTH_TRUE = "true";
    private static final String SMTP = "smtp";
    private static final String SSL_CLASS = "javax.net.ssl.SSLSocketFactory";

    /**
     * Endereço para envio dos e-mails.
     */
    private static final String HOST_SMTP = ConfigFactory.load().getString(MAIL_SMTP_HOST);

    /**
     * Porta do SMTP Host.
     */
    private static final String SMTP_PORT = ConfigFactory.load().getString(MAIL_SMTP_PORT);

    /**
     * E-mail remetente dos e-mails.
     */
    private static final String SES_USERNAME = ConfigFactory.load().getString(MAIL_SMTP_USER);

    /**
     * E-mail remetente dos e-mails.
     */
    private static final String SES_PASSWORD = ConfigFactory.load().getString(MAIL_SMTP_PASSWORD);

    /**
     * E-mail remetente dos e-mails.
     */
    private static final String USERNAME = ConfigFactory.load().getString(MAIL_USER);

    /**
     * Classe que guardará as propriedades do envio do e-mail.
     */
    private static final Properties PROPERTIES;

    static {
        // Instanciação estática das propriedades do e-mail.
        PROPERTIES = new Properties();
        PROPERTIES.setProperty(MAIL_PROTOCOL, SMTP);
        PROPERTIES.setProperty(MAIL_SMTP_HOST, HOST_SMTP);
        PROPERTIES.setProperty(MAIL_USER, SES_USERNAME);
        PROPERTIES.setProperty(MAIL_PASSWORD, SES_PASSWORD);
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
                            return new PasswordAuthentication(SES_USERNAME, SES_PASSWORD);
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

}
