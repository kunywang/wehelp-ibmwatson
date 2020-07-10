package com.jy.mfe.bean;

import java.io.Serializable;

/**
 * @author kunpn
 */
public class PropertySet implements Serializable {
    private static final long serialVersionUID = 5240387944349633137L;

    private String name;
    private String type;
    private String property;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getProperty() {
        return property;
    }
    public void setProperty(String property) {
        this.property = property;
    }
}

