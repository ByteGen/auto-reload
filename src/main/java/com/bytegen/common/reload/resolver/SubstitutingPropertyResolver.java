package com.bytegen.common.reload.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Implementation of a {@link PropertyResolver} resolving property keys via substitution.
 * Substitution occurs only for properly formatted markers e.g <code>${...}</code>
 * <pre>
 *     project.property 		= PropertyValue
 *     project.property.substitue = ${project.property}
 * </pre>
 */
public class SubstitutingPropertyResolver implements PropertyResolver {
    private static final Logger log = LoggerFactory.getLogger(SubstitutingPropertyResolver.class);

    @Override
    public String resolveValue(final Object propertyValue) {
        if (null != propertyValue) {
            final String stringValue = propertyValue.toString();

            // if property is a ${} then substitute it for the property it refers to
            return propertyRequiresSubstitution(stringValue)
                    ? stringValue.substring(2, stringValue.length() - 1)
                    : stringValue;
        }

        return null;
    }

    @Override
    public boolean requiresFurtherResolution(final Object propertyValue) {
        if (null == propertyValue) {
            log.info("Property value is null");
            return false;
        }
        final boolean propertyRequiresSubstitution = propertyRequiresSubstitution(propertyValue.toString());
        if (propertyRequiresSubstitution) {
            log.info("Further resolution required for property value [{}]", propertyValue);
        }
        return propertyRequiresSubstitution;
    }

    /**
     * Tests whether the given property is a ${...} property and therefore requires further resolution
     */
    private boolean propertyRequiresSubstitution(final String property) {
        if (null != property) {
            return property.contains("${") && property.contains("}") && property.indexOf("${") < property.indexOf("}");
        }
        return false;
    }

    @Override
    public Object resolveProperty(final Properties properties, final Object propertyName) {
        if (null != properties && null != propertyName) {
            Object resolvedPropertyValue = properties.get(this.resolveValue(propertyName));
            if (notStringPropertyToSubstitute(resolvedPropertyValue)) {
                return resolvedPropertyValue;
            }
            while (this.requiresFurtherResolution(resolvedPropertyValue)) {
                resolvedPropertyValue = buildResolvedString(properties, resolvedPropertyValue.toString());
            }
            return resolvedPropertyValue;
        }

        return null;
    }

    private String buildResolvedString(final Properties properties, final String resolvedPropertyValue) {

        final int startingIndex = resolvedPropertyValue.indexOf("${");
        final int endingIndex = resolvedPropertyValue.indexOf("}", startingIndex) + 1;

        final String toResolve = resolvedPropertyValue.substring(startingIndex, endingIndex);
        final String resolved = resolveProperty(properties, toResolve).toString();

        return resolvedPropertyValue.substring(0, startingIndex) +
                resolved +
                resolvedPropertyValue.substring(endingIndex);
    }

    /**
     * Tests whether the property value is String instance
     */
    private boolean notStringPropertyToSubstitute(final Object resolvedPropertyValue) {
        return !(resolvedPropertyValue instanceof String);
    }
}
