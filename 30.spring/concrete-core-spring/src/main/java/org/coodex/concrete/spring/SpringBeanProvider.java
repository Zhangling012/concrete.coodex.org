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

package org.coodex.concrete.spring;

import org.coodex.concrete.common.AbstractBeanProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-09-02.
 */
public class SpringBeanProvider extends AbstractBeanProvider implements ApplicationContextAware {

    private static ApplicationContext context = null;

    @Override
    @Scheduled
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

////    @Override
//    @SuppressWarnings("unchecked")
//    public <T> T getBean(String getName) {
//        return (T) context.getBean(getName);
//    }
//
////    @Override
//    public <T> T getBean(Class<T> type, String getName) {
//        return context.getBeansOfType(type).get(getName);
//    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        return (context == null) ? new HashMap<String, T>() : context.getBeansOfType(type);
    }

}
