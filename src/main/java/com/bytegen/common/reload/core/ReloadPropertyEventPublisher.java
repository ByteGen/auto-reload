package com.bytegen.common.reload.core;

import com.bytegen.common.reload.bean.PropertyChangedEvent;
import com.bytegen.common.reload.event.EventNotifier;
import com.bytegen.common.reload.event.EventPublisher;
import com.bytegen.common.reload.resolver.MutablePropertyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Properties;

/**
 * User: xiang
 * Date: 2018/8/6
 * Desc:
 */
public class ReloadPropertyEventPublisher implements EventPublisher {
    private static Logger log = LoggerFactory.getLogger(ReloadPropertyEventPublisher.class);

    private final MutablePropertyResolver propertyResolver;
    private final EventNotifier eventNotifier;

    private final Map<String, String> resolvedBeanProperty;

    public ReloadPropertyEventPublisher(MutablePropertyResolver propertyResolver,
                                        EventNotifier eventNotifier,
                                        Map<String, String> resolvedBeanProperty) {
        Assert.notNull(propertyResolver, "Property resolver must not be null");
        Assert.notNull(eventNotifier, "Event notifier can not be null");
        Assert.notNull(resolvedBeanProperty, "Resolved property map can not be null");

        this.propertyResolver = propertyResolver;
        this.eventNotifier = eventNotifier;
        this.resolvedBeanProperty = resolvedBeanProperty;
    }

    public MutablePropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    public EventNotifier getEventNotifier() {
        return eventNotifier;
    }

    @Override
    public void onPropertyChanged(final Properties properties) {
        // Update properties of resolver
        for (String key : properties.stringPropertyNames()) {
            String newValue = properties.getProperty(key);
            String oldValue = this.propertyResolver.getPropertyAsRawString(key);

            if (propertyExists(key) && propertyChangedAndNotNull(oldValue, newValue)) {
                this.propertyResolver.setProperty(key, newValue);
            }
        }

        for (final String key : this.resolvedBeanProperty.keySet()) {
            final String oldValue = this.resolvedBeanProperty.get(key);
            final String newValue = this.propertyResolver.resolvePlaceholders(key);

            if (propertyChangedAndNotNull(oldValue, newValue)) {
                // Update cache
                this.resolvedBeanProperty.put(key, newValue);

                // Post change event to notify any potential listeners
                this.eventNotifier.post(new PropertyChangedEvent(key, oldValue, newValue));
                log.info("Publish property changes for [{}] with new value [{}]", key, newValue);
            }
        }
    }

    private boolean propertyChangedAndNotNull(final String oldValue, final String newValue) {
        return null != newValue && (null == oldValue || !oldValue.equals(newValue));
    }

    private boolean propertyExists(final String property) {
        return this.propertyResolver.propertyNames().contains(property);
    }
}
