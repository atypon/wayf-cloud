package com.atypon.wayf.mail;

import com.atypon.wayf.data.ServiceException;
import org.apache.http.HttpStatus;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;


public class Mail {

    private MailMessageSender mailMessageSender;
    private MimeMessage message;


    public Mail(MailMessageSender mailMessageSender){
        this.mailMessageSender = mailMessageSender;
        message = new MimeMessage(mailMessageSender.newSession());

    }


    protected static void setContentType(Part part, String contentType) throws MessagingException {
        if (contentType.startsWith("text/")) {
            if (!contentType.contains("charset=")) {
                contentType += "; charset=UTF-8";
            }
        }
        part.setHeader("Content-Type", contentType);
    }


    public void addRecipient(Message.RecipientType type, String address, String name) throws MessagingException {
        try {
            InternetAddress internetAddress = new InternetAddress(address, name);
            internetAddress.validate();
            message.addRecipient(type, internetAddress);
        } catch (AddressException | UnsupportedEncodingException e) {

        }
    }

    public void addTo(String address, String name) throws MessagingException {
        addRecipient(Message.RecipientType.TO, address, name);
    }

    public void addTo(String address) throws MessagingException {
        addTo(address, null);
    }

//    public void addCc(String address, String name) throws MessagingException {
//        addRecipient(Message.RecipientType.CC, address, name);
//    }
//
//    public void addBcc(String address, String name) throws MessagingException {
//        addRecipient(Message.RecipientType.BCC, address, name);
//    }

    public void addFrom(String address, String name) throws MessagingException {
        try {
            message.addFrom(new InternetAddress[]{new InternetAddress(address, name)});
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    public void addFrom(String address) throws MessagingException {
        addFrom(address, null);
    }

//    public void addReplyTo(String address, String name) throws MessagingException {
//        //there is no "addReply" in MimeMessage
//        message.addHeader("Reply-To", new InternetAddress(address).toString());
//    }

    public void setSubject(String subject) throws MessagingException {
        message.setSubject(subject);
    }


    public void setBodyText(String body) throws MessagingException {
        message.setText(body);
        setContentType(message, "text/plain");
    }

//    public void setBodyHtml(String body) throws MessagingException {
//        message.setText(body);
//        setContentType(message, "text/html");
//    }
//
//
//    public void setContent(final Multipart mp) throws MessagingException {
//        message.setContent(mp);
//    }

    public void send() throws MessagingException {
        mailMessageSender.send(message);
    }


}
