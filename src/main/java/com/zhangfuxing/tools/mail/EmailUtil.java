package com.zhangfuxing.tools.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/16
 * @email zhangfuxing1010@163.com
 */
public class EmailUtil {
    public static void sendEmail(EmailConfig config, EmailBody emailBody) throws MessagingException {
        MimeMessage message = config.getMimeMessage();
        message.setFrom(config.getForm());
        InternetAddress[] internetAddresses = emailBody.getInternetAddresses();
        message.addRecipients(Message.RecipientType.TO, internetAddresses);
        message.setSubject(emailBody.getSubject());
        MimeMultipart multipart = emailBody.getMultipart();
        message.setContent(multipart);
        Transport.send(message);
    }
}
