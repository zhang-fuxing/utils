package com.zhangfuxing.tools.common.enums;

/**
 * @author zhangfx
 * @date 2023/3/24
 */
public class JoinStr {
    private String character;
    private String prefix;
    private String suffix;

    public JoinStr() {
    }

    public JoinStr(String character) {
        this.character = character;
    }

    public String getCharacter() {
        return character;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public JoinStr(String character, String prefix, String suffix) {
        this.character = character;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public static JoinStr of(String c) {
        return new JoinStr(c);
    }

    public static JoinStr of(String c, String s, String e) {
        return new JoinStr(c, s, e);
    }
}
