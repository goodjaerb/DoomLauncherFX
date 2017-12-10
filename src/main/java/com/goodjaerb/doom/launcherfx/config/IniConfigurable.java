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
abstract class IniConfigurable {
    
    private final EnumMap<Field, ReadOnlyStringWrapper> fieldMap;
    private final Section iniSection;
    
    IniConfigurable(Section iniSection, Field... fields) {
        this.iniSection = iniSection;
        this.fieldMap = new EnumMap(Field.class);

        for(Field f : fields) {
            String value = iniSection.get(f.iniKey());
            if(value != null) {
                fieldMap.put(f, new ReadOnlyStringWrapper(value));
            }
        }
    }
    
    final String sectionName() {
        return iniSection.getName();
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
    
    public final ReadOnlyStringProperty valueProperty(Field f) {
        return fieldMap.get(f).getReadOnlyProperty();
    }
}
