package cc.coodex.concrete.core.intercept;

import cc.coodex.concrete.common.RuntimeContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public interface ConcreteInterceptor extends MethodInterceptor, InterceptOrdered {
    @Override
    Object invoke(MethodInvocation invocation) throws Throwable;

    boolean accept(RuntimeContext context);

    Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable;

    void before(RuntimeContext context, MethodInvocation joinPoint);

    Object after(RuntimeContext context, MethodInvocation joinPoint, Object result);

    Throwable onError(RuntimeContext context, MethodInvocation joinPoint, Throwable th);
}
