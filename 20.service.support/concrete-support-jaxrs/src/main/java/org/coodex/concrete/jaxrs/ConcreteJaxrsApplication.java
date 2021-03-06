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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.AbstractErrorCodes;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.ErrorMessageFacade;
import org.coodex.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteHelper.foreachClassInPackages;

public abstract class ConcreteJaxrsApplication extends Application implements org.coodex.concrete.api.Application {

    protected static final JaxRSModuleMaker moduleMaker = new JaxRSModuleMaker();
    private final static Logger log = LoggerFactory.getLogger(ConcreteJaxrsApplication.class);
    private Set<Class<? extends ConcreteService>> servicesClasses = new HashSet<Class<? extends ConcreteService>>();
    private Set<Class<?>> jaxrsClasses = new HashSet<Class<?>>();
    private Set<Object> singletonInstances = new HashSet<Object>();
    private Set<Class<?>> othersClasses = new HashSet<Class<?>>();

    private Application application = null;
    private boolean exceptionMapperRegisted = false;

    public ConcreteJaxrsApplication() {
        super();
        registerDefault();
    }

    public ConcreteJaxrsApplication(Application application) {
        this.application = application;
        registerDefault();
    }

    @SuppressWarnings({"unsafe", "unchecked"})
    public Set<Class<? extends ConcreteService>> getServicesClasses() {
        return servicesClasses;
    }

    public Set<Class<?>> getJaxrsClasses() {
        return jaxrsClasses;
    }

    public Set<Object> getSingletonInstances() {
        return singletonInstances;
    }

    public Set<Class<?>> getOthersClasses() {
        return othersClasses;
    }

    public Application getApplication() {
        return application;
    }

    protected abstract ClassGenerator getClassGenerator();

    protected void registerDefault() {
        register(Polling.class);
        registerPackage(ErrorCodes.class.getPackage().getName());
    }

    @Override
    public void registerPackage(String... packages) {

        foreachClassInPackages(new ReflectHelper.Processor() {
            @Override
            public void process(Class<?> serviceClass) {
                registerClass(serviceClass);
            }
        }, packages);
    }

    @Override
    public void register(Class<?>... classes) {
        for (Class<?> clz : classes) {
            registerClass(clz);
        }
    }

    private Object newInstance(Class<?> clz) {
        try {
            return clz.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable th) {
            throw new UnableNewInstanceException(th);
        }
    }


    protected void registerConcreteService(Class<? extends ConcreteService> concreteServiceClass) {
        if (!servicesClasses.contains(concreteServiceClass)) {
            servicesClasses.add(concreteServiceClass);
            Class<?> jaxrs = generateJaxrsClass(concreteServiceClass);

            if (jaxrs != null) {
                if (log.isDebugEnabled()) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("\n\tclassName: ").append(jaxrs.getName()).append(";");
                    for (Method method : jaxrs.getMethods()) {
                        if (Object.class.equals(method.getDeclaringClass())) continue;
                        builder.append("\n\t\tmethod: ").append(method.getName()).append("(");
                        for (int i = 0; i < method.getGenericParameterTypes().length; i++) {
                            if (i > 0) builder.append(", ");
                            if (method.getParameterAnnotations()[i] != null)
                                for (Annotation annotation : method.getParameterAnnotations()[i]) {
                                    builder.append(annotation.annotationType().getName())
                                            .append(" ");
                                }
                            builder.append(method.getParameterTypes()[i].toString());
                        }
                        builder.append(");");
                    }
                    log.debug("class info:{}", builder.toString());
                }
                jaxrsClasses.add(jaxrs);
//                singletonInstances.add(newInstance(jaxrs));
            }
        }
    }

    protected Class<?> generateJaxrsClass(Class<?> concreteServiceClass) {
        try {
            return getClassGenerator().generatesImplClass(moduleMaker.make(concreteServiceClass));
        } catch (RuntimeException t) {
            throw t;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void registerClass(Class<?> clz) {
        if (ConcreteHelper.isConcreteService(clz)) {
            registerConcreteService((Class<? extends ConcreteService>) clz);
        } else if (AbstractErrorCodes.class.isAssignableFrom(clz)) {
            ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) clz);
        } else {
            if(ConcreteExceptionMapper.class.isAssignableFrom(clz)){
                exceptionMapperRegisted = true;
            }
            if (!othersClasses.contains(clz)) {
                othersClasses.add(clz);
                if (clz.getAnnotation(Provider.class) != null) {
                    singletonInstances.add(newInstance(clz));
                }
            }
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        if(!exceptionMapperRegisted){
            register(ConcreteExceptionMapper.class);
        }
        Set<Class<?>> set = new HashSet<Class<?>>();
        if (application != null) {
            set.addAll(application.getClasses());
        }
        set.addAll(jaxrsClasses);
        set.addAll(othersClasses);
        return Collections.unmodifiableSet(set);
    }

    @Override
    public Set<Object> getSingletons() {
        if(!exceptionMapperRegisted){
            register(ConcreteExceptionMapper.class);
        }
        Set<Object> set = new HashSet<Object>();
        if (application != null) {
            set.addAll(application.getSingletons());
        }
        set.addAll(singletonInstances);
        return Collections.unmodifiableSet(set);
    }

    private class UnableNewInstanceException extends RuntimeException {
        public UnableNewInstanceException(Throwable th) {
            super(th);
        }
    }

}
