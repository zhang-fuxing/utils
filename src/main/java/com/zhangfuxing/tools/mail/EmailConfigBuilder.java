package com.zhangfuxing.tools.mail;

public class EmailConfigBuilder {
    private String host;
    private int port;
    private boolean ssl;
    private boolean tls;
    private boolean auth;
    private String userName;
    private String password;
    private String form;

    public EmailConfigBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public EmailConfigBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public EmailConfigBuilder setSsl(boolean ssl) {
        this.ssl = ssl;
        return this;
    }

    public EmailConfigBuilder setTls(boolean tls) {
        this.tls = tls;
        return this;
    }

    public EmailConfigBuilder setAuth(boolean auth) {
        this.auth = auth;
        return this;
    }

    public EmailConfigBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public EmailConfigBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public EmailConfigBuilder setForm(String form) {
        this.form = form;
        return this;
    }

    public EmailConfig createEmailConfig() {
        return new EmailConfig(host, port, ssl, tls, auth, userName, password, form);
    }
}