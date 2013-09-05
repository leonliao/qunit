/*
* $$Id$$
* Copyright (c) 2011 Qunar.com. All Rights Reserved.
*/
package com.qunar.base.qunit.transport.command;

import com.qunar.base.qunit.model.KeyValueStore;
import com.qunar.base.qunit.objectfactory.BeanUtils;
import com.qunar.base.qunit.response.Response;
import com.qunar.base.qunit.transport.model.ServiceDesc;
import com.qunar.base.qunit.transport.rpc.RpcServiceFactory;
import com.qunar.base.qunit.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class RpcExecuteCommand extends ExecuteCommand {
    private static final Logger logger = LoggerFactory.getLogger(RpcExecuteCommand.class);

    private final ServiceDesc serviceDesc;

    public RpcExecuteCommand(String id, ServiceDesc serviceDesc, String desc) {
        super(id, desc);
        this.serviceDesc = serviceDesc;
    }

    @Override
    public Response execute(List<KeyValueStore> params) {
        Response response = new Response();
        try {
            Object service = RpcServiceFactory.getRpcService(serviceDesc);
            Method executeMethod = ReflectionUtils.getMethod(serviceDesc.getMethod(), serviceDesc.getServiceClass());
            Object result = executeMethod.invoke(service,
                    BeanUtils.getParameters(params, executeMethod.getGenericParameterTypes()));
            response.setBody(result);
        } catch (InvocationTargetException ite) {
            logger.error("call rpc error", ite.getTargetException());
            response.setException(ite.getTargetException());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("illegal access", e);
        } catch (Exception e) {
            logger.error("call rpc error", e);
            response.setException(e);
        }
        return response;
    }

    @Override
    public String toReport() {
        return String.format("调用位于%s的RPC服务%s.%s, version=%s",
                serviceDesc.getUrl(),
                serviceDesc.getClazz(),
                serviceDesc.getMethod(),
                serviceDesc.getVersion());
    }

    @Override
    public com.qunar.base.qunit.model.ServiceDesc desc() {
        return new com.qunar.base.qunit.model.ServiceDesc(this.id,
                String.format("%s.%s", serviceDesc.getClazz(), serviceDesc.getMethod()),
                this.desc);
    }
}