package com.zhangfuxing.tools.mail;

import javax.mail.MessagingException;
import java.io.IOException;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/16
 * @email zhangfuxing1010@163.com
 */
public class EmailBodyBuilder {
    EmailBody emailBody = new EmailBody();

    public EmailBodyBuilder to(String... to) {
        emailBody.to(to);
        return this;
    }

    public EmailBodyBuilder subject(String subject) {
        emailBody.setSubject(subject);
        return this;
    }

    public EmailBodyBuilder body(String body) {
        try {
            emailBody.setBody(body);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public EmailBodyBuilder attachment(String... attachment) {
        try {
            emailBody.addArchive(attachment);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public EmailBody createEmailBody() {
        return emailBody;
    }
}
