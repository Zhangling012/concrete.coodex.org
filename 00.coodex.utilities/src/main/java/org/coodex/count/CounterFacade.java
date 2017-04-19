/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.count;

import org.coodex.util.SPIFacade;

/**
 * Created by davidoff shen on 2017-04-18.
 */
public class CounterFacade {
    private static final SPIFacade<CountFacade> COUNTER_FACTORY = new SPIFacade<CountFacade>() {
    };

    /**
     * 扔一个数进去统计
     *
     * @param value
     * @param <T>
     */
    public static <T extends Countable> void count(T value) {
        COUNTER_FACTORY.getInstance().count(value);
    }
}
