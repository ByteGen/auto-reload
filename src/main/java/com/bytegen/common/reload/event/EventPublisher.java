package com.bytegen.common.reload.event;

import java.util.Properties;

/**
 * Publish {@link com.bytegen.common.reload.bean.PropertyChangedEvent} on resource updated
 */
public interface EventPublisher {
    void onPropertyChanged(Properties properties);
}
