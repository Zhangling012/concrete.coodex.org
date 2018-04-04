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

package org.coodex.pojomocker.mockers;

import org.coodex.pojomocker.AbstractPrimitiveMocker;
import org.coodex.pojomocker.annotations.FLOAT;
import org.coodex.util.Common;

/**
 * Created by davidoff shen on 2017-05-15.
 */
public class DefaultFloatMocker extends AbstractPrimitiveMocker<Float, FLOAT> {
    @Override
    protected Object toPrimitive(Float aFloat) {
        return aFloat == null ? 0.0f : aFloat.floatValue();
    }

    @Override
    protected Float $mock(FLOAT mockAnnotation) {
        return (mockAnnotation.range() != null && mockAnnotation.range().length > 0) ?
                Common.random(mockAnnotation.range()) :
                (float) Common.random(mockAnnotation.min(), mockAnnotation.max());
    }
}
