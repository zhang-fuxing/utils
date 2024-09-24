package com.zhangfuxing.tools.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/24
 * @email zhangfuxing1010@163.com
 */
public class DomainResolver {
    private String domain;
    private InetAddress[] addresses;

    public static DomainResolver of(String domain) throws UnknownHostException {
        DomainResolver resolver = new DomainResolver();
        resolver.domain = domain;
        resolver.resolve();
        return resolver;
    }

    public InetAddress getFirstInetAddress() {
        return addresses[0];
    }

    public InetAddress getLastInetAddress() {
        return addresses[addresses.length - 1];
    }

    public InetAddress[] getInetAddresses() {
        return addresses;
    }

    public int getAddressCount() {
        return addresses.length;
    }

    public String getHostAddress() {
        return addresses[0].getHostAddress();
    }

    public String[] getHostAddresses() {
        String[] result = new String[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            result[i] = addresses[i].getHostAddress();
        }
        return result;
    }


    private void resolve() throws UnknownHostException {
        addresses = InetAddress.getAllByName(domain);
    }


    public IpType getIpType(InetAddress address) {
        return address instanceof Inet4Address ? IpType.IPv4 : IpType.IPv6;
    }

    public enum IpType {
        IPv4, IPv6
    }

}
