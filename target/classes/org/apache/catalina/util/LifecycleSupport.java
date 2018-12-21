/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

/**
 * Support class to assist in firing LifecycleEvent notifications to
 * registered LifecycleListeners.
 *
 * @author Craig R. McClanahan
 */
public final class LifecycleSupport {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new LifecycleSupport object associated with the specified
     * Lifecycle component.
     *
     * @param lifecycle The Lifecycle component that will be the source
     *  of events that we fire
     */
    public LifecycleSupport(Lifecycle lifecycle) {
        super();
        this.lifecycle = lifecycle;
    }


    // ----------------------------------------------------- Instance Variables

    /**
     * The source component for lifecycle events that we will fire.
     */
    private final Lifecycle lifecycle;


    /**
     * The list of registered LifecycleListeners for event notifications.
     * 线程安全集合
     * 1. 适合于具有一下特征的应用程序：List 大小通常保持很小，只读操作远多于可变操作，需要在遍历期间防止线程间的冲突
     * 2. 支持高效率并发且是线程安全的
     * 3. 因为通常需要复制整个基础数组，所以可变操作（add(), set（）, 和 remove () 等等）开销很大
     * 4. 迭代器支持 hasHext(), next() 等不可变操作，但不支持可变 remove() 等操作
     * 5. 使用迭代进行遍历的速度很快，并且不会与其他线程发生冲突，在构造迭代器时，迭代器依赖于不变的数据快照
     */
    private final List<LifecycleListener> listeners = new CopyOnWriteArrayList<>();


    // --------------------------------------------------------- Public Methods

    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {
        listeners.add(listener);
    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {
        return listeners.toArray(new LifecycleListener[0]);
    }


    /**
     * Notify all lifecycle event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireLifecycleEvent(String type, Object data) {
        LifecycleEvent event = new LifecycleEvent(lifecycle, type, data);
        for (LifecycleListener listener : listeners) {
            listener.lifecycleEvent(event);
        }
    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {
        listeners.remove(listener);
    }
}
