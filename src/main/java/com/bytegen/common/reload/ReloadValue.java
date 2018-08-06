package com.bytegen.common.reload;

import com.bytegen.common.reload.conversion.DefaultPropertyConversion;
import com.bytegen.common.reload.conversion.PropertyConversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Marks a field to be set from the given property value,
 * the specified property will reset the field if changed during runtime.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ReloadValue {

    /**
     * The property key in config file
     */
    String value();

    /**
     * The class used to convert the given property {@link Object} before being set on the given {@link Field}.
     * The class will be instantiated with {@link org.springframework.beans.BeanUtils}, and default constructor is required
     */
    Class<? extends PropertyConversion> conversion() default DefaultPropertyConversion.class;
}
