package com.bytegen.common.reload.event;

import com.bytegen.common.reload.bean.PropertyChangedEvent;

/**
 * User: xiang
 * Date: 2018/8/6
 * Desc:
 */
public interface EventNotifier {
    void post(PropertyChangedEvent propertyChangedEvent);

    void unregister(EventSubscriber eventSubscriber);

    void register(EventSubscriber eventSubscriber);
}
