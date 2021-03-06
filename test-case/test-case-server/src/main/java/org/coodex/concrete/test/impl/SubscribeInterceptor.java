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

package org.coodex.concrete.test.impl;

import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.intercept.AbstractTokenBasedTopicSubscribeInterceptor;
import org.coodex.concrete.message.MessageConsumer;
import org.coodex.concrete.message.MessageFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@MessageConsumer(queue = "test")
public class SubscribeInterceptor extends AbstractTokenBasedTopicSubscribeInterceptor<TestSubject> {
    public static final String SUBSCRIBED = "org.coodex.token.subscribed";
    public static final String SUBSCRIBED_NUMBER_KEY = "X1";
    private final static Logger log = LoggerFactory.getLogger(SubscribeInterceptor.class);

//    @Queue("test")
//    private TokenBasedTopic<TestSubject> tokenBasedTopic;

    @Inject
    private Token token;

//    @Override
//    protected Subscription subscribe() {
//        Token token = TokenWrapper.getInstance();
//        Subscription subscription = null;
//        log.debug(" tokenid: {}, x1", token.getTokenId(), token.getAttribute(SUBSCRIBED_NUMBER_KEY, Integer.class));
//        if (token.isValid()
//                && token.getAttribute(SUBSCRIBED, Object.class) == null
//                && token.getAttribute(SUBSCRIBED_NUMBER_KEY, Object.class) != null) {
//            synchronized (tokenBasedTopic) {
//                if (token.isValid()
//                        && token.getAttribute(SUBSCRIBED, Object.class) == null
//                        && token.getAttribute(SUBSCRIBED_NUMBER_KEY, Object.class) != null) {
//                    final Integer x1 = token.getAttribute(SUBSCRIBED_NUMBER_KEY, Integer.class);
//                    subscription = tokenBasedTopic.subscribe(new MessageFilter<TestSubject>() {
//                        @Override
//                        public boolean handle(TestSubject message) {
//                            return message.getNumber() == x1;
//                        }
//                    });
//                    log.debug("TestSubject subscribed.");
//
//                    token.setAttribute(SUBSCRIBED, Boolean.valueOf(true));
//                }
//            }
//        }
//        return subscription;
//    }


    @Override
    protected boolean checkAccountCredible() {
        return false;
    }

    @Override
    protected MessageFilter<TestSubject> subscribe() {
        final Integer x1 = token.getAttribute(SUBSCRIBED_NUMBER_KEY, Integer.class);

        token.setAttribute(SUBSCRIBED, Boolean.valueOf(true));
        log.debug("TestSubject subscribed.");
        return new MessageFilter<TestSubject>() {
            @Override
            public boolean handle(TestSubject message) {
                return message.getNumber() == x1;
            }
        };


    }

    @Override
    protected boolean check() {
        return token.getAttribute(SUBSCRIBED, Object.class) == null
                && token.getAttribute(SUBSCRIBED_NUMBER_KEY, Object.class) != null;
    }

}
