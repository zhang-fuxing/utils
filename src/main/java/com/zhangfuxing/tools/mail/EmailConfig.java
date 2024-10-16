package com.zhangfuxing.tools.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/16
 * @email zhangfuxing1010@163.com
 */
public class EmailConfig {
    private String host;
    private int port;
    private boolean ssl;
    private boolean tls;
    private boolean auth;
    private String userName;
    private String password;
    private String form;

    EmailConfig(String host, int port, boolean ssl, boolean tls, boolean auth, String userName, String password, String form) {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.tls = tls;
        this.auth = auth;
        this.userName = userName;
        this.password = password;
        this.form = form;
    }

    public static EmailConfigBuilder create() {
        return new EmailConfigBuilder();
    }

    public MimeMessage getMimeMessage() {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", this.host);
        props.setProperty("mail.smtp.port", String.valueOf(this.port));
        props.setProperty("mail.smtp.auth", String.valueOf(auth));
        props.setProperty("mail.smtp.ssl.enable", String.valueOf(ssl));
        props.setProperty("mail.smtp.starttls.enable", String.valueOf(tls));
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });
        return new MimeMessage(session);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }
}
