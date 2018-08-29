package com.bytegen.common.reload.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Properties;
import java.util.Set;

/**
 * Implementation of a {@link MutablePropertyResolver} resolving properties with {@link PropertyPlaceholderHelper}.
 * Resolving occurs only for properly formatted markers e.g <code>${...}</code>
 * <pre>
 *     project.property 		= PropertyValue
 *     project.property.substitue = ${project.property}
 * </pre>
 */
public class PropertiesPropertyResolver implements MutablePropertyResolver {
    private static final Logger log = LoggerFactory.getLogger(PropertiesPropertyResolver.class);

    /**
     * Prefix for property placeholders:
     */
    private static final String PLACEHOLDER_PREFIX = "${";
    /**
     * Suffix for property placeholders: "}"
     */
    private static final String PLACEHOLDER_SUFFIX = "}";
    /**
     * Value separator for property placeholders: ":"
     */
    private static final String VALUE_SEPARATOR = ":";

    private static final boolean ignoreUnresolvablePlaceholders = false;

    private Properties properties;
    private PropertyPlaceholderHelper strictHelper;

    public PropertiesPropertyResolver() {
        this.properties = new Properties();
        this.strictHelper = new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX,
                VALUE_SEPARATOR, ignoreUnresolvablePlaceholders);
    }

    public void addProperties(Properties props) {
        if (null != props) {
            this.properties.putAll(props);
        }
    }

    @Override
    public Set<String> propertyNames() {
        return this.properties.stringPropertyNames();
    }

    @Override
    public Object setProperty(String key, String value) {
        Assert.notNull(key, "Property key must not be null");
        return this.properties.setProperty(key, value);
    }

    @Override
    public String getPropertyAsRawString(String key) {
        return getProperty(key, false);
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, true);
    }

    @Override
    public String resolvePlaceholders(String text) {
        Assert.notNull(text, "'text' must not be null");

        return strictHelper.replacePlaceholders(text, this::getPropertyAsRawString);
    }

    protected String getProperty(String key, boolean resolveNestedPlaceholders) {
        String value = properties.getProperty(key);
        if (value != null) {
            if (resolveNestedPlaceholders) {
                value = resolvePlaceholders(value);
            }

            log.debug("Found key '" + key + "' in properties with value of " + value);
            return value;
        }

        log.info("Could not find key '" + key + "' in properties");
        return null;
    }

}
