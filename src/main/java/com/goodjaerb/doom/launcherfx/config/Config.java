/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

import com.goodjaerb.doom.launcherfx.LauncherFX;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.*;

/**
 * @author goodjaerb<goodjaerb @ gmail.com>
 */
public class Config {
    public enum Type {
        PORT, TC, MOD, IWAD, PWAD;

        public String iniValue() {
            return name().toLowerCase();
        }
    }

    public static final String TRUE  = "true";
    public static final String FALSE = "false";

    public static final String USER_HOME   = System.getProperty("user.home");
    public static final String CONFIG_DIR  = ".launcherfx";
    public static final String CONFIG_FILE = "launcherfx.ini";
    public static final String HIDE_FILE   = "launcherfx_hide.ini";

    public static final String DIR_IMAGES         = "images";
    public static final String DIR_IWAD           = "iwad";
    public static final String DIR_MODS           = "mods";
    public static final String DIR_WADS           = "wads";
    public static final String DIR_BOOM           = "boom";
    public static final String DIR_LIMIT_REMOVING = "limit-removing";
    public static final String DIR_VANILLA        = "vanilla";
    public static final String DIR_DOOM           = "doom";
    public static final String DIR_DOOM2          = "doom2";

    private static final String CONFIG_LAUNCHER_DATA_DIR_SECTION = "LauncherFXDataDir";
    private static final String CONFIG_LAUNCHER_DATA_DIR_KEY     = "launcher-data";

    private static final Path   HOME_CONFIG_FILE_PATH = FileSystems.getDefault().getPath(USER_HOME, CONFIG_DIR, CONFIG_FILE);
    private static final Ini    INI_FILE              = new Ini();
    private static final Config INSTANCE              = new Config();

    private final List<IniConfigurableItem> CONFIGURABLES  = new ArrayList<>();
    private final Set<String>               hiddenSections = new HashSet<>();

    private String configHome;

    private Config() {

    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public String getConfigHome() {
        return configHome;
    }

    public Collection<IniConfigurableItem> getConfigurables() {
        SortedSet<IniConfigurableItem> sortedConfigurables = new TreeSet<>((o1, o2) -> {
            int sort1 = o1.getInt(Field.SORT, Integer.MAX_VALUE);
            int sort2 = o2.getInt(Field.SORT, Integer.MAX_VALUE);

            // i know this breaks a contract but i just want to know that if two things are equal which side
            // they will sort on.
            return sort1 == sort2 ? 1 : sort1 - sort2;
        });
        sortedConfigurables.addAll(CONFIGURABLES);
        return Collections.unmodifiableCollection(sortedConfigurables);
    }

    /**
     * Add a new section to INI_FILE.
     * If section name exists, append "_1", with increasing numbers until
     * no more conflict.
     *
     * @param sectionName
     * @return the section name created, which may differ from the sectionName
     * passed as a parameter if there was a name conflict resolution.
     */
    public String addNewSection(String sectionName) {
        String originalSectionName = sectionName;
        int appendNum = 0;
        while(getConfigurableByName(sectionName) != null) {
            appendNum++;
            sectionName = originalSectionName + "_" + appendNum;
        }
        INI_FILE.add(sectionName);
        LauncherFX.info("Added new INI section: " + sectionName);
        return sectionName;
    }

    public void deleteSection(String sectionName) {
        INI_FILE.remove(sectionName);
    }

    public void hideSection(String sectionName) {
        hiddenSections.add(sectionName);
    }

    public void unHideSection(String sectionName) {
        hiddenSections.remove(sectionName);
    }

    public boolean isHidden(String sectionName) {
        return hiddenSections.contains(sectionName);
    }

    public void update(String section, Field f, boolean value) {
        if(value) {
            update(section, f, TRUE);
        }
        update(section, f, null);
    }

    public void update(String section, Field f, String value) {
        if(value == null || value.trim().equals("")) {
            INI_FILE.remove(section, f.iniKey());
            LauncherFX.info("Updated section '" + section + "'. Removed " + f.iniKey());
        }
        else {
            INI_FILE.put(section, f.iniKey(), value);
            LauncherFX.info("Updated section '" + section + "'. Set " + f.iniKey() + "=" + value);
        }
    }

    private List<IniConfigurableItem> getOfTypesSorted(Config.Type... types) {
        List<Config.Type> typesList = Arrays.asList(types);
        int sort = 0;

        List<IniConfigurableItem> items = new ArrayList<>();
        for(IniConfigurableItem item : getConfigurables()) {
            if(typesList.contains(item.getType())) {
                item.set(Field.SORT, Integer.toString(sort++));
                items.add(item);
            }
        }

        return Collections.unmodifiableList(items);
    }

    /**
     * Gets a list of IniConigurableItems where getType() == Config.Type.PORT.
     * Does NOT include total conversions (Config.Type.TC).
     *
     * @return
     */
    public List<IniConfigurableItem> getPorts() {
        List<IniConfigurableItem> ports = new ArrayList<>();
        getConfigurables().stream().filter((ic) -> (ic.isType(Type.PORT))).forEachOrdered(ports::add);
        return Collections.unmodifiableList(ports);
    }

    public List<IniConfigurableItem> getPortsAndTcs() {
        return getOfTypesSorted(Type.PORT, Type.TC);
    }

    public List<IniConfigurableItem> getIwads() {
        return getOfTypesSorted(Type.IWAD);
    }

    public List<IniConfigurableItem> getMods() {
        return getOfTypesSorted(Type.MOD);
    }

    public IniConfigurableItem getConfigurableByName(String sectionName) {
        for(IniConfigurableItem ic : CONFIGURABLES) {
            if(ic.sectionName().equals(sectionName)) {
                return ic;
            }
        }
        return null;
    }

    /**
     * @return true if config file exists in user.home. false otherwise.
     */
    public boolean isFirstRun() {
        Path configFilePath = FileSystems.getDefault().getPath(USER_HOME, CONFIG_DIR, CONFIG_FILE);
        return !Files.exists(configFilePath);
    }

    public void initializeConfig() throws IOException {
        initializeConfig(null);
    }

    public void initializeConfig(Path configCustomPath) throws IOException {
        FileSystem fs = FileSystems.getDefault();
        Path homeConfigFilePath = fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_FILE);

        if(configCustomPath == null) {
            configHome = homeConfigFilePath.getParent().toString();

            if(!Files.exists(homeConfigFilePath)) {
                createDefaultConfig(homeConfigFilePath);
            }
        }
        else {
            configHome = configCustomPath.toString();

            if(!Files.exists(homeConfigFilePath)) {
                createPointerConfig(homeConfigFilePath, configCustomPath);
            }
        }
        loadConfig();
    }

