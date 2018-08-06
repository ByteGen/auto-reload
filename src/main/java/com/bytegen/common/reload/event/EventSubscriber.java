package com.bytegen.common.reload.event;

import com.bytegen.common.reload.bean.PropertyChangedEvent;
import com.google.common.eventbus.Subscribe;

/**
 * Update bean properties once {@link PropertyChangedEvent} happened
 */
public interface EventSubscriber {

    @Subscribe
    void onPropertyChangedEvent(final PropertyChangedEvent event);
}
