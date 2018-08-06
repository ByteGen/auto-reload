package com.bytegen.common.reload.core;

import com.bytegen.common.reload.bean.PropertyChangedEvent;
import com.bytegen.common.reload.event.EventNotifier;
import com.bytegen.common.reload.event.EventPublisher;
import com.bytegen.common.reload.resolver.PropertyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * User: xiang
 * Date: 2018/8/6
 * Desc:
 */
public class ReloadPropertyEventPublisher implements EventPublisher {
    private static Logger log = LoggerFactory.getLogger(ReloadPropertyEventPublisher.class);

    private final Set<EncodedResource> locations;
    private Properties properties;

    private final PropertyResolver propertyResolver;
    private final EventNotifier eventNotifier;

    public ReloadPropertyEventPublisher(Set<EncodedResource> locations, Properties properties,
                                        PropertyResolver propertyResolver, EventNotifier eventNotifier) throws IOException {
        Assert.notNull(locations, "PropertyResource must not be null");
        Assert.notNull(properties, "Properties must not be null");
        Assert.notNull(propertyResolver, "PropertyResolver must not be null");
        Assert.notNull(eventNotifier, "EventNotifier can not be null");

        this.locations = locations;
        this.properties = properties;
        this.propertyResolver = propertyResolver;
        this.eventNotifier = eventNotifier;

        log.info("Start watching for properties file changes");
        // Here we actually create and set a FileWatcher to monitor the given locations
        new PropertiesFileWatcher(this.getLocations(), this).startWatching();
    }

    public Properties getProperties() {
        return properties;
    }

    public Set<EncodedResource> getLocations() {
        return locations;
    }

    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    public EventNotifier getEventNotifier() {
        return eventNotifier;
    }

    @Override
    public void onResourceChanged(final EncodedResource resource) {
        try {
            final Properties reloadedProperties = PropertiesLoaderUtils.loadProperties(resource);

            Properties updatedProperties = new Properties(this.properties);
            updatedProperties.putAll(reloadedProperties);

            for (final String propertyName : this.getProperties().stringPropertyNames()) {
                final String oldValue = this.propertyResolver.resolveProperty(this.properties, propertyName).toString();
                final String newValue = this.propertyResolver.resolveProperty(updatedProperties, propertyName).toString();

                if (propertyExistsAndNotNull(propertyName, newValue) && propertyChange(oldValue, newValue)) {
                    // Update locally stored copy of properties
                    this.properties.setProperty(propertyName, newValue);

                    // Post change event to notify any potential listeners
                    this.eventNotifier.post(new PropertyChangedEvent(propertyName, oldValue, newValue));
                    log.info("Publish property changes for [{}] with new value [{}]", propertyName, newValue);
                }
            }
        } catch (final IOException e) {
            log.error("Failed to reload properties file once change", e);
        }
    }

    private boolean propertyChange(final String oldValue, final String newValue) {
        return null == oldValue || !oldValue.equals(newValue);
    }

    private boolean propertyExistsAndNotNull(final String property, final String newValue) {
        return this.properties.containsKey(property) && null != newValue;
    }
}
