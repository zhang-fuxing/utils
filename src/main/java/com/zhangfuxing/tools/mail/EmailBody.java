package com.zhangfuxing.tools.mail;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/16
 * @email zhangfuxing1010@163.com
 */
public class EmailBody {
    private String[] addresses;
    private String subject;
    private final MimeMultipart multipart = new MimeMultipart();

    EmailBody() {
    }

    public static EmailBodyBuilder create() {
        return new EmailBodyBuilder();
    }

    public String[] getAddresses() {
        return addresses;
    }

    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

    public void to(String... emailAddr) {
        this.addresses = emailAddr;
    }

    public InternetAddress[] getInternetAddresses() throws AddressException {
        InternetAddress[] addresses = new InternetAddress[this.addresses.length];
        for (int i = 0; i < this.addresses.length; i++) {
            addresses[i] = new InternetAddress(this.addresses[i]);
        }
        return addresses;
    }


    public void addArchive(String... attachments) throws MessagingException, IOException {
        for (String attachment : attachments) {
            MimeBodyPart part = new MimeBodyPart();
            part.attachFile(attachment);
            this.multipart.addBodyPart(part);
        }
    }

    public MimeMultipart getMultipart() {
        return multipart;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }


    public void setBody(String body) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        part.setText(body);
        this.multipart.addBodyPart(part);
    }
}
