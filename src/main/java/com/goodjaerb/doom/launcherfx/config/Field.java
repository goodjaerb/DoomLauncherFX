/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import com.goodjaerb.doom.launcherfx.config.input.BooleanInput;
import com.goodjaerb.doom.launcherfx.config.input.BrowseInput;
import com.goodjaerb.doom.launcherfx.config.input.Input;
import com.goodjaerb.doom.launcherfx.config.input.ListInput;
import com.goodjaerb.doom.launcherfx.config.input.MultiListInput;
import com.goodjaerb.doom.launcherfx.config.input.HiddenInput;
import com.goodjaerb.doom.launcherfx.config.input.TextInput;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public enum Field {
    NAME("Name:", new TextInput(),
            createHelpMap(
                    "The name of the source port being configured. Will be displayed in the main window.", 
                    "The name of the total conversion being configured. Will be displayed in the main window.", 
                    "The name of the game defined by this IWAD file. Will be displayed in the main window.", 
                    "The name of the mod being configured. Will be displayed in the main window.", 
                    "The name of the PWAD being configured. Will be displayed in the PWAD list."), 
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD,
            Config.Type.PWAD),
    DESC("Description:", new TextInput(),
            createHelpMap(
                    "Description of the source port being configured. Will be displayed in the main window.",
                    "Description of the total conversion being configured. Will be displayed in the main window.",
                    "Description of the game defined by this IWAD file. Will be displayed in the main window",
                    "Description of the mod being configured. Will be displayed in the main window.",
                    "Field.DESC not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD),
    TYPE("Type:", new HiddenInput(),
            createHelpMap(
                    "Field.TYPE assumed for ports.",
                    "Field.TYPE assumed for total conversions.",
                    "Field.TYPE assumed for IWAD's.",
                    "Field.TYPE assumed for mods.",
                    "Field.TYPE assumed for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD,
            Config.Type.PWAD),
    IWAD("IWAD:", new MultiListInput(),
            createHelpMap(
                    "Limit the IWAD's that this port is compatible with to the ones selected. None selected will allow the port to use any IWAD.",
                    "Limit the IWAD's that this total conversion is compatible with to the ones selected. None selected will allow the total conversion to be loaded on any IWAD.",
                    "Field.IWAD not implements for IWAD's.",
                    "Limit the IWAD's that this mod is compatible with to the ones selected. None selected will allow the mod to be used with any IWAD.",
                    "Field.IWAD not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.MOD),
    FILE("File:", new BrowseInput(),
            createHelpMap(
                    "Field.FILE not implemented for ports.",
                    "Field.FILE not implemented for tc's.",
                    "Enter the filename of the IWAD. This can be just the filename or a relative path (relative to your '%CONFIGHOME%/iwad' directory), or an absolute path.",
                    "Enter the filename of the mod. This can be just the filename or a relative path to the file (relative to your '%CONFIGHOME%/mods' directory), or an absolute path.",
                    "Field.FILE not implemented for PWAD's."),
            Config.Type.IWAD,
            Config.Type.MOD),
    WARP("Warp List:", new TextInput(),
            createHelpMap(
                    "Field.WARP not implemented for ports.",
                    "Enter a comma-separated list of map names that this total conversion replaces to have them highlighted in the warp list. Doom 1 style map names are ExMx. Doom 2 style map names are MAPXX.",
                    "Field.WARP not implemented for IWAD's.",
                    "Field.WARP not implemented for mods.",
                    "Enter a comma-separated list of map names that this PWAD replaces to have them highlighted in the warp list. Doom 1 style map names are ExMx. Doom 2 style map names are MAPXX."),
            Config.Type.TC,
            Config.Type.PWAD),
    GAME("Game:", new ListInput("DOOM", "ULTIMATE", "DOOM2", "HERETIC", "HERETIC_EXP"),
            createHelpMap(
                    "Field.GAME not implemented for ports.",
                    "Field.GAME not implemented for tc's.",
                    "Declares what game this IWAD corresponds to. Not required for most offical IWAD's (detected via their SHA-1 value). Useful to, for instance, associate FreeDoom Phase 1 with Doom (or Phase 2 with Doom2) so that you can load compatible mods/pwads. Valid values are 'DOOM' (Doom 1), 'ULTIMATE' (Ulitmate Doom), 'DOOM2' (Doom 2), 'HERETIC' (Heretic), 'HERETIC_EXP' (Heretic: Shadow of the Serpent Riders).",
                    "Field.GAME not implemented for mods.",
                    "Field.GAME not implemented for PWAD's."),
            Config.Type.IWAD),
    SKIPWADS("Skip PWADS", new BooleanInput(),
            createHelpMap(
                    "If true, do not offer any PWAD's for this port.",
                    "If true, do not offer any PWAD's for this total conversion.",
                    "Field.SKIPWADS not implemented for IWAD's.",
                    "Field.SKIPWADS not implemented for mods.",
                    "Field.SKIPWADS not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC),
    WADFOLDER("Wad Folder(s):", new MultiListInput(),
            createHelpMap(
                    "Limit the folders that the application should search for wads in for this port to the selected.\nNone selected will allow all folders to be searched.",
                    "Limit the folders that the application should search for wads in for this total conversion to the selected. None selected will allow all folders to be searched.",
                    "Field.WADFOLDER not implemented for IWAD's.",
                    "Field.WADFOLDER not implemented for mods.",
                    "Field.WADFOLDER not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC),
    PORT("Supported Port(s):", new MultiListInput(),
            createHelpMap(
                    "Field.PORT not implemented for ports.",
                    "Select the ports that this total conversion can use to run.",
                    "Field.PORT not implemented for IWAD's.",
                    "Select the ports that this mod is compatible with.",
                    "Field.PORT not implemented for PWAD's."),
            Config.Type.TC,
            Config.Type.MOD),
    CMD("Command:", new BrowseInput(),
            createHelpMap(
                    "Enter the command to run this source port. Must be an absolute path or just the executable name if it is on a system path. Command arguments can be placed in the Arguments field.",
                    "Field.CMD not implemented for total conversions.",
                    "Field.CMD not implemented for IWAD's.",
                    "Field.CMD not implemented for mods.",
                    "Field.CMD not implemented for PWAD's."),
            Config.Type.PORT),
    ARGS("Command Arguments:", new TextInput(),
            createHelpMap(
                    "Enter command line arguments for the source port.\nFiles referenced must be absolute paths OR they may be relative paths stemming from %CONFIGHOME%/mods.\nIn either case file names/paths should be enclosed in \"double-quotes\".\nPlease use / as the path separator, \\ may cause parsing issues.",
                    "Enter command line arguments to forward to the selected source port to run this total conversion. Files referenced must be absolute paths OR they may be relative paths stemming from %CONFIGHOME%/mods. In either case file names/paths should be enclosed in \"double-quotes\". Please use / as the path separator, \\ may cause parsing issues.",
                    "Field.ARGS not implemented for IWAD's.",
                    "Field.ARGS not implemented for mods.",
                    "Enter command line arguments for the PWAD. While the application will attempt to auto-detect matching .deh files for a wad or pk3, more complicated wads that need multiple different files will need to have args defined. Files referenced must be absolute paths OR if just a filename is entered it will be looked for in the same location as the wad. In either case file names/paths should be enclosed in \"double-quotes\". Please use / as the path separator, \\ may cause parsing issues."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.PWAD),
    IMG("Image:", new BrowseInput(),
            createHelpMap(
                    "Enter an image to display on the button selector for this port. Can be just a filename if contained in %CONFIGHOME%/images, otherwise an absolute path.",
                    "Enter an image to display on the button selector for this total conversion. Can be just a filename if contained in %CONFIGHOME%/images, otherwise an absolute path.",
                    "Enter an image to display on the button selector for this IWAD. Can be just a filename if contained in %CONFIGHOME%/images, otherwise an absolute path.",
                    "Enter an image to display on the button selector for this mod. Can be just a filename if contained in %CONFIGHOME%/images, otherwise an absolute path.",
                    "Field.IMG not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD),
    AUTHOR("WAD Author:", new TextInput(),
            createHelpMap(
                    "Field.AUTHOR not implemented for ports.",
                    "Field.AUTHOR not implemented for total conversions.",
                    "Field.AUTHOR not implemented for IWAD's.",
                    "Field.AUTHOR not implemented for mods.",
                    "Enter the WAD author's name to have it displayed in the PWAD list."), 
            Config.Type.PWAD),
    TXT("Text File:", new BrowseInput(),
            createHelpMap(
                    "Field.TXT not implemented for ports.",
                    "Field.TXT not implemented for total conversions.",
                    "Field.TXT not implemented for IWAD's.",
                    "Field.TXT not implemented for mods.",
                    "Enter the filename of the text file associated with this wad.\nOnly necessary if the filenames do not match as the application will auto-detect matching text files."), 
            Config.Type.PWAD),
    IGNORE("Ignore:", new BooleanInput(),
            createHelpMap(
                    "Field.IGNORE not implemented for ports.",
                    "Field.IGNORE not implemented for total conversions.",
                    "Field.IGNORE not implemented for IWAD's.",
                    "Field.IGNORE not implemented for mods.",
                    "Ignored files will not be shown in the PWAD list by default."), 
            Config.Type.PWAD);
    
    public final String label;
    public final Input input;
    public final Map<Config.Type, String> helpMap;
    public final List<Config.Type> validTypes;
    
    private Field(String label, Input input, Map<Config.Type, String> helpMap, Config.Type... validTypes) {
        this.label = label;
        this.input = input;
        this.helpMap = helpMap;
        this.validTypes = Collections.unmodifiableList(Arrays.asList(validTypes));
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
