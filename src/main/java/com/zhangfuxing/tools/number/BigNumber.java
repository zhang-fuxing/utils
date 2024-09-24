package com.zhangfuxing.tools.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/9/20
 * @email zhangfuxing1010@163.com
 */
public class BigNumber {
    private BigDecimal value;
    private BigInteger bigInteger;
    private boolean isInt;
    private boolean exceptionContinue = false;

    private BigNumber() {
    }

    private BigNumber(BigDecimal value) {
        this.value = value;
        this.isInt = false;
    }

    private BigNumber(BigInteger bigInteger) {
        this.bigInteger = bigInteger;
        this.isInt = false;
    }

    public static BigNumber ofInt(String value) {
        return new BigNumber(new BigInteger(value));
    }

    public static BigNumber ofDecimal(String value) {
        return new BigNumber(new BigDecimal(value));
    }

    public void setExceptionContinue(boolean exceptionContinue) {
        this.exceptionContinue = exceptionContinue;
    }

    public BigNumber add(String... otherNums) {
        for (String otherNum : otherNums) {
            try {
                if (this.isInt) this.bigInteger = this.bigInteger.add(new BigInteger(otherNum));
                this.value = value.add(new BigDecimal(otherNum));
            } catch (Exception e) {
                if (!exceptionContinue) throw new RuntimeException(e);
            }
        }
        return this;
    }

    public BigNumber subtract(String... otherNums) {
        for (String otherNum : otherNums) {
            try {
                if (this.isInt) this.bigInteger = this.bigInteger.add(new BigInteger(otherNum));
                this.value = value.subtract(new BigDecimal(otherNum));
            } catch (Exception e) {
                if (!exceptionContinue) throw new RuntimeException(e);
            }
        }
        return this;
    }

    public BigNumber multiply(String... otherNums) {
        for (String otherNum : otherNums) {
            try {
                if (this.isInt) this.bigInteger = this.bigInteger.multiply(new BigInteger(otherNum));
                this.value = value.multiply(new BigDecimal(otherNum));
            } catch (Exception e) {
                if (!exceptionContinue) throw new RuntimeException(e);
            }
        }
        return this;
    }

    public BigNumber divide(RoundingMode roundingMode, String... otherNums) {
        for (String otherNum : otherNums) {
            try {
                if (this.isInt) this.bigInteger = this.bigInteger.divide(new BigInteger(otherNum));
                this.value = value.divide(new BigDecimal(otherNum), roundingMode);
            } catch (Exception e) {
                if (!exceptionContinue) throw new RuntimeException(e);
            }
        }
        return this;
    }

    public String getStr() {
        return isInt ? bigInteger.toString() : value.toString();
    }

    public BigInteger getBigInteger() {
        return bigInteger;
    }

    public BigDecimal getBigDecimal() {
        return value;
    }

    public Double getDouble() {
        return value.doubleValue();
    }

    public Float getFloat() {
        return value.floatValue();
    }

    public Double getDouble(int scale, RoundingMode roundingMode) {
        return value.setScale(scale, roundingMode).doubleValue();
    }
}
