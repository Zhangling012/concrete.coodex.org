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

package org.coodex.pojomocker.mockers;

import org.coodex.pojomocker.AbstractPrimitiveMocker;
import org.coodex.pojomocker.annotations.SHORT;

/**
 * Created by davidoff shen on 2017-05-15.
 */
public class DefaultShortMocker extends AbstractPrimitiveMocker<Short, SHORT> {
    @Override
    protected Object toPrimitive(Short aShort) {
        return aShort == null ? (short)0 : aShort.shortValue();
    }

    @Override
    protected Short $mock(SHORT mockAnnotation) {
        short min = mockAnnotation.min();
        short max = mockAnnotation.max();
        boolean positive = true;
        if(min == max) return min;
        if(min > max){
            short t = min;
            min = max;
            max = t;
        }

        if(min <0 && max > 0){
            if(Math.random() < 0.5){
                max = 0;
                positive = false;
            } else {
                min = 0;
            }
        }

        return (short)(Math.random() * (max - min) * (positive ?  1 : -1));
    }
}