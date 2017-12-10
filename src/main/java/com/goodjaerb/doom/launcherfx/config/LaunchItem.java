/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import org.ini4j.Profile.Section;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public abstract class LaunchItem extends IniConfigurable implements Comparable<LaunchItem> {
    
    LaunchItem(Section iniSection, Field... fields) {
        super(iniSection, fields);
    }
    
    @Override
    public int compareTo(LaunchItem other) {
        Integer leftSort = getInt(Field.SORT, Integer.MAX_VALUE);
        Integer rightSort = other.getInt(Field.SORT, Integer.MAX_VALUE);

        if(leftSort.equals(rightSort)) {
            //In the event two items have matching sort values, this will make
            //sure the items are still placed in the Set.
            return 1;
        }
        
        return leftSort.compareTo(rightSort);
    }
}
