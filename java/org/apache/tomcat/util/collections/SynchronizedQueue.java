/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat.util.collections;

/**
 * This is intended as a (mostly) GC-free alternative to
 * {@link java.util.concurrent.ConcurrentLinkedQueue} when the requirement is to
 * create an unbounded queue with no requirement to shrink the queue. The aim is
 * to provide the bare minimum of required functionality as quickly as possible
 * with minimum garbage.
 */
public class SynchronizedQueue<T> {

    public static final int DEFAULT_SIZE = 128;

    private Object[] queue;
    private int size;
    private int insert = 0;
    private int remove = 0;

    public SynchronizedQueue() {
        this(DEFAULT_SIZE);
    }

    public SynchronizedQueue(int initialSize) {
        queue = new Object[initialSize];
        size = initialSize;
    }

    public synchronized boolean offer(T t) {
        // 存放数据
        queue[insert++] = t;

        // Wrap
        if (insert == size) {
            insert = 0;
        }

        /**
         * 队列扩容的条件比较苛刻
         *
         * 当 poll 速度慢于 offer 达到 size 的长度时
         */
        if (insert == remove) {
            expand();
        }
        return true;
    }

    public synchronized T poll() {
        if (insert == remove) {
            // empty
            return null;
        }

        @SuppressWarnings("unchecked")
        T result = (T) queue[remove];
        queue[remove] = null;
        remove++;

        // Wrap
        // 没有数据了
        if (remove == size) {
            remove = 0;
        }

        return result;
    }

    /**
     * 队列扩容方法
     */
    private void expand() {
        int newSize = size * 2;
        Object[] newQueue = new Object[newSize];


        // 参数: 源数据、源数据要复制的起始位置、目的数据、目的数据放置的起始位置、复制的长度
        // 将未处理的数据移动到 newQueue 的前端，将已经处理的数据移动到 newQueue 后段
        System.arraycopy(queue, insert, newQueue, 0, size - insert);
        System.arraycopy(queue, 0, newQueue, size - insert, insert);

        insert = size;
        remove = 0;
        queue = newQueue;
        size = newSize;
    }

    public synchronized int size() {
        // 队列为处理数据数量
        int result = insert - remove;
        if (result < 0) {
            // 处理落后一圈
            result += size;
        }
        return result;
    }

    public synchronized void clear() {
        queue = new Object[size];
        insert = 0;
        remove = 0;
    }
}
