package com.bytegen.common.reload.event;

import org.springframework.core.io.support.EncodedResource;

/**
 * Publish {@link com.bytegen.common.reload.bean.PropertyChangedEvent} on resource updated
 */
public interface EventPublisher {
    void onResourceChanged(EncodedResource resource);
}
