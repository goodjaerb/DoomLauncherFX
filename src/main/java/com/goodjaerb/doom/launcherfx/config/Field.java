/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public enum Field {
    NAME("Name:", InputType.TEXT, 
            (t) -> {
                return t != Config.Type.PWAD;
            },
            createHelpMap(
                    "The name of the source port being configured.\nWill be displayed in the main window.", 
                    "The name of the total conversion being configured.\nWill be displayed in the main window.", 
                    "The name of the game defined by this IWAD file.\nWill be displayed in the main window.", 
                    "The name of the mod being configured.\nWill be displayed in the main window.", 
                    "The name of the PWAD being configured.\nWill be displayed in the PWAD list."), 
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD,
            Config.Type.PWAD),
    VERSION("Version:", InputType.TEXT,
            createHelpMap(
                    "Enter version for the item. Optional.\nCan be anything you want to use to differentiate the item from other's with\nthe same name, for example multiple versions of GZDoom or Brutal Doom.",
                    "Enter version for the item. Optional.\nCan be anything you want to use to differentiate the item from other's with\nthe same name, for example multiple versions of GZDoom or Brutal Doom.",
                    "Enter version for the item. Optional.\nCan be anything you want to use to differentiate the item from other's with\nthe same name, for example multiple versions of GZDoom or Brutal Doom.",
                    "Enter version for the item. Optional.\nCan be anything you want to use to differentiate the item from other's with\nthe same name, for example multiple versions of GZDoom or Brutal Doom.",
                    "Field.IMG not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD),
    DESC("Description:", InputType.TEXT,
            createHelpMap(
                    "Description of the source port being configured.\nWill be displayed in the main window.",
                    "Description of the total conversion being configured.\nWill be displayed in the main window.",
                    "Description of the game defined by this IWAD file.\nWill be displayed in the main window",
                    "Description of the mod being configured.\nWill be displayed in the main window.",
                    "Field.DESC not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD),
    HTTP("Link:", InputType.TEXT, 
            createHelpMap(
                    "Enter a web address for this source port.",
                    "Enter a web address for this total conversion.",
                    "Enter a web address for this IWAD.",
                    "Enter a web address for this mod.",
                    "Field.HTTP not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD),
    TYPE("Type:", InputType.HIDDEN,
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
    SORT("Sort:", InputType.HIDDEN,
            createHelpMap(
                    "Field.SORT auto for ports.",
                    "Field.SORT auto for total conversions.",
                    "Field.SORT auto for IWAD's.",
                    "Field.SORT auto for mods.",
                    "Field.SORT not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD),
    IWAD("Restrict IWAD's:", InputType.MULTI_LIST,
            createHelpMap(
                    "Limit the IWAD's that this port is compatible with to the ones selected.\nNone selected will allow the port to use any IWAD.",
                    "Limit the IWAD's that this total conversion is compatible with to the ones selected.\nNone selected will allow the total conversion to be loaded on any IWAD.",
                    "Field.IWAD not implements for IWAD's.",
                    "Limit the IWAD's that this mod is compatible with to the ones selected.\nNone selected will allow the mod to be used with any IWAD.",
                    "Field.IWAD not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.MOD),
    FILE("File:", InputType.BROWSE, 
            true,
            createHelpMap(
                    "Field.FILE not implemented for ports.",
                    "Field.FILE not implemented for tc's.",
                    "Enter the filename of the IWAD.\nThis can be just the filename or a relative path (relative to your '%CONFIGHOME%/iwad' directory), or an absolute path.",
                    "Enter the file(s) required of the mod.\nThis can be just the filenames or a relative paths to the files (relative to your '%CONFIGHOME%/mods' directory), or an absolute path.",
                    "Field.FILE not implemented for PWAD's."),
            Config.Type.IWAD,
            Config.Type.MOD),
    WARP("Warp List:", InputType.TEXT,
            createHelpMap(
                    "Field.WARP not implemented for ports.",
                    "Enter a comma-separated list of map names that this total conversion replaces to have them highlighted in the warp list.\nDoom 1 style map names are ExMx. Doom 2 style map names are MAPXX.",
                    "Field.WARP not implemented for IWAD's.",
                    "Field.WARP not implemented for mods.",
                    "Enter a comma-separated list of map names that this PWAD replaces to have them highlighted in the warp list.\nDoom 1 style map names are ExMx. Doom 2 style map names are MAPXX."),
            Config.Type.TC,
            Config.Type.PWAD),
    GAME("Game:", InputType.LIST,
            createHelpMap(
                    "Field.GAME not implemented for ports.",
                    "Field.GAME not implemented for tc's.",
                    "Declares what game this IWAD corresponds to.\nNot required for most offical IWAD's (detected via their SHA-1 value).\nUseful to, for instance, associate FreeDoom Phase 1 with Doom (or Phase 2 with Doom2) so that you can load compatible mods/pwads.",
                    "Field.GAME not implemented for mods.",
                    "Field.GAME not implemented for PWAD's."),
            Config.Type.IWAD),
    SKIPWADS("Skip PWADS", InputType.BOOLEAN,
            createHelpMap(
                    "If true, do not offer any PWAD's for this port.",
                    "If true, do not offer any PWAD's for this total conversion.",
                    "Field.SKIPWADS not implemented for IWAD's.",
                    "Field.SKIPWADS not implemented for mods.",
                    "Field.SKIPWADS not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC),
    WADFOLDER("Wad Folder(s):", InputType.MULTI_LIST,
            createHelpMap(
                    "Limit the folders that the application should search for wads in for this port to the selected.\nNone selected will allow all folders to be searched.",
                    "Limit the folders that the application should search for wads in for this total conversion to the selected.\nNone selected will allow all folders to be searched.",
                    "Field.WADFOLDER not implemented for IWAD's.",
                    "Field.WADFOLDER not implemented for mods.",
                    "Field.WADFOLDER not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC),
    PORT("Supported Port(s):", InputType.MULTI_LIST,
            (t) -> { return t == Config.Type.TC; },
            createHelpMap(
                    "Field.PORT not implemented for ports.",
                    "Select the ports that this total conversion can use to run.",
                    "Field.PORT not implemented for IWAD's.",
                    "Select the ports that this mod is compatible with.",
                    "Field.PORT not implemented for PWAD's."),
            Config.Type.TC,
            Config.Type.MOD),
    CMD("Command:", InputType.BROWSE, 
            true,
            createHelpMap(
                    "Enter the command to run this source port.\nMust be an absolute path or just the executable name if it is on a system path.\nPaths with spaces will need to be encapsulated in \"double quotes\".\nCommand arguments can be placed in the Arguments field.",
                    "Field.CMD not implemented for total conversions.",
                    "Field.CMD not implemented for IWAD's.",
                    "Field.CMD not implemented for mods.",
                    "Field.CMD not implemented for PWAD's."),
            Config.Type.PORT),
    ARGS("Command Arguments:", InputType.TEXT,
            createHelpMap(
                    "Enter command line arguments for the source port.\nFiles referenced must be absolute paths OR they may be relative paths stemming from %CONFIGHOME%/mods.\nIn either case file names/paths should be enclosed in \"double-quotes\".",
                    "Enter command line arguments to forward to the selected source port to run this total conversion.\nFiles referenced must be absolute paths OR they may be relative paths stemming from %CONFIGHOME%/mods.\nIn either case file names/paths should be enclosed in \"double-quotes\".",
                    "Field.ARGS not implemented for IWAD's.",
                    "Enter command line arguments to launch this mod. If this is set, the File field is ignored.\nFiles referenced must be absolute paths OR they may be relative paths stemming from %CONFIGHOME%/mods.\nIn either case file names/paths should be enclosed in \"double-quotes\".",
                    "Enter command line arguments for the PWAD.\nWhile the application will attempt to auto-detect matching .deh files for a wad or pk3, more complicated wads that need multiple different files will need to have args defined.\nFiles referenced must be absolute paths OR if just a filename is entered it will be looked for in the same location as the wad.\nIn either case file names/paths should be enclosed in \"double-quotes\"."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.MOD,
            Config.Type.PWAD),
    IMG("Image:", InputType.BROWSE,
            createHelpMap(
                    "Enter an image to display on the button selector for this port.\nCan be just a filename if contained in %CONFIGHOME%/images, otherwise an absolute path.",
                    "Enter an image to display on the button selector for this total conversion.\nCan be just a filename if contained in %CONFIGHOME%/images, otherwise an absolute path.",
                    "Enter an image to display on the button selector for this IWAD.\nCan be just a filename if contained in %CONFIGHOME%/images, otherwise an absolute path.",
                    "Enter an image to display on the button selector for this mod.\nCan be just a filename if contained in %CONFIGHOME%/images, otherwise an absolute path.",
                    "Field.IMG not implemented for PWAD's."),
            Config.Type.PORT,
            Config.Type.TC,
            Config.Type.IWAD,
            Config.Type.MOD),
    AUTHOR("WAD Author:", InputType.TEXT,
            createHelpMap(
                    "Field.AUTHOR not implemented for ports.",
                    "Field.AUTHOR not implemented for total conversions.",
                    "Field.AUTHOR not implemented for IWAD's.",
                    "Field.AUTHOR not implemented for mods.",
                    "Enter the WAD author's name to have it displayed in the PWAD list."), 
            Config.Type.PWAD),
    TXT("Text File:", InputType.BROWSE,
            createHelpMap(
                    "Field.TXT not implemented for ports.",
                    "Field.TXT not implemented for total conversions.",
                    "Field.TXT not implemented for IWAD's.",
                    "Field.TXT not implemented for mods.",
                    "Enter the filename of the text file associated with this wad.\nOnly necessary if the filenames do not match as the application will auto-detect matching text files."), 
            Config.Type.PWAD),
    IGNORE("Ignore:", InputType.HIDDEN,
            createHelpMap(
                    "Field.IGNORE not implemented for ports.",
                    "Field.IGNORE not implemented for total conversions.",
                    "Field.IGNORE not implemented for IWAD's.",
                    "Field.IGNORE not implemented for mods.",
                    "Ignored files will not be shown in the PWAD list by default."), 
            Config.Type.PWAD);
    
    public final String label;
    public final InputType inputType;
    public final Map<Config.Type, String> helpMap;
    public final List<Config.Type> validTypes;
    
    private final Function<Config.Type, Boolean> isRequiredFunc;
    
    private Field(String label, InputType inputType, boolean isRequired, Map<Config.Type, String> helpMap, Config.Type... validTypes) {
        this(label, inputType, (t) -> { return isRequired; }, helpMap, validTypes);
    }
    
    private Field(String label, InputType inputType, Map<Config.Type, String> helpMap, Config.Type... validTypes) {
        this(label, inputType, (t) -> { return false; }, helpMap, validTypes);
    }
        
    private Field(String label, InputType inputType, Function<Config.Type, Boolean> isRequiredFunc, Map<Config.Type, String> helpMap, Config.Type... validTypes) {
        this.label = label;
        this.inputType = inputType;
        this.helpMap = helpMap;
        this.validTypes = Collections.unmodifiableList(Arrays.asList(validTypes));
        this.isRequiredFunc = isRequiredFunc;
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
    
    public boolean isRequired(Config.Type type) {
        return isRequiredFunc.apply(type);
    }

    public String iniKey() {
        return name().toLowerCase();
    }
}
