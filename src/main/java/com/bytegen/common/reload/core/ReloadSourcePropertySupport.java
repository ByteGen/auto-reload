package com.bytegen.common.reload.core;

import com.bytegen.common.reload.ReloadResource;
import com.bytegen.common.reload.ReloadValue;
import com.bytegen.common.reload.bean.BeanPropertyHolder;
import com.bytegen.common.reload.conversion.DefaultPropertyConversion;
import com.bytegen.common.reload.conversion.PropertyConversion;
import com.bytegen.common.reload.event.EventNotifier;
import com.bytegen.common.reload.event.EventPublisher;
import com.bytegen.common.reload.event.GuavaEventNotifier;
import com.bytegen.common.reload.resolver.PropertiesPropertyResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.UnknownHostException;
import java.util.*;

/**
 * <p>
 * Processes beans on start up injecting field values marked with {@link ReloadValue} setting the associated annotated property value with properties
 * configured in a {@link ReloadResource}.
 * </p>
 * <p>
 * The processor also has the ability to reload/re-inject properties from the configured {@link ReloadResource} which are changed.
 * Once a property is reloaded the associated bean holding that value will have its property updated, no further bean operations are performed on the reloaded
 * bean.
 * </p>
 * <p>
 * The processor will also substitute any properties with values starting with "${" and ending with "}", none recursive.
 * </p>
 */
