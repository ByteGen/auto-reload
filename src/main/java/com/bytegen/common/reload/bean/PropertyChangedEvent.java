package com.bytegen.common.reload.bean;

import java.util.Objects;

/**
 * User: xiang
 * Date: 2018/8/6
 * Desc:
 */
public class PropertyChangedEvent {

    private String propertyName;
    private Object oldValue;
    private Object newValue;

    public PropertyChangedEvent(final String propertyName, final Object oldValue, final Object newValue) {
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public Object getOldValue() {
        return this.oldValue;
    }

    public Object getNewValue() {
        return this.newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyChangedEvent that = (PropertyChangedEvent) o;
        return Objects.equals(propertyName, that.propertyName) &&
                Objects.equals(oldValue, that.oldValue) &&
                Objects.equals(newValue, that.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyName, oldValue, newValue);
    }

    @Override
    public String toString() {
        return "{\"PropertyChangedEvent\":"
                + ", \"propertyName\":\"" + propertyName + "\""
                + ", \"oldValue\":" + oldValue
                + ", \"newValue\":" + newValue
                + "}";
    }
}
