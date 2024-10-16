package com.zhangfuxing.tools.rpc;

import com.zhangfuxing.tools.rpc.anno.RpcClient;
import com.zhangfuxing.tools.rpc.anno.RpcMapping;
import com.zhangfuxing.tools.rpc.anno.RpcParam;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/10/15
 * @email zhangfuxing1010@163.com
 */
@RpcClient(address = "localhost:8080")
public interface ProjectService {

    @RpcMapping("/discovery/get")
    String getURL(@RpcParam("serviceName") String var1);

}
