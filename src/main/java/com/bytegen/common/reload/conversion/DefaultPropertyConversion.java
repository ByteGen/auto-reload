package com.bytegen.common.reload.conversion;

import com.google.common.base.Function;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.convert.support.DefaultConversionService;

import java.lang.reflect.Field;

/**
 * Default implementation of {@link PropertyConversion}, attempting to convert an object otherwise utilising {@link DefaultConversionService#sharedInstance} if no matching converter is found.
 */
public class DefaultPropertyConversion implements PropertyConversion {

    private DefaultPropertyConversion() {
    }

    private static class DefaultPropertyConversionHolder {
        private static final DefaultPropertyConversion instance = new DefaultPropertyConversion();
    }

    public static DefaultPropertyConversion getInstance() {
        return DefaultPropertyConversionHolder.instance;
    }

    @Override
    public Object convertPropertyForField(final Field field, final Object property) {
        try {
            return new DefaultConverter<>(field.getType()).apply(property);
        } catch (final Throwable e) {
            throw new BeanInitializationException(
                    String.format("Unable to convert property for field [%s].  Value [%s] cannot be converted to [%s]",
                            field.getName(), property, field.getType()), e);
        }
    }

    private static class DefaultConverter<T> implements Function<Object, T> {
        private final Class<T> type;

        public DefaultConverter(final Class<T> type) {
            this.type = type;
        }

        @Override
        public T apply(final Object input) {
            return DefaultConversionService.getSharedInstance().convert(input, this.type);
        }
    }
}

