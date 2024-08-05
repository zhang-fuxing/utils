package com.zhangfuxing.tools.lambda.meta;

import com.zhangfuxing.tools.util.ClassUtil;

import java.io.*;
import java.lang.invoke.SerializedLambda;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2024/7/29
 * @email zhangfuxing1010@163.com
 */
public class SerialLambdaMeta implements Serializable,LambdaMeta {
    @Serial
    private static final long serialVersionUID = 1L;

    final SerializedLambda lambdaMeta;

    public SerialLambdaMeta(SerializedLambda lambdaMeta) {
        this.lambdaMeta = lambdaMeta;
    }

    public SerialLambdaMeta(Serializable serializable) {
        this(extract(serializable));
    }

    public static SerializedLambda extract(Serializable serializable) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(serializable);
            oos.flush();
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    return super.resolveClass(desc);
                }

            };
            try (objectInputStream) {
                return (SerializedLambda) objectInputStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String implMethodName() {
        return lambdaMeta.getImplMethodName();
    }

    @Override
    public Class<?> instanceClass() {
        String instantiatedMethodType = lambdaMeta.getInstantiatedMethodType();
        String instanceType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(";")).replace("/",".");
        return ClassUtil.toClassConfident(instanceType, lambdaMeta.getClass().getClassLoader());
    }

}
