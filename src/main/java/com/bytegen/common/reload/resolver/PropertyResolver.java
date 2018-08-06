package com.bytegen.common.reload.resolver;

import java.util.Properties;

/**
 * Interface to be apply any special property resolution techniques on the given object
 */
public interface PropertyResolver {

    /**
     * @param propertyValue The value to resolve, if required
     * @return The result of the value resolution, or the property itself if no further resolution was required
     */
    String resolveValue(final Object propertyValue);

    /**
     * Can be used to check whether a property requires further resolution
     *
     * @param propertyValue The value to resolve, if required
     * @return true if the chosen {@link PropertyResolver} performs custom resolution
     */
    boolean requiresFurtherResolution(final Object propertyValue);

    /**
     * Get the property value from {@link Properties} by name, and resolve the value if required
     *
     * @param properties   The properties to resolve from
     * @param propertyName The name to resolve
     * @return The result of the property value after resolution
     */
    Object resolveProperty(final Properties properties, final Object propertyName);
}
