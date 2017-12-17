/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import java.util.EnumMap;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.ini4j.Profile.Section;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public final class IniConfigurableItem {
    
    private final EnumMap<Field, ReadOnlyStringWrapper> fieldMap;
    private final Section iniSection;
    
    IniConfigurableItem(Section iniSection) {
        this.iniSection = iniSection;
        this.fieldMap = new EnumMap<>(Field.class);
        
        if(iniSection != null) {
            for(Field f : Field.values()) {
                String value = iniSection.get(f.iniKey());
                if(value != null) {
                    fieldMap.put(f, new ReadOnlyStringWrapper(value));
                }
            }
        }
    }
    
    public final String sectionName() {
        return iniSection == null ? null : iniSection.getName();
    }
    
    public final void set(Field f, String value) {
        fieldMap.get(f).setValue(value);
    }
    
    public final String get(Field f) {
        ReadOnlyStringWrapper w = fieldMap.get(f);
        return (w == null) ? null : w.getValue();
    }
    
    public final String get(Field f, String ifNull) {
        ReadOnlyStringWrapper w = fieldMap.get(f);
        return (w == null || w.getValue() == null) ? ifNull : w.getValue();
    }
    
    public final Integer getInt(Field f, Integer ifNull) {
        ReadOnlyStringWrapper w = fieldMap.get(f);
        return (w == null || w.getValue() == null) ? ifNull : Integer.parseInt(w.getValue());
    }
    
    public final Config.Type getType() {
        return Config.Type.valueOf(fieldMap.get(Field.TYPE).getValue().toUpperCase());
    }
    
    public final ReadOnlyStringProperty valueProperty(Field f) {
        ReadOnlyStringWrapper wrapper = fieldMap.get(f);
        if(wrapper == null) {
            wrapper = new ReadOnlyStringWrapper("");
            fieldMap.put(f, wrapper);
        }
        return wrapper.getReadOnlyProperty();
    }
}
