/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.ini4j.Profile.Section;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class Port implements Comparable<Port> {
    
    public enum Field {
        NAME,
        DESC,
        TYPE,
        SORT,
        IWAD,
        WARP,
        SKIPWADS,
        WADFOLDER,
        PORT,
        WORKINGDIR,
        CMD,
        ARGS,
        IMG;
        
        private StringProperty valueProperty = new SimpleStringProperty(null);
    }
    
    private final Section iniSection;
    
    protected Port(Section iniSection) {
        this.iniSection = iniSection;

        for(Map.Entry<String, String> entry : iniSection.entrySet()) {
            try {
                Field.valueOf(entry.getKey().toUpperCase()).valueProperty.setValue(entry.getValue());
            }
            catch(IllegalArgumentException ex) {
                System.out.println("Field '" + entry.getKey() + "' not valid in section '" + iniSection.getName() + "'.");
            }
        }
    }
    
    public String get(Field f) {
        return f.valueProperty.getValue();
    }
    
    public StringProperty valueProperty(Field f) {
        return f.valueProperty;
    }
    
    String sectionName() {
        return iniSection.getName();
    }
    
    @Override
    public int compareTo(Port other) {
        Integer leftSort = Integer.parseInt(get(Field.SORT));
        Integer rightSort = Integer.parseInt(other.get(Field.SORT));

        if(leftSort.equals(rightSort)) {
            //In the event someone doesn't set a sort value, this will make
            //sure the items are still placed in the Set.
            return 1;
        }

        return leftSort.compareTo(rightSort);
    }
}
