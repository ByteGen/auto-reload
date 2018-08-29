package com.bytegen.common.reload.resolver;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * User: xiang
 * Date: 2018/8/29
 * Desc:
 */
public class PropertiesPropertyResolverTest {
    @Test
    public void getProperty() throws Exception {
    }

    @Test
    public void resolvePlaceholders() throws Exception {
        PropertiesPropertyResolver resolver = new PropertiesPropertyResolver();
        resolver.setProperty("key", "value");

        String result = resolver.resolvePlaceholders("${key}");
        Assert.assertThat(result, CoreMatchers.is("value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolvePlaceholdersNotFound() throws Exception {
        PropertiesPropertyResolver resolver = new PropertiesPropertyResolver();

        String result = resolver.resolvePlaceholders("${key}");
    }

    @Test
    public void resolvePlaceholdersDefault() throws Exception {
        PropertiesPropertyResolver resolver = new PropertiesPropertyResolver();

        String result = resolver.resolvePlaceholders("${key:value}");
        Assert.assertThat(result, CoreMatchers.is("value"));
    }

    @Test
    public void resolvePlaceholdersKeyCompose() throws Exception {
        PropertiesPropertyResolver resolver = new PropertiesPropertyResolver();
        resolver.setProperty("key", "base");
        resolver.setProperty("inner", "compose");
        resolver.setProperty("key_compose", "value");

        String result = resolver.resolvePlaceholders("${key_${inner}}");
        Assert.assertThat(result, CoreMatchers.is("value"));
    }

    @Test
    public void resolvePlaceholdersValueCompose() throws Exception {
        PropertiesPropertyResolver resolver = new PropertiesPropertyResolver();
        resolver.setProperty("key", "value");
        resolver.setProperty("inner", "compose");
        resolver.setProperty("key_compose", "${key}");

        String result = resolver.resolvePlaceholders("${key_${inner}}");
        Assert.assertThat(result, CoreMatchers.is("value"));
    }

}