    public void loadConfig() throws IOException {
        INI_FILE.load(Files.newBufferedReader(HOME_CONFIG_FILE_PATH));
        String datadir = INI_FILE.get(CONFIG_LAUNCHER_DATA_DIR_SECTION, CONFIG_LAUNCHER_DATA_DIR_KEY);

        FileSystem fs = FileSystems.getDefault();
        if(datadir != null) {
            configHome = datadir;

            Path customConfigFilePath = fs.getPath(configHome, CONFIG_FILE);
            if(!Files.exists(customConfigFilePath)) {
                createDefaultConfig(customConfigFilePath);
            }

            INI_FILE.clear();
            INI_FILE.load(Files.newBufferedReader(customConfigFilePath));
        }
        else {
            configHome = USER_HOME + File.separator + CONFIG_DIR;
        }

        Path hideFilePath = fs.getPath(configHome, HIDE_FILE);
        if(Files.exists(hideFilePath)) {
            try(BufferedReader hideFileReader = Files.newBufferedReader(hideFilePath)) {
                String line;
                while((line = hideFileReader.readLine()) != null) {
                    hiddenSections.add(line);
                }
            }
        }

        //create the directory structure
        Path[] configDirs = {
                fs.getPath(configHome, DIR_IMAGES),
                fs.getPath(configHome, DIR_IWAD),
                fs.getPath(configHome, DIR_MODS),
                fs.getPath(configHome, DIR_WADS, DIR_BOOM, DIR_DOOM),
                fs.getPath(configHome, DIR_WADS, DIR_BOOM, DIR_DOOM2),
                fs.getPath(configHome, DIR_WADS, DIR_LIMIT_REMOVING, DIR_DOOM),
                fs.getPath(configHome, DIR_WADS, DIR_LIMIT_REMOVING, DIR_DOOM2),
                fs.getPath(configHome, DIR_WADS, DIR_VANILLA, DIR_DOOM),
                fs.getPath(configHome, DIR_WADS, DIR_VANILLA, DIR_DOOM2),
        };
        for(Path p : configDirs) {
            Files.createDirectories(p);
        }

        parseIni();
    }

