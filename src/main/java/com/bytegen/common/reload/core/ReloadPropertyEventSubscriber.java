package com.bytegen.common.reload.core;

import com.bytegen.common.reload.ReloadValue;
import com.bytegen.common.reload.bean.BeanPropertyHolder;
import com.bytegen.common.reload.bean.PropertyChangedEvent;
import com.bytegen.common.reload.conversion.DefaultPropertyConversion;
import com.bytegen.common.reload.conversion.PropertyConversion;
import com.bytegen.common.reload.event.EventNotifier;
import com.bytegen.common.reload.event.EventSubscriber;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * User: xiang
 * Date: 2018/8/6
 * Desc:
 */
public class ReloadPropertyEventSubscriber implements EventSubscriber {
    private static Logger log = LoggerFactory.getLogger(ReloadPropertyEventSubscriber.class);

    private final EventNotifier eventNotifier;
    private final Map<String, Set<BeanPropertyHolder>> beanPropertySubscriptions;

    public ReloadPropertyEventSubscriber(EventNotifier eventNotifier,
                                         Map<String, Set<BeanPropertyHolder>> beanPropertySubscriptions) {
        Assert.notNull(eventNotifier, "EventNotifier can not be null");

        this.eventNotifier = eventNotifier;
        this.beanPropertySubscriptions = (null == beanPropertySubscriptions) ?
                Collections.emptyMap() : beanPropertySubscriptions;

        log.info("Registering ReloadPropertyEventSubscriber for properties file changes");
        registerPropertyReloader();
    }

    /**
     * Utility method to register the class for receiving events about property files being changed,
     * setting up bean re-injection once triggered.
     */
    public final void registerPropertyReloader() {
        // Setup event listener
        this.eventNotifier.register(this);
    }

    /**
     * Utility method to unregister the class from receiving events about property files being changed.
     */
    public final void unregisterPropertyReloader() {
        log.info("Unregistering class from property file changes");
        this.eventNotifier.unregister(this);
    }

    /**
     * Method subscribing to the {@link PropertyChangedEvent} utilising the {@link Subscribe} annotation
     *
     * @param event the {@link PropertyChangedEvent} detailing what's changed
     */
    @Subscribe
    public void onPropertyChangedEvent(final PropertyChangedEvent event) {
        Set<BeanPropertyHolder> holders = this.beanPropertySubscriptions.get(event.getPropertyName());
        if (null != holders) {
            for (final BeanPropertyHolder bean : holders) {
                updateField(bean, event);
            }
        }
    }

    public void updateField(final BeanPropertyHolder holder, final PropertyChangedEvent event) {
        final Object beanToUpdate = holder.getBean();
        final Field fieldToUpdate = holder.getField();
        final String canonicalName = beanToUpdate.getClass().getCanonicalName();

        final Object convertedProperty = convertPropertyForField(fieldToUpdate, event.getNewValue());
        try {
            fieldToUpdate.set(beanToUpdate, convertedProperty);
            log.info("Reloading property [{}] on field [{}] for class [{}] with value [{}]",
                    event.getPropertyName(), fieldToUpdate.getName(), canonicalName, convertedProperty);
        } catch (final IllegalAccessException e) {
            log.error(String.format("Unable to reloading property [%s] on field [%s] for class [%s]",
                    event.getPropertyName(), fieldToUpdate.getName(), canonicalName), e);
        }
    }

    // ///////////////////////////////////
    // Utility methods for class access //
    // ///////////////////////////////////

    private Object convertPropertyForField(final Field field, final Object propertyValue) {
        try {
            Class<? extends PropertyConversion> conversionClass = field.getAnnotation(ReloadValue.class).conversion();

            PropertyConversion conversion;
            if (conversionClass == PropertyConversion.class || conversionClass == DefaultPropertyConversion.class) {
                conversion = DefaultPropertyConversion.getInstance();
            } else {
                conversion = BeanUtils.instantiateClass(conversionClass);
            }

            return conversion.convertPropertyForField(field, propertyValue);
        } catch (final Throwable e) {
            throw new BeanInitializationException(
                    String.format("Unable to convert property for field [%s].  Value [%s] cannot be converted to [%s]",
                            field.getName(), propertyValue, field.getType()), e);
        }
    }
}
