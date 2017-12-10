/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import org.ini4j.Profile;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class Pwad extends IniConfigurable {
    
    Pwad(Profile.Section iniSection) {
        super(iniSection, 
                Field.NAME,
                Field.AUTHOR,
                Field.WARP);
    }
}
