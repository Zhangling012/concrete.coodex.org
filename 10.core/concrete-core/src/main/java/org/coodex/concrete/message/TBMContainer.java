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

package org.coodex.concrete.message;

import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Singleton;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Queue;
import java.util.concurrent.*;

public class TBMContainer {

    private final static Logger log = LoggerFactory.getLogger(TBMContainer.class);


    private static Singleton<ScheduledExecutorService> scheduledExecutor = new Singleton<ScheduledExecutorService>(
            new Singleton.Builder<ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService build() {
                    return ExecutorsHelper.newScheduledThreadPool(1);
                }
            }
    );
    private static TBMContainer tbmContainer = new TBMContainer();
    private static SingletonMap<String, TBMQueue> queues = new SingletonMap<String, TBMQueue>(
            new SingletonMap.Builder<String, TBMQueue>() {
                @Override
                public TBMQueue build(String key) {
                    return new TBMQueue();
                }
            }
    );


    private TBMContainer() {
    }

    public static TBMContainer getInstance() {
        return tbmContainer;
    }

    private static void remove(String tokenId, TBMMessage message) {
        queues.getInstance(tokenId).remove(message);
        if (log.isDebugEnabled()) {
            log.debug("removed from token {}\n{}", tokenId,
                    JSONSerializerFactory.getInstance().toJson(message.message));
        }
    }

    void push(TokenBasedTopicPrototype.TokenConfirm tokenConfirm, Topic<TokenBasedTopicPrototype.ConsumedNotify> consumedNotifyTopic) {
        queues.getInstance(tokenConfirm.getTokenId()).put(new TBMMessage(tokenConfirm, consumedNotifyTopic));
    }

    void remove(TokenBasedTopicPrototype.ConsumedNotify consumedNotify) {
        queues.getInstance(consumedNotify.getTokenId());
    }

    public List<ServerSideMessage> getMessages(String tokenId, long timeOut) {
        List<TBMMessage> messageList = queues.getInstance(tokenId).peekAll(timeOut);

        List<ServerSideMessage> messages = new ArrayList<ServerSideMessage>();
        for (TBMMessage message : messageList) {
            message.consumedNotifyTopic.publish(new TokenBasedTopicPrototype.ConsumedNotify(message.id, tokenId));
            messages.add(new SSMImpl(message));
        }

        return messages;
    }

    public List<ServerSideMessage> getMessages(long timeOut) {
        String tokenId = TokenWrapper.getInstance().getTokenId();
        return getMessages(tokenId, timeOut);
    }

    static class SSMImpl implements ServerSideMessage {
        private String subject = null;
        private String host = null;
        private String id;
        private Object body;

        SSMImpl(TBMMessage message) {
            this.id = message.id;
//            this.host = // TODO 获得当前主机编号
            this.body = message.message;
            if (message.message instanceof Subject) {
                this.subject = ((Subject) message.message).getSubject();
            }
        }


        @Override
        public String getSubject() {
            return subject;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public Object getBody() {
            return body;
        }
    }


    static class TBMMessage {
        private Object message;
        private String id;
        private Future future;
        private Topic<TokenBasedTopicPrototype.ConsumedNotify> consumedNotifyTopic;


        public TBMMessage(final TokenBasedTopicPrototype.TokenConfirm tokenConfirm, Topic<TokenBasedTopicPrototype.ConsumedNotify> consumedNotifyTopic) {
            this.consumedNotifyTopic = consumedNotifyTopic;
            this.id = tokenConfirm.getId();
            this.message = tokenConfirm.getMessage();
            this.future = scheduledExecutor.getInstance().schedule(
                    new Runnable() {
                        @Override
                        public void run() {
                            remove(tokenConfirm.getTokenId(), TBMMessage.this);
                        }
                    }, 5, TimeUnit.MINUTES); // 5分钟以后失效
        }
    }

    static class TBMQueue {
        private Queue<TBMMessage> queue = new LinkedBlockingQueue<TBMMessage>();
        private Map<String, TBMMessage> index = new ConcurrentHashMap<String, TBMMessage>();

        private Object lock = new Object();

        void remove(TBMMessage message) {
            if (queue.contains(message)) {
                synchronized (queue) {
                    if (queue.contains(message)) {
                        queue.remove(message);
                        index.remove(message.id);
                    }
                }
            }
        }

        public void put(TBMMessage message) {
            synchronized (queue) {
                queue.add(message);
                index.put(message.id, message);
                synchronized (lock) {
                    queue.notifyAll();
                }
            }
        }

        public void remove(String id) {
            if (index.containsKey(id)) {
                synchronized (queue) {
                    remove(index.get(id));
                }
            }
        }


        public List<TBMMessage> peekAll(long timeOut) {
            List<TBMMessage> list = null;

            if (queue.size() == 0 && timeOut > 0) {
                synchronized (queue) {
                    try {
                        queue.wait(timeOut);
                    } catch (InterruptedException e) {
                        log.warn(e.getLocalizedMessage(), e);
                    }
                }
            }

            if (queue.size() > 0) {
                synchronized (lock) {
                    list = Arrays.asList(queue.toArray(new TBMMessage[0]));
                    queue.clear();
                    index.clear();
                    for (TBMMessage message : list) {
                        message.future.cancel(true);
                    }
                }
            }

            return list == null ? Collections.<TBMMessage>emptyList() : list;
        }
    }

}