    public void writeIni() throws IOException {
        Iterator<Section> sectionItr = INI_FILE.values().iterator();
        while(sectionItr.hasNext()) {
            Section s = sectionItr.next();
            if(s.size() <= 1) {
                // All sections have at least a type field and so if only one
                // field is set there's no use keeping it in the ini.
                sectionItr.remove();
                hiddenSections.remove(s.getName());
                LauncherFX.info("Deleted section '" + s.getName() + "' due to no more entries.");
            }
        }

        Path hideFilePath = Paths.get(configHome, HIDE_FILE);
        if(hiddenSections.isEmpty()) {
            Files.deleteIfExists(hideFilePath);
        }
        else {
            try(PrintWriter hideWriter = new PrintWriter(Files.newBufferedWriter(hideFilePath))) {
                for(String sectionName : hiddenSections) {
                    hideWriter.println(sectionName);
                }
            }
        }

        INI_FILE.store(Files.newBufferedWriter(Paths.get(configHome, CONFIG_FILE)));
        parseIni();
        LauncherFX.info("launcherfx.ini wrote to disk.");
    }

    private void parseIni() {
        CONFIGURABLES.clear();
        INI_FILE.values().forEach((section) -> {
//            Type type = Type.valueOf(section.get(Field.TYPE.iniKey()).toUpperCase());
            // if i ever have a Config.Type and later remove it from the source, i need this check to make sure
            // i don't load any deprecated types into configurables.
            CONFIGURABLES.add(new IniConfigurableItem(section));
        });
    }

    private void createPointerConfig(Path configFilePath, Path pointToPath) throws IOException {        //check that .launcherfx directory exists.
        if(!Files.exists(configFilePath.getParent())) {
            Files.createDirectory(configFilePath.getParent());
        }

        //check that launcherfx.ini file exists.
        if(!Files.exists(configFilePath)) {
            Files.createFile(configFilePath);
        }

        //initialize with example data.
        try(PrintWriter writer = new PrintWriter(Files.newBufferedWriter(configFilePath, StandardOpenOption.WRITE))) {
            writer.println("; Be safe and use a forward slash / in your paths. Backslash \\ will not parse well.");
            writer.println("[" + CONFIG_LAUNCHER_DATA_DIR_SECTION + "]");
            writer.println(CONFIG_LAUNCHER_DATA_DIR_KEY + "=" + pointToPath.toString().replaceAll("\\\\", "/"));
            writer.println();
        }
    }

