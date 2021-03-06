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

package org.coodex.concrete.common;

import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.Singleton;

public class ThrowableMapperFacade {

    private static Singleton<AcceptableServiceLoader<Throwable, ThrowableMapper>> mapperLoader
            = new Singleton<AcceptableServiceLoader<Throwable, ThrowableMapper>>(
            new Singleton.Builder<AcceptableServiceLoader<Throwable, ThrowableMapper>>() {
                @Override
                public AcceptableServiceLoader<Throwable, ThrowableMapper> build() {
                    return new AcceptableServiceLoader<Throwable, ThrowableMapper>(
                            new ConcreteServiceLoader<ThrowableMapper>() {
                            }
                    );
                }
            }
    );


    public static ErrorInfo toErrorInfo(Throwable exception) {

        ConcreteException concreteException = ConcreteHelper.findException(exception);

        if (concreteException != null) {
            return new ErrorInfo(concreteException.getCode(), concreteException.getMessage());
        } else {
            ThrowableMapper mapper = mapperLoader.getInstance().getServiceInstance(exception);
            if (mapper != null) {
                return mapper.toErrorInfo(exception);
            } else {
                return new ErrorInfo(ErrorCodes.UNKNOWN_ERROR, exception.getLocalizedMessage());
            }
        }
    }
}
