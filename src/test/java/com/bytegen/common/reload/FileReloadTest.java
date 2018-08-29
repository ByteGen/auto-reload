package com.bytegen.common.reload;

import com.bytegen.common.reload.bean.ReloadingPropertyBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Hello world!
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class FileReloadTest {

    @Resource
    ReloadingPropertyBean reloadingPropertyBean;

    private static final String DIR = "target/test-classes/";
    private static final String PROPERTIES = "application.properties";

    private Properties loadedProperties;

    @Before
    public void setUp() throws IOException {
        this.loadedProperties = PropertiesLoaderUtils.loadAllProperties(PROPERTIES);
        assertThat(this.reloadingPropertyBean.getIntProperty(), is(1));
        assertThat(this.reloadingPropertyBean.getBoolProperty(), is(true));
        assertThat(this.reloadingPropertyBean.getStringProperty(), is("Injected String Value"));
        assertThat(this.reloadingPropertyBean.getCompositeStringProperty(), is("Hello, World!"));
    }

    @After
    public void cleanUp() throws Exception {
        this.loadedProperties.setProperty("reloadable.intValue", "1");
        this.loadedProperties.setProperty("reloadable.boolValue", "true");
        this.loadedProperties.setProperty("reloadable.stringValue", "Injected String Value");
        this.loadedProperties.setProperty("reloadable.baseStringValue", "World");
        this.loadedProperties.setProperty("reloadable.compositeStringValue", "Hello, ${reloadable.baseStringValue}!");
        this.loadedProperties.setProperty("reloadable.listProperty", "Value1;Value2;Value3");

        final OutputStream newOutputStream = Files.newOutputStream(new File(DIR + PROPERTIES).toPath(), new OpenOption[]{});
        this.loadedProperties.store(newOutputStream, null);

        Thread.sleep(3000); // this is a hack
        assertThat(this.reloadingPropertyBean.getStringProperty(), is("Injected String Value"));
        assertThat(this.reloadingPropertyBean.getCompositeStringProperty(), is("Hello, World!"));
    }

    @Test
    public void shouldReloadAlteredStringProperty() throws Exception {
        assertThat(this.reloadingPropertyBean.getStringProperty(), is("Injected String Value"));

        this.loadedProperties.setProperty("reloadable.stringValue", "Altered Injected String Value");

        final File file = new File(DIR + PROPERTIES);
        final OutputStream newOutputStream = Files.newOutputStream(file.toPath());
        this.loadedProperties.store(newOutputStream, null);
        newOutputStream.flush();
        newOutputStream.close();

        Thread.sleep(3000); // this is a hack
        assertThat(this.reloadingPropertyBean.getStringProperty(), is("Altered Injected String Value"));
    }

    @Test
    public void shouldReloadAlteredCompositeStringProperty() throws Exception {
        assertThat(this.reloadingPropertyBean.getCompositeStringProperty(), is("Hello, World!"));

        this.loadedProperties.setProperty("reloadable.compositeStringValue", "Goodbye, ${reloadable.baseStringValue}!");
        assertThat(this.loadedProperties.getProperty("reloadable.compositeStringValue"), is("Goodbye, ${reloadable.baseStringValue}!"));

        final File file = new File(DIR + PROPERTIES);
        final OutputStream newOutputStream = Files.newOutputStream(file.toPath());
        this.loadedProperties.store(newOutputStream, null);
        newOutputStream.flush();
        newOutputStream.close();

        Thread.sleep(3000); // this is a hack
        assertThat(this.reloadingPropertyBean.getCompositeStringProperty(), is("Goodbye, World!"));
    }

    @Test
    public void shouldReloadAlteredBaseProperty() throws Exception {
        assertThat(this.reloadingPropertyBean.getCompositeStringProperty(), is("Hello, World!"));

        this.loadedProperties.setProperty("reloadable.baseStringValue", "Universe");
        assertThat(this.loadedProperties.getProperty("reloadable.compositeStringValue"), is("Hello, ${reloadable.baseStringValue}!"));

        final File file = new File(DIR + PROPERTIES);
        final OutputStream newOutputStream = Files.newOutputStream(file.toPath());
        this.loadedProperties.store(newOutputStream, null);
        newOutputStream.flush();
        newOutputStream.close();

        Thread.sleep(3000);
        assertThat(this.reloadingPropertyBean.getCompositeStringProperty(), is("Hello, Universe!"));
    }

    @Test
    public void shouldReloadAlteredListProperty() throws Exception {
        assertThat(this.reloadingPropertyBean.getListProperty(), is(Arrays.asList("Value1", "Value2", "Value3")));

        this.loadedProperties.setProperty("reloadable.listProperty", "Altered Value1;Altered Value2;Altered Value3");

        final File file = new File(DIR + PROPERTIES);
        final OutputStream newOutputStream = Files.newOutputStream(file.toPath());
        this.loadedProperties.store(newOutputStream, null);
        newOutputStream.flush();
        newOutputStream.close();

        Thread.sleep(3000);
        assertThat(this.reloadingPropertyBean.getListProperty(), is(Arrays.asList("Altered Value1", "Altered Value2", "Altered Value3")));
    }
}
