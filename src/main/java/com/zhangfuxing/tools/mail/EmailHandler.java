package com.zhangfuxing.tools.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Objects;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/23
 * @email zhangfuxing1010@163.com
 */
public class EmailHandler {
    EmailConfig config;

    public EmailHandler(EmailConfig config) {
        this.config = config;
    }

    public boolean send(EmailBody content) throws MessagingException {
        Objects.requireNonNull(config);
        Objects.requireNonNull(content);
        MimeMessage message = config.getMimeMessage();
        message.setFrom(config.getForm());
        InternetAddress[] internetAddresses = content.getInternetAddresses();
        message.addRecipients(Message.RecipientType.TO, internetAddresses);
        message.setSubject(content.getSubject());
        MimeMultipart multipart = content.getMultipart();
        message.setContent(multipart);
        Transport.send(message);
        return true;
    }

    public void setConfig(EmailConfig config) {
        this.config = config;
    }
}
