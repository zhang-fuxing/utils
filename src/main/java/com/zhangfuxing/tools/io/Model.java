package com.zhangfuxing.tools.io;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/8/1
 * @email zhangfuxing1010@163.com
 */
public enum Model {
    /**
     * 只读
     */
    R,

    /**
     * 可读可写,如果文件不存在，则将尝试创建它
     */
    RW,

    /**
     * 可读写，要求对文件内容或元数据的每次更新都同步写入底层存储设备
     */
    RWS,

    /**
     * 可读写，要求对“文件的内容”的每个更新都同步写入到基础存储设备
     */
    RWD;
}
