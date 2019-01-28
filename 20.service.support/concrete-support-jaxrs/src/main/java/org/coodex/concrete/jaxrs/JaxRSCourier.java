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

import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.core.messages.Courier;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Clock;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Deprecated
public class JaxRSCourier implements Courier {

    private final static Logger log = LoggerFactory.getLogger(JaxRSCourier.class);
    private final static long MAX_LIFE = 5 * 60 * 1000;
    private final static Map<String, Queue<MessageWithArrived>> queueMap = new HashMap<String, Queue<MessageWithArrived>>();
    private final static Map<Queue<MessageWithArrived>, Set<AsyncMessageReceiver>> ASYNC_MESSAGE_GETTER_MAP
            = new HashMap<Queue<MessageWithArrived>, Set<AsyncMessageReceiver>>();
    //    private static ScheduledExecutorService scheduledExecutorService;
    private static Singleton<ScheduledExecutorService> scheduledExecutorService =
            new Singleton<ScheduledExecutorService>(new Singleton.Builder<ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService build() {
                    return ExecutorsHelper.newSingleThreadScheduledExecutor();
                }
            });

    public JaxRSCourier() {
//        synchronized (JaxRSCourier.class) {
//            if (scheduledExecutorService == null)
//                scheduledExecutorService = ExecutorsHelper.newSingleThreadScheduledExecutor();
//        }
    }

    private static ScheduledExecutorService getScheduledExecutorService() {
//        if (scheduledExecutorService == null)
//            synchronized (JaxRSCourier.class) {
//                if(scheduledExecutorService == null) {
//                    scheduledExecutorService = ExecutorsHelper.newSingleThreadScheduledExecutor();
//                }
//            }
//        return scheduledExecutorService;
        return scheduledExecutorService.getInstance();
    }

    // TODO 性能优化
    private static synchronized Queue<MessageWithArrived> getQueue(String tokenId) {
        if (!queueMap.containsKey(tokenId)) {
            Queue<MessageWithArrived> queue = new LinkedBlockingQueue<MessageWithArrived>();
            queueMap.put(tokenId, queue);
            clean(queue, Clock.currentTimeMillis() + MAX_LIFE);
        }
        return queueMap.get(tokenId);
    }

    private static void clean(final Queue<MessageWithArrived> queue, final long deathLine) {
        long time = Math.max(deathLine - Clock.currentTimeMillis(), 1000);
        getScheduledExecutorService().schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (queue) {
                    MessageWithArrived head = queue.peek();
                    while (head != null && head.arrived <= deathLine) {
                        queue.remove();
                        head = queue.peek();
                    }
                    clean(queue, head == null ? (Clock.currentTimeMillis() + MAX_LIFE) : head.arrived);
                    queue.notifyAll();
                }
            }
        }, time, TimeUnit.MILLISECONDS);

    }

    public static void asyncMessageReceive(String tokenId, AsyncMessageReceiver asyncMessageReceiver) {
        Queue<MessageWithArrived> queue = getQueue(tokenId);

        Set<AsyncMessageReceiver> asyncMessageReceiverSet = getAsyncMessageReceivers(queue);
        synchronized (asyncMessageReceiverSet) {
            if (!asyncMessageReceiverSet.contains(asyncMessageReceiver)) {
                List<Message> messages = getMessage(tokenId, -1);
                if (messages.size() > 0) {
                    asyncMessageReceiver.resume(messages);
                } else {
                    asyncMessageReceiverSet.add(asyncMessageReceiver);
                }
            }
        }
    }

    // TODO 性能优化
    private static synchronized Set<AsyncMessageReceiver> getAsyncMessageReceivers(Queue<MessageWithArrived> queue) {
        if (!ASYNC_MESSAGE_GETTER_MAP.containsKey(queue)) {
            ASYNC_MESSAGE_GETTER_MAP.put(queue, new HashSet<AsyncMessageReceiver>());
        }
        return ASYNC_MESSAGE_GETTER_MAP.get(queue);
    }

    public static List<Message> getMessage(String tokenId, long timeOut) {
        Queue<MessageWithArrived> queue = getQueue(tokenId);

        List<Message> messages = new ArrayList<Message>();
        synchronized (queue) {
            try {
                if (queue.isEmpty()) {
                    try {
                        if (timeOut >= 0)
//                            queue.wait(timeOut);
                            Clock.objWait(queue, timeOut);
                    } catch (Throwable e) {
                        log.warn("{}", e.getLocalizedMessage(), e);
                    }
                }
                while (!queue.isEmpty()) {
                    messages.add(queue.poll().message);
                }
            } finally {
                queue.notify();
            }
        }
        return messages;
    }

    @SuppressWarnings("unchecked")
    public static void deregister(AsyncMessageReceiver getter) {
        getAsyncMessageReceivers(getter.getKey()).remove(getter);
    }

    @Override
    public String getType() {
        return "JAXRS_COURIER";
    }

    @Override
    public <T> void pushTo(Message<T> message, Token token) {
        Queue<MessageWithArrived> queue = getQueue(token.getTokenId());
        synchronized (queue) {
            boolean handle = false;
            try {
                for (AsyncMessageReceiver receiver : getAsyncMessageReceivers(queue)) {
                    try {
                        handle = true;
                        receiver.resume(Arrays.asList((Message) message));
                    } catch (Throwable throwable) {
                        log.warn("{}", throwable.getLocalizedMessage(), throwable);
                    }
                }
            } finally {
                if (!handle) {
                    queue.add(new MessageWithArrived(message));
                    queue.notify();
                } else {
                    ASYNC_MESSAGE_GETTER_MAP.remove(queue);
                }
            }
        }
    }

    private class MessageWithArrived {
        private final Message message;
        private final long arrived = Clock.currentTimeMillis();

        public MessageWithArrived(Message message) {
            this.message = message;
        }

        public Message getMessage() {
            return message;
        }

        public long getArrived() {
            return arrived;
        }
    }
}
