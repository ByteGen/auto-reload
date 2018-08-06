package com.bytegen.common.reload;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * Annotation providing a convenient and declarative mechanism for adding a
 * {@link org.springframework.core.io.Resource Resource} to
 * {@link com.bytegen.common.reload.core.ReloadResourceFactoryProcessor ReloadResourceFactory}.
 * To be used in conjunction with @{@link Configuration} classes.
 * <p>
 * <h3>Example usage</h3>
 * <p>
 * <p>Given a file {@code app.properties} containing the key/value pair
 * {@code reloadable.stringValue=testValue}, the following {@code @Configuration} class
 * uses {@code @ReloadResource} to contribute {@code app.properties} to the
 * {@code ReloadPropertySourceSupport}.
 * <p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;ReloadResource("classpath:app.properties")
 * public class ReloadingPropertyBean {
 * <p>
 * &#064;ReloadValue("reloadable.stringValue")
 * private String stringProperty;
 * <p>
 * public String getStringProperty() {
 * return this.stringProperty;
 * }
 * }
 * </pre>
 * <p>
 * <h3>Resolving properties in {@code <bean>} and {@code @ReloadValue} annotations</h3>
 * <p>
 * In order to resolve properties in {@code <bean>} definitions or {@code @ReloadValue}
 * annotations using properties from a {@code Resource}, one must register a
 * {@code ReloadablePropertySourceSupport}. This happens automatically when using
 * component-scanning, but must be explicitly registered using a {@code static}
 * {@code @Bean} method when using {@code @Configuration} classes. See the
 * "Working with externalized values" section of @{@link Configuration}'s javadoc and
 * "a note on BeanFactoryPostProcessor-returning @Bean methods" of @{@link Bean}'s javadoc
 * for details and examples.
 * <p>
 * <h3>A note on property overriding with @PropertySource</h3>
 * <p>
 * In cases where a given property key exists in more than one {@code .properties}
 * file, the last {@code @ReloadResource} annotation processed will 'win' and override.
 * <p>
 * For example, given two properties files {@code a.properties} and
 * {@code b.properties}, consider the following two configuration classes
 * that reference them with {@code @ReloadResource} annotations:
 * <p>
 * <pre class="code">
 * &#064;Configuration
 * &#064;ReloadResource("classpath:/com/myco/a.properties")
 * public class ConfigA { }
 * <p>
 * &#064;Configuration
 * &#064;ReloadResource("classpath:/com/myco/b.properties")
 * public class ConfigB { }
 * </pre>
 * <p>
 * The override ordering depends on the order in which these classes are registered
 * with the application context. Once both registered, the override ordering depends
 * on the modifier time.
 * <p>
 * <pre class="code">
 * AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
 * ctx.register(ConfigA.class);
 * ctx.register(ConfigB.class);
 * ctx.refresh();
 * </pre>
 * <p>
 * In the scenario above, the properties in {@code b.properties} will override any
 * duplicates that exist in {@code a.properties}, because {@code ConfigB} was registered
 * last. And {@code a.properties} will 'win' the override if updated after registration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReloadResource {

    /**
     * Indicate the resource location(s) of the properties file to be loaded.
     * For example, {@code "classpath:/com/myco/app.properties"} or
     * {@code "file:/path/to/file"}.
     * <p>Resource location wildcards (e.g. *&#42;/*.properties) are not permitted;
     * each location must evaluate to exactly one {@code .properties} resource.
     * See {@linkplain ReloadResource above} for examples.
     */
    String[] value();

    /**
     * Indicate if failure to find the a {@link #value() property resource} should be
     * ignored.
     * <p>{@code true} is appropriate if the properties file is completely optional.
     * Default is {@code false}.
     */
    boolean ignoreResourceNotFound() default false;

    /**
     * A specific character encoding for the given resources, e.g. "UTF-8".
     */
    String encoding() default "";

}
