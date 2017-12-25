/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public enum Field {
    NAME("Name:", 
            createHelpMap(
                    "The name of the source port being configured. Will be displayed in the main window.", 
                    "The name of the total conversion being configured. Will be displayed in the main window.", 
                    "The name of the game defined by this IWAD file. Will be displayed in the main window.", 
                    "The name of the mod being configured. Will be displayed in the main window.", 
                    "The name of the PWAD being configured. Will be displayed in the PWAD list.")),
    DESC("Description:",
            createHelpMap(
                    "Description of the source port being configured. Will be displayed in the main window.",
                    "Description of the total conversion being configured. Will be displayed in the main window.",
                    "Description of the game defined by this IWAD file. Will be displayed in the main window",
                    "Description of the mod being configured. Will be displayed in the main window.",
                    "Field.DESC not implemented for PWAD's.")),
    TYPE("Type:", 
            createHelpMap(
                    "Choose 'PORT' to configure a source port for Doom (or another game). Choose 'TC' if you are configuring a total conversion that you want to have launched by another configured source port from the main tab.",
                    "Choose 'PORT' to configure a source port for Doom (or another game). Choose 'TC' if you are configuring a total conversion that you want to have launched by another configured source port from the main tab.",
                    "Field.TYPE assumed for IWAD's.",
                    "Field.TYPE assumed for mods.",
                    "Field.TYPE assumed for PWAD's.")),
    IWAD("IWAD:", 
            createHelpMap(
                    "Limit the IWAD's that this port is compatible with to the ones selected. None selected will allow the port to use any IWAD.",
                    "Limit the IWAD's that this total conversion is compatible with to the ones selected. None selected will allow the total conversion to be loaded on any IWAD.",
                    "Field.IWAD not implements for IWAD's.",
                    "Limit the IWAD's that this mod is compatible with to the ones selected. None selected will allow the mod to be used with any IWAD.",
                    "Field.IWAD not implemented for PWAD's.")),
    FILE("File:", 
            createHelpMap(
                    "Field.FILE not implemented for ports.",
                    "Field.FILE not implemented for tc's.",
                    "Enter the filename of the IWAD. This can be just the filename or a relative path (relative to your '%CONFIGHOME%/iwad' directory), or an absolute path.",
                    "Enter the filename of the mod. This can be just the filename or a relative path to the file (relative to your '%CONFIGHOME%/mods' directory), or an absolute path.",
                    "Field.FILE not implemented for PWAD's.")),
    WARP("Warp List:",
            createHelpMap(
                    "Field.WARP not implemented for ports.",
                    "Enter a comma-separated list of map names that this total conversion replaces to have them highlighted in the warp list. Doom 1 style map names are ExMx. Doom 2 style map names are MAPXX.",
                    "Field.WARP not implemented for IWAD's.",
                    "Field.WARP not implemented for mods.",
                    "Enter a comma-separated list of map names that this PWAD replaces to have them highlighted in the warp list. Doom 1 style map names are ExMx. Doom 2 style map names are MAPXX.")),
    GAME("Game:",
            createHelpMap(
                    "Field.GAME not implemented for ports.",
                    "Field.GAME not implemented for tc's.",
                    "Declares what game this IWAD corresponds to. Not required for most offical IWAD's (detected via their SHA-1 value). Useful to, for instance, associate FreeDoom Phase 1 with Doom (or Phase 2 with Doom2) so that you can load compatible mods/pwads. Valid values are 'DOOM' (Doom 1), 'ULTIMATE' (Ulitmate Doom), 'DOOM2' (Doom 2), 'HERETIC' (Heretic), 'HERETIC_EXP' (Heretic: Shadow of the Serpent Riders).",
                    "Field.GAME not implemented for mods.",
                    "Field.GAME not implemented for PWAD's.")),
    SKIPWADS("Skip PWADS:",
            createHelpMap(
                    "If true, do not offer any PWAD's for this port.",
                    "If true, do not offer any PWAD's for this total conversion.",
                    "Field.SKIPWADS not implemented for IWAD's.",
                    "Field.SKIPWADS not implemented for mods.",
                    "Field.SKIPWADS not implemented for PWAD's.")),
    WADFOLDER("Wad Folder(s):",
            createHelpMap(
                    "Limit the folders that the application should search for wads in for this port to the selected. None selected will allow all folders to be searched.",
                    "Limit the folders that the application should search for wads in for this total conversion to the selected. None selected will allow all folders to be searched.",
                    "Field.WADFOLDER not implemented for IWAD's.",
                    "Field.WADFOLDER not implemented for mods.",
                    "Field.WADFOLDER not implemented for PWAD's.")),
    PORT,
    CMD,
    ARGS,
    IMG,
    AUTHOR,
    TXT,
    IGNORE;
    
    public final String label;
    public final Map<Config.Type, String> helpMap;
    
    private Field(String label, Map<Config.Type, String> helpMap) {
        this.label = label;
        this.helpMap = helpMap;
    }
    
    private static Map<Config.Type, String> createHelpMap(String portHelp, String tcHelp, String iwadHelp, String modHelp, String pwadHelp) {
        EnumMap<Config.Type, String> theMap = new EnumMap<>(Config.Type.class);
        theMap.put(Config.Type.PORT, portHelp);
        theMap.put(Config.Type.TC, tcHelp);
        theMap.put(Config.Type.IWAD, iwadHelp);
        theMap.put(Config.Type.MOD, modHelp);
        theMap.put(Config.Type.PWAD, pwadHelp);
        
        return Collections.unmodifiableMap(theMap);
    }

    String iniKey() {
        return name().toLowerCase();
    }
}
