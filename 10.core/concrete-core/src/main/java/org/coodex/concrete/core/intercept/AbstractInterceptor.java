/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.client.LocalServiceContext;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.common.ServerSideContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.concrete.common.TestServiceContext;
import org.coodex.concrete.core.intercept.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public abstract class AbstractInterceptor implements ConcreteInterceptor {

    private final static Logger log = LoggerFactory.getLogger(AbstractInterceptor.class);


//    private final static ThreadLocal<Object> atom = new ThreadLocal<>();


//    private final static Logger log = LoggerFactory.getLogger(AbstractInterceptor.class);


    protected final static RuntimeContext getContext(MethodInvocation joinPoint) {
        return RuntimeContext.getRuntimeContext(joinPoint.getMethod(),
                joinPoint.getThis().getClass());
    }

    @Override
    public final boolean accept(RuntimeContext context) {
        return sideAccept() && accept_(context);
    }

    private boolean sideAccept() {
        ServiceContext serviceContext = getServiceContext();
        Class<? extends AbstractInterceptor> clz = this.getClass();
        if (serviceContext instanceof ServerSideContext) {
            return clz.getAnnotation(ServerSide.class) != null;
        } else if (serviceContext instanceof ClientSideContext) {
            return clz.getAnnotation(ClientSide.class) != null;
        } else if (serviceContext instanceof LocalServiceContext) {
            return clz.getAnnotation(Local.class) != null;
        } else if (serviceContext instanceof TestServiceContext) {
            return clz.getAnnotation(TestContext.class) != null;
        } else
            return clz.getAnnotation(Default.class) != null || (
                    clz.getAnnotation(ServerSide.class) == null
                            && clz.getAnnotation(ClientSide.class) == null
                            && clz.getAnnotation(Default.class) == null);
    }

    protected abstract boolean accept_(RuntimeContext context);

    //    private boolean isAtomLevel(){
//        return false;
//    }

//    @Override
//    public Object invoke(MethodInvocation invocation) throws Throwable {
////        Object lock = atom.get();
////
////        if(!isAtomLevel() && lock != null){
////            return invocation.proceed();
////        }
//
//        RuntimeContext context = getContext(invocation);
//        if (context == null || !accept(context)) {
//            return invocation.proceed();
//        }
//
//        before(context, invocation);
//        try {
//            Object result = around(context, invocation);
//            try {
//                after(context, invocation, result);
////            }catch(ConcreteException ce){
////                throw ce;
//            } catch (Throwable t) {
//                log.warn("Error occured in afterAdvice. {}", t.getLocalizedMessage(), t);
//            } finally {
//                return result;
//            }
//        } catch (ConcreteException ce) {
//            throw ce;
//        } catch (Throwable t) {
//            Throwable t2 = onError(context, invocation, t);
//            throw t2 == null ? t : t2;
//        }
//    }

//    @Override
//    public boolean accept(RuntimeContext context) {
//        return true;
//    }

//    @Override
//    private Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
//        return joinPoint.proceed();
//    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
    }

    @Override
    public Object after(RuntimeContext context, MethodInvocation joinPoint, Object result) {
        return result;
    }

    @Override
    public Throwable onError(RuntimeContext context, MethodInvocation joinPoint, Throwable th) {
        return th;
    }


}
