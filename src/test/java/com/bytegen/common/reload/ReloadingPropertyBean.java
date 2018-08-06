package com.bytegen.common.reload;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ReloadResource(value = {"application.properties", "classpath:app.properties"}, ignoreResourceNotFound = true)
public class ReloadingPropertyBean {

    @ReloadValue(value = "reloadable.stringValue")
    private String stringProperty;

    @ReloadValue(value = "reloadable.compositeStringValue")
    private String compositeStringProperty;

    @ReloadValue(value = "reloadable.listProperty", conversion = ListPropertyConversion.class)
    private List<String> listProperty;

    public String getStringProperty() {
        return this.stringProperty;
    }

    public String getCompositeStringProperty() {
        return this.compositeStringProperty;
    }

    public List<String> getListProperty() {
        return listProperty;
    }

    @Override
    public String toString() {
        return "{\"ReloadingPropertyBean\":{"
                + "\"stringProperty\":\"" + stringProperty + "\""
                + ", \"compositeStringProperty\":\"" + compositeStringProperty + "\""
                + ", \"listProperty\":\"" + listProperty + "\""
                + "}}";
    }
}