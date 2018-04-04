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

package org.coodex.concrete.jaxrs.client;

import org.coodex.concrete.api.ConcreteService;

/**
 * Created by davidoff shen on 2016-12-07.
 */
@Deprecated
public interface ClientInstanceFactory {

//    <T extends ConcreteService> T create(Class<? extends T> type);

    <T extends ConcreteService> T create(Class<? extends T> type, String domain, String tokenManagerKey);

    <T extends ConcreteService> T create(Class<? extends T> type, String domain);

    Invoker getInvoker(String domain, String tokenManagerKey);
}
