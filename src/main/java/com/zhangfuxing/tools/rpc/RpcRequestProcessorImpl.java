package com.zhangfuxing.tools.rpc;

import com.zhangfuxing.tools.http.HttpRequestBuilder;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/12/5
 * @email zhangfuxing1010@163.com
 */
public class RpcRequestProcessorImpl implements RpcRequestProcessor {

    @Override
    public void processor(HttpRequestBuilder builder) {
        System.out.println("RpcRequestProcessorImpl processor......");
    }
}
