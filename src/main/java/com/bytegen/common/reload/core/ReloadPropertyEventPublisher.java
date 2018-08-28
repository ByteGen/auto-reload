package com.bytegen.common.reload.core;

import com.bytegen.common.reload.bean.PropertyChangedEvent;
import com.bytegen.common.reload.event.EventNotifier;
import com.bytegen.common.reload.event.EventPublisher;
import com.bytegen.common.reload.resolver.PropertyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User: xiang
 * Date: 2018/8/6
 * Desc:
 */
public class ReloadPropertyEventPublisher implements EventPublisher {
    private static Logger log = LoggerFactory.getLogger(ReloadPropertyEventPublisher.class);

    private Properties properties;

    private final PropertyResolver propertyResolver;
    private final EventNotifier eventNotifier;

    public ReloadPropertyEventPublisher(Properties properties, PropertyResolver propertyResolver,
                                        EventNotifier eventNotifier) {
        Assert.notNull(properties, "Properties must not be null");
        Assert.notNull(propertyResolver, "PropertyResolver must not be null");
        Assert.notNull(eventNotifier, "EventNotifier can not be null");

        this.properties = properties;
        this.propertyResolver = propertyResolver;
        this.eventNotifier = eventNotifier;
    }

    public Properties getProperties() {
        return properties;
    }

    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    public EventNotifier getEventNotifier() {
        return eventNotifier;
    }

    @Override
    public void onPropertyChanged(final Properties properties) {
        Map<String, String> cacheUpdated = new HashMap<>();
        // Copy for compare
        Properties newProperties = new Properties(this.getProperties());
        newProperties.putAll(properties);

        for (final String propertyName : this.getProperties().stringPropertyNames()) {
            final String oldValue = this.propertyResolver.resolveProperty(this.getProperties(), propertyName).toString();
            final String newValue = this.propertyResolver.resolveProperty(newProperties, propertyName).toString();

            if (propertyExistsAndNotNull(propertyName, newValue) && propertyChange(oldValue, newValue)) {
                // Temporary cache
                cacheUpdated.put(propertyName, newValue);

                // Post change event to notify any potential listeners
                this.eventNotifier.post(new PropertyChangedEvent(propertyName, oldValue, newValue));
                log.info("Publish property changes for [{}] with new value [{}]", propertyName, newValue);
            }
        }

        if (!cacheUpdated.isEmpty()) {
            // Update locally stored copy of properties
            cacheUpdated.forEach((propertyName, newValue) -> {
                this.getProperties().setProperty(propertyName, newValue);
            });
        }
    }

    private boolean propertyChange(final String oldValue, final String newValue) {
        return null == oldValue || !oldValue.equals(newValue);
    }

    private boolean propertyExistsAndNotNull(final String property, final String newValue) {
        return this.getProperties().containsKey(property) && null != newValue;
    }
}
