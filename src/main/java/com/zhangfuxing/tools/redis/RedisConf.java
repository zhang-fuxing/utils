package com.zhangfuxing.tools.redis;

import redis.clients.jedis.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.function.Supplier;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/15
 * @email zhangfuxing1010@163.com
 */
public class RedisConf implements JedisClientConfig {

    private String user;
    private String password;
    private int database;

    public RedisConf() {
    }

    public RedisConf(String password, int database) {
        this.password = password;
        this.database = database;
    }

    public RedisConf(String user, String password, int database) {
        this.user = user;
        this.password = password;
        this.database = database;
    }

    @Override
    public RedisProtocol getRedisProtocol() {
        return JedisClientConfig.super.getRedisProtocol();
    }

    @Override
    public int getConnectionTimeoutMillis() {
        return JedisClientConfig.super.getConnectionTimeoutMillis();
    }

    @Override
    public int getSocketTimeoutMillis() {
        return JedisClientConfig.super.getSocketTimeoutMillis();
    }

    @Override
    public int getBlockingSocketTimeoutMillis() {
        return JedisClientConfig.super.getBlockingSocketTimeoutMillis();
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public Supplier<RedisCredentials> getCredentialsProvider() {
        return JedisClientConfig.super.getCredentialsProvider();
    }

    @Override
    public int getDatabase() {
        return this.database;
    }

    @Override
    public String getClientName() {
        return JedisClientConfig.super.getClientName();
    }

    @Override
    public boolean isSsl() {
        return JedisClientConfig.super.isSsl();
    }

    @Override
    public SSLSocketFactory getSslSocketFactory() {
        return JedisClientConfig.super.getSslSocketFactory();
    }

    @Override
    public SSLParameters getSslParameters() {
        return JedisClientConfig.super.getSslParameters();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return JedisClientConfig.super.getHostnameVerifier();
    }

    @Override
    public HostAndPortMapper getHostAndPortMapper() {
        return JedisClientConfig.super.getHostAndPortMapper();
    }

    @Override
    public ClientSetInfoConfig getClientSetInfoConfig() {
        return JedisClientConfig.super.getClientSetInfoConfig();
    }
}
