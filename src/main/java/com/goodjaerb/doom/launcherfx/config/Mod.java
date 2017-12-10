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
public class Mod extends IniConfigurable {
    public static final Mod STANDARD_MOD = new Mod();
    
    private Mod() {
        super(null);
        set(Field.NAME, "Standard");
        set(Field.DESC, "Run the selected Port/TC with no mods.");
        set(Field.TYPE, Config.Type.MOD.value());
    }
    
    Mod(Profile.Section iniSection) {
        super(iniSection, 
                Field.NAME,
                Field.DESC,
                Field.TYPE,
                Field.FILE,
                Field.IWAD,
                Field.PORT,
                Field.ARGS,
                Field.IMG);
    }
}
