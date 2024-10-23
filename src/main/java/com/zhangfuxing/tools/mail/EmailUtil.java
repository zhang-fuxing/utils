package com.zhangfuxing.tools.mail;

import javax.mail.MessagingException;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/16
 * @email zhangfuxing1010@163.com
 */
public class EmailUtil {
    public static void sendEmail(EmailConfig config, EmailBody emailBody) throws MessagingException {
        EmailHandler handler = new EmailHandler(config);
        handler.send(emailBody);
    }
}
