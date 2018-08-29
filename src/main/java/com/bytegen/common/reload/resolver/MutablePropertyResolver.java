package com.bytegen.common.reload.resolver;

import java.util.Set;

/**
 * Interface to be apply any special property resolution techniques on the given object
 */
public interface MutablePropertyResolver {

    /**
     * Get all property names.
     *
     * @return set of the property names in this resolver.
     */
    Set<String> propertyNames();

    /**
     * Update/set the property value associated with the given key.
     *
     * @param key   the property name to set.
     * @param value the property value to set.
     * @return set property result
     */
    Object setProperty(String key, String value);

    /**
     * Return the unresolved property value associated with the given key,
     * or {@code null} if the key not exists.
     *
     * @param key the property name to search.
     */
    String getPropertyAsRawString(String key);

    /**
     * Return the resolved property value {@link #resolvePlaceholders} associated with the given key,
     * or {@code null} if the key not exists.
     *
     * @param key the property name to search.
     */
    String getProperty(final String key);

    /**
     * Resolve ${...} placeholders in the given text, replacing them with corresponding
     * property values as resolved by {@link #getProperty}. Unresolvable placeholders with
     * no default value will cause an IllegalArgumentException to be thrown.
     *
     * @return the resolved String (never {@code null}).
     * @throws IllegalArgumentException if given text is {@code null}
     *                                  or if any placeholders are unresolvable.
     */
    String resolvePlaceholders(final String text);

}
