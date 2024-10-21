package com.zhangfuxing.tools.rpc;
/*
 * 通过Java接口定义Http接口，使用RpcService.getService(Class<?> serviceClass) 获取实例，通过实例调用方法可实现http调用
 *
 * 接口预定义参数：RpcCookie，RpcHeader，HttpRequest.BodyPublisher，HttpResponse.BodyHandler<?>
 *
 * 示例：
 * @RpcClient(port=8080, schema="http", host="127.0.0.1")
 * class ServiceImpl {
 *      @RpcMapping(value = "/index", method = Method.GET)
 *      <T> HttpResponse<T> test(@RpcParam("_a") String param,RpcCookie cookie, RpcHeader header, HttpRequest.BodyPublisher, HttpResponse.BodyHandler<T> bodyHandler);
 * }
 *
 * 使用 RpcCookie 进行Cookie传递，RpcHeader 进行请求的 header 传递，HttpRequest.BodyPublisher可以自定义请求体，HttpResponse.BodyHandler可以自定义响应体
 * 也可以使用注解进行参数绑定： @RpcParam 进行URL参数传递，使用 ？ 进行拼接，@RpcBody 进行请求体参数绑定，默认传递的请求体是JSON格式，可修改
 *
 * ServiceImpl service = RpcService.getService(ServiceImpl);
 * HttpResponse<T> result = service.test(..);
 * 上面的接口生成的http接口： http://127.0.0.1:8080/index?_a=${param}
 *
 */