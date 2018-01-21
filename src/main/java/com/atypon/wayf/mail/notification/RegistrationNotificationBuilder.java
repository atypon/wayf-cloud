package com.atypon.wayf.mail.notification;


import com.atypon.wayf.data.publisher.registration.PublisherRegistration;
import com.atypon.wayf.mail.EmailUtil;
import com.atypon.wayf.mail.Mail;
import com.atypon.wayf.mail.MailMessageSender;

import javax.mail.MessagingException;
import java.util.List;

public class RegistrationNotificationBuilder {

    private static String MAIL_SUBJECT = "Registration Notification";
    private static String MESSAGE_BODY = "New registration received from Publisher : %s";
    private static String WAYF_EMAIL = "wayf-cloud@atypon.com";
    private static String WAYF_NAME = "WAYF";

    private MailMessageSender sender;

    private List<String> recipients;
    private PublisherRegistration publisherRegistration;


    public RegistrationNotificationBuilder(MailMessageSender sender) {
        this.sender = sender;
    }

    public Mail build() throws MessagingException {
        Mail mail = new Mail(this.sender);
        mail.setSubject(MAIL_SUBJECT);
        mail.setBodyText(String.format(MESSAGE_BODY, this.publisherRegistration.getPublisherName()));
        for (String recipient : recipients) {
            if (EmailUtil.IsValidEmailAddress(recipient))
                mail.addTo(recipient);
        }
        mail.addFrom(WAYF_EMAIL, WAYF_NAME);
        return mail;
    }

    public RegistrationNotificationBuilder addRecipients(List<String> recipients) {
        this.recipients = recipients;
        return this;
    }

    public RegistrationNotificationBuilder addPublisherRegistration(PublisherRegistration registration) {
        this.publisherRegistration = registration;
        return this;
    }

}
