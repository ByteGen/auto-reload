package com.bytegen.common.reload.bean;

import com.bytegen.common.reload.ReloadResource;
import com.bytegen.common.reload.ReloadValue;
import com.bytegen.common.reload.conversion.ListPropertyConversion;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ReloadResource(value = {"application.properties", "classpath:app.properties", "${reloadable.path:}"}, ignoreResourceNotFound = true)
public class ReloadingPropertyBean {

    @ReloadValue(value = "${reloadable.intValue}")
    private int intProperty;
    @ReloadValue(value = "${reloadable.boolValue}")
    private boolean boolProperty;

    @ReloadValue(value = "${reloadable.stringValue}")
    private String stringProperty;

    @ReloadValue(value = "${reloadable.compositeStringValue}")
    private String compositeStringProperty;

    @ReloadValue(value = "${reloadable.listProperty}", conversion = ListPropertyConversion.class)
    private List<String> listProperty;

    public int getIntProperty() {
        return intProperty;
    }

    public boolean getBoolProperty() {
        return boolProperty;
    }

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
                + "\"intProperty\":\"" + intProperty + "\""
                + ", \"boolProperty\":\"" + boolProperty + "\""
                + ", \"stringProperty\":\"" + stringProperty + "\""
                + ", \"compositeStringProperty\":\"" + compositeStringProperty + "\""
                + ", \"listProperty\":" + listProperty
                + "}}";
    }
}