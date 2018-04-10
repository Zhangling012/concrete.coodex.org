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

package org.coodex.concrete.client.rx;

import org.coodex.concrete.client.ClientServiceContext;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.concrete.common.struct.AbstractUnit;

public final class ToAsyncClientContext extends ClientServiceContext {

    public ToAsyncClientContext(Destination destination, RuntimeContext context) {
        super(destination, context);
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        if(serviceContext != null) {
            this.caller = serviceContext.getCaller();
            this.courier = serviceContext.getCourier();
            this.currentUnit = serviceContext.getCurrentUnit();
            this.logging = serviceContext.getLogging();
            this.model = serviceContext.getModel();
            this.subjoin = serviceContext.getSubjoin();
        }
    }

    @Override
    protected AbstractUnit getUnit(RuntimeContext context) {
        return null;
    }
}
