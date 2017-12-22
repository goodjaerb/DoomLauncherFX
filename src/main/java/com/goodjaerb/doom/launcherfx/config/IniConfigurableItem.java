/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import java.util.EnumMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import org.ini4j.Profile.Section;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public final class IniConfigurableItem {
    public static final IniConfigurableItem EMPTY_ITEM = new IniConfigurableItem(null);
    
    private final EnumMap<Field, ReadOnlyStringWrapper> fieldMap;
    private final Section iniSection;
    private final SimpleBooleanProperty selectedProperty;
    private final SimpleBooleanProperty enabledProperty;
    
    IniConfigurableItem(Section iniSection) {
        this.iniSection = iniSection;
        this.fieldMap = new EnumMap<>(Field.class);
        this.selectedProperty = new SimpleBooleanProperty(false);
        this.enabledProperty = new SimpleBooleanProperty(true);
        
        if(iniSection != null) {
            for(Field f : Field.values()) {
                String value = iniSection.get(f.iniKey());
                if(value != null) {
                    fieldMap.put(f, new ReadOnlyStringWrapper(value));
                }
            }
        }
    }
    
    /**
     * returns this items section name as defined in the ini file, or the empty string in the case of EMPTY_ITEM.
     * @return 
     */
    public final String sectionName() {
        return iniSection == null ? "" : iniSection.getName();
    }
    
    public final void set(Field f, String value) {
        fieldMap.get(f).setValue(value);
    }
    
    /**
     * gets the value for the given field. Null if it doesn't exist.
     * 
     * @param f
     * @return 
     */
    public final String get(Field f) {
        ReadOnlyStringWrapper w = fieldMap.get(f);
        return (w == null) ? null : w.getValue();
    }
    
    /**
     * gets the value for the given field. returns 'ifNull' if it doesn't exist.
     * 
     * @param f
     * @param ifNull
     * @return 
     */
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
    
    public final void setEnabled(boolean b) {
        enabledProperty.set(b);
        if(!b) {
            selectedProperty.set(b);
        }
    }
    
    public final boolean isEnabled() {
        return enabledProperty.get();
    }
    
    public final BooleanProperty enabledProperty() {
        return enabledProperty;
    }
    
    public final void setSelected(boolean b) {
        selectedProperty.set(b);
    }
    
    public final boolean isSelected() {
        return selectedProperty.get();
    }
    
    public final BooleanProperty selectedProperty() {
        return selectedProperty;
    }
}