@Component
public class ReloadSourcePropertySupport extends InstantiationAwareBeanPostProcessorAdapter implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(ReloadSourcePropertySupport.class);

    @Resource
    private Environment environment;
    @Resource
    private ReloadResourceFactoryProcessor reloadResourceFactoryProcessor;

    private final EventNotifier eventNotifier = GuavaEventNotifier.getInstance();
    private final PropertiesPropertyResolver propertyResolver = new PropertiesPropertyResolver();

    private final Map<String, Set<BeanPropertyHolder>> beanPropertySubscriptions = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Loading Reloadable Properties resources...");
        Pair<Set<EncodedResource>, Properties> resourceAndProperties = loadResources(reloadResourceFactoryProcessor.getReloadResourceCandidates());
        Set<EncodedResource> locations = resourceAndProperties.getLeft();
        Properties properties = resourceAndProperties.getRight();
        propertyResolver.addProperties(properties);

        log.info("Registering ReloadPropertyPubSub for properties file changes");
        if (null == locations || locations.isEmpty()) {
            log.info("Locations are empty, break for reloadable source property support...!");
            return;
        }

        try {
            log.info("Start watching for properties file changes");
            EventPublisher publisher = new ReloadPropertyEventPublisher(propertyResolver, eventNotifier, beanPropertySubscriptions);
            new ReloadPropertyEventSubscriber(eventNotifier, beanPropertySubscriptions);

            // Here we actually create and set a FileWatcher to monitor the given locations
            new PropertiesFileWatcher(locations, publisher).startWatching();
        } catch (final IOException e) {
            log.error("Unable to start properties file watcher", e);
        }
    }

    private Pair<Set<EncodedResource>, Properties> loadResources(List<AnnotatedBeanDefinition> definitions) {
        // None @ReloadResource annotated bean definition found
        if (CollectionUtils.isEmpty(definitions)) {
            return Pair.of(null, null);
        }

        Properties properties = new Properties();
        Set<EncodedResource> locations = new HashSet<>();

        definitions.forEach(bd -> {
            Map<String, Object> attributes = bd.getMetadata()
                    .getAnnotationAttributes(ReloadResource.class.getCanonicalName());
            processReloadResourceAttributes(new AnnotationAttributes(attributes), properties, locations);
        });

        return Pair.of(locations, properties);
    }

    private String resolveEnvironmentProperty(String text) {
        if (null != text) {
            return environment.resolveRequiredPlaceholders(text);
        }
        return null;
    }

    private void processReloadResourceAttributes(AnnotationAttributes propertySource, Properties properties, Set<EncodedResource> encodedResources) throws BeanDefinitionStoreException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String encoding = resolveEnvironmentProperty(propertySource.getString("encoding"));
        if (StringUtils.isBlank(encoding)) {
            encoding = null;
        }
        String[] locations = propertySource.getStringArray("value");

        Assert.isTrue(locations.length > 0, "At least one @ReloadResource(value) location is required");
        boolean ignoreResourceNotFound = propertySource.getBoolean("ignoreResourceNotFound");

        for (String location : locations) {
            try {
                String resolved = resolveEnvironmentProperty(location);
                if (StringUtils.isBlank(resolved)) {
                    if (log.isInfoEnabled()) {
                        log.warn("Properties location [" + location + "] is blank, skipped.");
                    }
                    continue;
                }

                EncodedResource encodedResource = new EncodedResource(resourceLoader.getResource(resolved), encoding);
                Properties props = PropertiesLoaderUtils.loadProperties(encodedResource);

                encodedResources.add(encodedResource);
                properties.putAll(props);
            } catch (IOException ex) {
                // Resource not found when trying to open it
                if (ignoreResourceNotFound &&
                        (ex instanceof FileNotFoundException || ex instanceof UnknownHostException)) {
                    if (log.isInfoEnabled()) {
                        log.warn("Properties location [" + location + "] not resolvable: " + ex.getMessage());
                    }
                } else {
                    throw new BeanDefinitionStoreException(
                            "Failed to resolve configuration resource [" + location + "]", ex);
                }
            }
        }
    }


    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {
        if (log.isDebugEnabled()) {
            log.debug("Setting Reloadable Properties on [{}]", beanName);
        }
        setPropertiesOnBean(bean);
        return true;
    }

    private void setPropertiesOnBean(final Object bean) {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {

            @Override
            public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {

                final ReloadValue annotation = field.getAnnotation(ReloadValue.class);
                if (null != annotation) {

                    ReflectionUtils.makeAccessible(field);
                    validateFieldNotFinal(bean, field);

                    final Object propertyValue = propertyResolver.resolvePlaceholders(annotation.value());
                    validatePropertyAvailableOrDefaultSet(bean, field, annotation, propertyValue);

                    if (null != propertyValue) {
                        log.info("Attempting to convert and set property [{}] on field [{}] for class [{}] to type [{}]",
                                propertyValue, field.getName(), bean.getClass().getCanonicalName(), field.getType());

                        final Object convertedProperty = convertPropertyForField(field, propertyValue, annotation.conversion());

                        log.info("Setting field [{}] of class [{}] with value [{}]",
                                field.getName(), bean.getClass().getCanonicalName(), convertedProperty);

                        field.set(bean, convertedProperty);

                        subscribeBeanToPropertyChangedEvent(annotation.value(), new BeanPropertyHolder(bean, field));
                    } else {
                        log.info("Leaving field [{}] of class [{}] with default value",
                                field.getName(), bean.getClass().getCanonicalName());
                    }
                }
            }
        });
    }

    private void validatePropertyAvailableOrDefaultSet(final Object bean, final Field field, final ReloadValue annotation, final Object propertyValue)
            throws IllegalArgumentException, IllegalAccessException {
        if (null == propertyValue && fieldDoesNotHaveDefault(field, bean)) {
            throw new BeanInitializationException(String.format("No property found for field annotated with @ReloadValue, "
                    + "and no default specified. Property [%s] of class [%s] requires a property named [%s]", field.getName(), bean.getClass()
                    .getCanonicalName(), annotation.value()));
        }
    }

    private void validateFieldNotFinal(final Object bean, final Field field) {
        if (Modifier.isFinal(field.getModifiers())) {
            throw new BeanInitializationException(String.format("Unable to set field [%s] of class [%s] as is declared final", field.getName(), bean.getClass()
                    .getCanonicalName()));
        }
    }

    private boolean fieldDoesNotHaveDefault(final Field field, final Object value) throws IllegalArgumentException, IllegalAccessException {
        try {
            return (null == field.get(value));
        } catch (final NullPointerException e) {
            return true;
        }
    }

    private void subscribeBeanToPropertyChangedEvent(final String propertyName, final BeanPropertyHolder fieldProperty) {
        this.beanPropertySubscriptions.computeIfAbsent(propertyName, k -> new HashSet<>());
        this.beanPropertySubscriptions.get(propertyName).add(fieldProperty);
    }

    // ///////////////////////////////////
    // Utility methods for class access //
    // ///////////////////////////////////

    private Object convertPropertyForField(final Field field, final Object propertyValue, final Class<? extends PropertyConversion> conversionClass) {
        try {
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
