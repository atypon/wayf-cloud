package com.atypon.wayf.mail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


@Singleton
public class DefaultMailMessageSender implements MailMessageSender{


    @Inject
    @Named("mail.smtp.server")
    private
    String smtpServer;

    @Inject
    @Named("mail.smtp.port")
    private
    Integer smtpPort;

    @Inject
    @Named("mail.smtp.username")
    private
    String smtpUsername;

    @Inject
    @Named("mail.smtp.password")
    private
    String smtpPassword;

    public void send(MimeMessage message) throws MessagingException {
        Transport transport = message.getSession().getTransport("smtp");
        try {
            transport.connect(smtpServer, smtpPort, smtpUsername, smtpPassword);
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {
            transport.close();
        }
    }

    public Session newSession() {
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        return Session.getInstance(props, null);
    }


}
