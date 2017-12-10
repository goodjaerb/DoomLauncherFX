/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import org.ini4j.Profile.Section;

/**
 * Represents a launchable Port or Total Conversion. A TC will either reference a Port to launch with its CMD
 * with a specified set of ARGS, possibly relative to a WORKINGDIR, or the TC can have its own CMD.
 * 
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class Port extends LaunchItem {
    
    Port(Section iniSection) {
        super(iniSection, 
                Field.NAME,
                Field.DESC,
                Field.TYPE,
                Field.SORT,
                Field.IWAD,
                Field.WARP,
                Field.WADFOLDER,
                Field.SKIPWADS,
                Field.PORT,
                Field.WORKINGDIR,
                Field.CMD,
                Field.ARGS,
                Field.IMG);
    }
}
