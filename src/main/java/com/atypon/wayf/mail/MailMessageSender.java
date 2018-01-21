package com.atypon.wayf.mail;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public interface MailMessageSender {

    void send(MimeMessage message) throws MessagingException;

    Session newSession();

}