    private void createDefaultConfig(Path configFilePath) throws IOException {
        //check that .launcherfx directory exists.
        if(!Files.exists(configFilePath.getParent())) {
            Files.createDirectory(configFilePath.getParent());
        }

        //check that launcherfx.ini file exists.
        if(!Files.exists(configFilePath)) {
            Files.createFile(configFilePath);
        }

        //initialize with example data.
        try(PrintWriter writer = new PrintWriter(Files.newBufferedWriter(configFilePath, StandardOpenOption.WRITE))) {
            writer.println("; A directory '" + CONFIG_DIR + "' will be created in the user's home directory. Within this directory will be another series of directories to store various wads and mods as described below. Also will be this '" + CONFIG_FILE + "' files.");
            writer.println("; The directory structure will be as follows:");
            writer.println("; <user home>/" + CONFIG_DIR + "/");
            writer.println("; +-> " + DIR_IMAGES + "/");
            writer.println("; +-> " + DIR_IWAD + "/");
            writer.println("; +-> " + DIR_MODS + "/");
            writer.println("; +-> " + DIR_WADS + "/");
            writer.println("; |    +-> " + DIR_BOOM + "/\t\tUsed for wads requiring Boom-compatible editing extensions source ports.");
            writer.println("; |         +-> " + DIR_DOOM + "/\t\tWads that require the iwad for Doom");
            writer.println("; |         +-> " + DIR_DOOM2 + "/\t\tWads that require the iwad for Doom 2");
            writer.println("; |    +-> " + DIR_LIMIT_REMOVING + "/\t\tUsed for wads requiring limit-removing source ports but no further extensions.");
            writer.println("; |         +-> " + DIR_DOOM + "/\t\tWads that require the iwad for Doom");
            writer.println("; |         +-> " + DIR_DOOM2 + "/\t\tWads that require the iwad for Doom 2");
            writer.println("; |    +-> " + DIR_VANILLA + "/\t\tUsed for wads that do not require 'limit-removing' source ports.");
            writer.println("; |         +-> " + DIR_DOOM + "/");
            writer.println("; |         +-> " + DIR_DOOM2 + "/");
            writer.println("; +-> " + CONFIG_FILE);
            writer.println();
            writer.println("; if you want to keep the above data structures in another location, uncomment the following two lines and give the absolute path to the location you want. Leave THIS ini here with this line in it, and copy the rest of your configuration below into the ini at your new location.");
            writer.println("; The application will see this line pointing to the other location, then open the ini that is over there.");
            writer.println("; [" + CONFIG_LAUNCHER_DATA_DIR_SECTION + "]");
            writer.println("; " + CONFIG_LAUNCHER_DATA_DIR_KEY + "=/path/to/launcher/data");
            writer.println();
            writer.println("; A section describing a Doom source port. The section name may be referenced from other options.");
            writer.println("; Use 'wadfolder=' in a port section to limit which wads folder to search for pwads. By default the application creates folders called 'boom', 'limit-removing', and 'vanilla'. If you need more you can create them and use the name in the ini. Optional. No value for wadfolder will assume all wads are legal.");
            writer.println("; Each section must have a 'type=', defining each section as a 'port', 'mod', or 'iwad'");
            writer.println("; Use quotes (\"...\") around the value for cmd if there are spaces in the path.");
            writer.println("; If 'img=' is not an absolute path, the 'images' folder above will be checked for the image file. If defining an absolute path or path with subdirectories, use '\\' or '/' as the path separator. a single '\' will not parse well. Do not use quotes for 'img' even if there are spaces in the path.");
            writer.println("[Example1]");
            writer.println("name=Example Source Port");
            writer.println("desc=Describe the port and its features.");
            writer.println("type=port");
            writer.println("wadfolder=limit-removing,vanilla");
            writer.println("cmd=/path/to/run/port");
            writer.println("img=/optional/path/to/image/for/button.png");
            writer.println();
            writer.println("; A section describing a mod that relies on a source port.");
            writer.println("; Use the 'port=' field to define the source port(s) that can play this mod using the section name. Optional.");
            writer.println("; Use 'iwad=' to list the iwads the mod is compatible with, separated by commas if more than one, again using section names defining the iwads. Optional.");
            writer.println("[Example2]");
            writer.println("name=Mod Name");
            writer.println("desc=Mod description.");
            writer.println("type=mod");
            writer.println("port=Example1");
            writer.println("iwad=Ultimate,Doom2");
            writer.println("img=/optional/path/to/img.png");
            writer.println();
            writer.println("; A section describing a Total Conversion (TC) that relies on a source port.");
            writer.println("; Use the 'port=' field to define the source port(s) that can play this mod using the section name.");
            writer.println("; Use 'iwad=' to list the iwads the mod is compatible with, separated by commas if more than one, again using section names defining the iwads. Optional.");
            writer.println("; Use 'cmd=' if you want the mod to appear in the menu as a means of direct launching. Optional. Use quotes (\"...\") around the value if there are spaces in the path.");
            writer.println("; Use 'args=' if the mod has to run with a source port (defined in 'port=') and needs to pass extra parameters. Optional. Use quotes (\"...\") around individual argument values that have spaces in them.");
//            writer.println("; Use 'workingdir=' to point to the mod folder in the event you have to run with 'args=' that point to files in said working directory. Like for 'img=', if not an absolute path, the mods folder defined above will be used as the root of the given working directory. Do not use quotes for 'workingdir' even if there are spaces in the path.");
            writer.println("; Use 'skipwads=true' if you don't want to be offered to load a pwad.");
            writer.println("[Example2]");
            writer.println("name=Mod Name");
            writer.println("desc=Mod description.");
            writer.println("type=tc");
            writer.println("port=Example1");
            writer.println("iwad=Ultimate,Doom2");
            writer.println("img=/optional/path/to/img.png");
            writer.println();
            writer.println("; Defines base iwad files required to play Doom. These files are to be stored in /<user home directory>/.launcherfx/iwad/. Do not use quotes for 'file' even if there are spaces in the path.");
            writer.println("[Ultimate]");
            writer.println("name=Ultimate Doom");
            writer.println("type=iwad");
            writer.println("file=doom.wad");
            writer.println();
            writer.println("[Doom2]");
            writer.println("name=Doom II");
            writer.println("type=iwad");
            writer.println("file=doom2.wad");
            writer.println();
            writer.println("; Helper lists for warp= values.");
            writer.println("; E1M1,E1M2,E1M3,E1M4,E1M5,E1M6,E1M7,E1M8,E1M9,E2M1,E2M2,E2M3,E2M4,E2M5,E2M6,E2M7,E2M8,E2M9,E3M1,E3M2,E3M3,E3M4,E3M5,E3M6,E3M7,E3M8,E3M9,E4M1,E4M2,E4M3,E4M4,E4M5,E4M6,E4M7,E4M8,E4M9");
            writer.println("; MAP01,MAP02,MAP03,MAP04,MAP05,MAP06,MAP07,MAP08,MAP09,MAP10,MAP11,MAP12,MAP13,MAP14,MAP15,MAP16,MAP17,MAP18,MAP19,MAP20,MAP21,MAP22,MAP23,MAP24,MAP25,MAP26,MAP27,MAP28,MAP29,MAP30,MAP31,MAP32");
            writer.flush();
        }
    }
}
