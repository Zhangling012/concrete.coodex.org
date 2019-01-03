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

package org.coodex.concrete.spring.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.coodex.concrete.core.intercept.SyncInterceptorChain;
import org.coodex.config.Config;
import org.springframework.core.Ordered;

import java.util.List;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.spring.aspects.AspectJHelper.ASPECT_POINT;

/**
 * Created by davidoff shen on 2016-09-07.
 */
@Aspect
public class ConcreteAOPChain extends SyncInterceptorChain implements Ordered {

//    private static final Profile_Deprecated profile = ConcreteHelper.getProfile();

    public ConcreteAOPChain() {

    }

    public ConcreteAOPChain(List<ConcreteInterceptor> interceptors) {
        super(interceptors);
    }

    @Override
    public int getOrder() {
        return Config.getValue(ConcreteAOPChain.class.getCanonicalName() + ".order", 0, getAppSet());
    }

    @Around(ASPECT_POINT)
    public Object weaverPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        return invoke(AspectJHelper.proceedJoinPointToMethodInvocation(joinPoint));
    }
}
