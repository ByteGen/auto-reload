package com.bytegen.common.reload.conversion;

import com.google.common.base.Function;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

/**
 * Default implementation of {@link PropertyConversion}, attempting to convert an object otherwise utilising {@link SimpleTypeConverter} if no matching converter is found.
 */
public class DefaultPropertyConversion implements PropertyConversion {

    private static TypeConverter DEFAULT;

    public DefaultPropertyConversion(TypeConverter springConverter) {
        Assert.notNull(springConverter, "SpringConverter can not be null");

        DEFAULT = springConverter;
    }

    @Override
    public Object convertPropertyForField(final Field field, final Object property) {
        try {
            return new DefaultConverter(field.getType()).apply(property);
        } catch (final Throwable e) {
            throw new BeanInitializationException(
                    String.format("Unable to convert property for field [%s].  Value [%s] cannot be converted to [%s]",
                            field.getName(), property, field.getType()), e);
        }
    }

    private static class DefaultConverter implements Function<Object, Object> {
        private final Class<?> type;

        public DefaultConverter(final Class<?> type) {
            this.type = type;
        }

        @Override
        public Object apply(final Object input) {
            return DEFAULT.convertIfNecessary(input, this.type);
        }
    }
}

