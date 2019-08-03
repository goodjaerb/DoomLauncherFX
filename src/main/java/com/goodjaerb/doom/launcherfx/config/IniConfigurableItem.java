/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.lang3.SystemUtils;
import org.ini4j.Profile.Section;

import java.util.EnumMap;

/**
 * @author goodjaerb<goodjaerb @ gmail.com>
 */
public final class IniConfigurableItem {
    public static final IniConfigurableItem EMPTY_ITEM = new IniConfigurableItem(null);

    private final EnumMap<Field, ReadOnlyStringWrapper> fieldMap         = new EnumMap<>(Field.class);
    private final SimpleBooleanProperty                 selectedProperty = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty                 enabledProperty  = new SimpleBooleanProperty(true);

    private final Section iniSection;

    IniConfigurableItem(Section iniSection) {
        this.iniSection = iniSection;

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
     *
     * @return
     */
    public final String sectionName() {
        return iniSection == null ? "" : iniSection.getName();
    }

    public final void set(Field f, String value) {
        ReadOnlyStringWrapper w = fieldMap.get(f);
        if(w != null) {
            w.setValue(value);
        }
        else {
            fieldMap.put(f, new ReadOnlyStringWrapper(value));
        }
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
        String value = get(f);
        return value == null ? ifNull : value;
    }

    public final Integer getInt(Field f, Integer ifNull) {
        String value = get(f);
        return value == null ? ifNull : Integer.parseInt(value);
    }

    public final Boolean getBoolean(Field f) {
        String value = get(f);
        return value != null && Boolean.parseBoolean(value);
    }

    public final String getCmd() {
        if(SystemUtils.IS_OS_WINDOWS) {
            return get(Field.WIN_CMD);
        }
        else if(SystemUtils.IS_OS_LINUX) {
            return get(Field.LINUX_CMD);
        }
        return null;
    }

    public final boolean isType(Config.Type type) {
        return getType() == type;
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
            selectedProperty.set(false);
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
