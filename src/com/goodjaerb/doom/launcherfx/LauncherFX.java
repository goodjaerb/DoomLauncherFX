/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 *
 * @author goodjaerb
 */
public class LauncherFX extends Application {
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String CONFIG_DIR = ".launcherfx";
    private static final String CONFIG_FILE = "launcherfx.ini";
    
    private static final String CONFIG_DIR_BOOMWADS = "boomwads";
    private static final String CONFIG_DIR_IMAGES = "images";
    private static final String CONFIG_DIR_IWAD = "iwad";
    private static final String CONFIG_DIR_MODS = "mods";
    private static final String CONFIG_DIR_VANILLAWADS = "vanillawads";
    private static final String CONFIG_DIR_DOOM = "doom";
    private static final String CONFIG_DIR_DOOM2 = "doom2";
    
    public static final String TYPE_PORT = "port";
    public static final String TYPE_TC = "tc";
    public static final String TYPE_MOD = "mod";
    public static final String TYPE_IWAD = "iwad";
    public static final String TYPE_PWAD = "pwad";
    
    public static final Ini INI_FILE = new Ini();
    
    private final String CONFIG_HOME;
    
    private TabPane tabPane;
    private Tab portsTab;
    private Tab iwadsTab;
    private Tab modsTab;
    private Tab pwadsTab;
    private Tab warpTab;
    
    private VBox portsBox;
    private VBox iwadsBox;
    private VBox modsBox;
    
    private ListView<PWadListItem> pwadListView;
    private ListView<WarpListItem> warpListView;
        
    private Button continueToWarpButton;
    private Button launchNowButton; // the button on the bottom of the window.
    private Button launchButton; // the button on the last tab.
    private Button cancelButton;
    
    private List<String> processCommand;
    private String selectedIwad;
    private String selectedPort;
    
    public LauncherFX() throws IOException {
        FileSystem fs = FileSystems.getDefault();
        Path configFile = fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_FILE);

        createConfigFile(configFile);

        INI_FILE.load(Files.newBufferedReader(configFile));
        String datadir = INI_FILE.get("LauncherFXDataDir", "launcher-data");

        if(datadir != null) {
            CONFIG_HOME = datadir;
            configFile = fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_FILE);
            createConfigFile(configFile);

            INI_FILE.clear();
            INI_FILE.load(Files.newBufferedReader(configFile));
        }
        else {
            CONFIG_HOME = USER_HOME;
        }

        //create the directory structure
        Path[] configDirs = {
            fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_BOOMWADS),
            fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_BOOMWADS, CONFIG_DIR_DOOM),
            fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_BOOMWADS, CONFIG_DIR_DOOM2),
            fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_IMAGES),
            fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_IWAD),
            fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_MODS),
            fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_VANILLAWADS),
            fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_VANILLAWADS, CONFIG_DIR_DOOM),
            fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_VANILLAWADS, CONFIG_DIR_DOOM2),
        };
        for(Path p : configDirs) {
            Files.createDirectories(p);
        }
    }
    
    private static void createConfigFile(Path configFile) throws IOException {
        //check that .launcherfx directory exists.
        if(!Files.exists(configFile.getParent())) {
            Files.createDirectory(configFile.getParent());
        }
        
        //check that launcherfx.ini file exists.
        if(!Files.exists(configFile)) {
            Files.createFile(configFile);

            //initialize with example data.
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(configFile, StandardOpenOption.WRITE))) {
                writer.println("; A directory '" + CONFIG_DIR + "' will be created in the user's home directory. Within this directory will be another series of directories to store various wads and mods as described below. Also will be this '" + CONFIG_FILE + "' files.");
                writer.println("; The directory structure will be as follows:");
                writer.println("; <user home>/" + CONFIG_DIR + "/");
                writer.println("; |-> " + CONFIG_DIR_BOOMWADS + "/\t\tUsed for wads requiring 'limit-removing' (often referred to as Boom compatible) source ports.");
                writer.println("; |    |-> " + CONFIG_DIR_DOOM + "/\t\tWads that require the iwad for Doom");
                writer.println("; |    |-> " + CONFIG_DIR_DOOM2 + "/\t\tWads that require the iwad for Doom 2");
                writer.println("; |-> " + CONFIG_DIR_IMAGES + "/");
                writer.println("; |-> " + CONFIG_DIR_IWAD + "/");
                writer.println("; |-> " + CONFIG_DIR_MODS + "/");
                writer.println("; |-> " + CONFIG_DIR_VANILLAWADS + "/\t\tUsed for wads that do not require 'limit-removing' source ports.");
                writer.println("; |    |-> " + CONFIG_DIR_DOOM + "/");
                writer.println("; |    |-> " + CONFIG_DIR_DOOM2 + "/");
                writer.println("; |-> " + CONFIG_FILE);
                writer.println();
                writer.println("; if you want to keep the above data structures in another location, uncomment the following two lines and give the absolute path to the location you want. Leave THIS ini here with this line in it, and copy the rest of your configuration below into the ini at your new location.");
                writer.println("; The application will see this line pointing to the other location, then open the ini that is over there.");
                writer.println("; [LauncherFXDataDir]");
                writer.println("; launcher-data=/path/to/launcher/data");
                writer.println();
                writer.println("; A section describing a Doom source port. The section name may be referenced from other options.");
                writer.println("; Use 'vanilla=' to denote whether a source port emulates the Vanilla Doom experience and should not be used with mods/wads that require 'limit-removing' source ports. Optional; default assumption is 'false'.");
                writer.println("; Each section must have a 'type=', defining each section as a 'port', 'mod', or 'iwad'");
                writer.println("; Use quotes (\"...\") around the value for cmd if there are spaces in the path.");
                writer.println("; If 'img=' is not an absolute path, the 'images' folder above will be checked for the image file. If defining an absolute path or path with subdirectories, use '\\' or '/' as the path separator. a single '\' will not parse well. Do not use quotes for 'img' even if there are spaces in the path.");
                writer.println("; 'sort=' can be used to create an order of the ports/mods in the user interface. Optional. Sort order is undefined if not specified or if sorts are not all unique.");
                writer.println("[Example1]");
                writer.println("name=Example Source Port");
                writer.println("desc=Describe the port and its features.");
                writer.println("type=port");
                writer.println("sort=1");
                writer.println("vanilla=true");
                writer.println("cmd=/path/to/run/port");
                writer.println("img=/optional/path/to/image/for/button.png");
                writer.println();
                writer.println("; A section describing a mod that relies on a source port.");
                writer.println("; Use the 'port=' field to define the source port(s) that can play this mod using the section name. Optional.");
                writer.println("; Use 'iwad=' to list the iwads the mod is compatible with, separated by commas if more than one, again using section names defining the iwads. Optional.");
                writer.println("; Use 'cmd=' if you want the mod to appear in the menu as a means of direct launching. Optional. Use quotes (\"...\") around the value if there are spaces in the path.");
                writer.println("; Use 'args=' if the mod has to run with a source port (defined in 'port=') and needs to pass extra parameters. Optional. If using this, can only list one port in 'port=' and that port's 'cmd' will be run with these args. Use quotes (\"...\") around individual argument values that have spaces in them.");
                writer.println("; If neither 'cmd' nor 'args' are defined, the mod will be listed in the Mods tab and apply itself to whatever port/iwad is selected.");
                writer.println("; Use 'workingdir=' to point to the mod folder in the event you have to run with 'args=' that point to files in said working directory. Like for 'img=', if not an absolute path, the mods folder defined above will be used as the root of the given working directory. Do not use quotes for 'workingdir' even if there are spaces in the path.");
                writer.println("; Use 'skipwads=true' if you don't want to be offered to load a pwad.");
                writer.println("[Example2]");
                writer.println("name=Mod Name");
                writer.println("desc=Mod description.");
                writer.println("type=mod");
                writer.println("sort=2");
                writer.println("port=Example1");
                writer.println("iwad=Ultimate,Doom2");
                writer.println("cmd=/optional/cmd/to/run/mod");
                writer.println("img=/optional/path/to/img.png");
                writer.println();
                writer.println("; Defines base iwad files required to play Doom. These files are to be stored in /<user home directory>/.launcherfx/iwad/. Do not use quotes for 'file' even if there are spaces in the path.");
                writer.println("[Ultimate]");
                writer.println("name=Ultimate Doom");
                writer.println("type=iwad");
                writer.println("wadfolder=doom");
                writer.println("file=doom.wad");
                writer.println();
                writer.println("[Doom2]");
                writer.println("name=Doom II");
                writer.println("type=iwad");
                writer.println("wadfolder=doom2");
                writer.println("file=doom2.wad");
                writer.flush();
            }
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws MalformedURLException {
        Set<Entry<String, Section>> sortedSections = new TreeSet<>((Entry<String, Section> left, Entry<String, Section> right) -> {
            Integer leftSort = left.getValue().get("sort", Integer.class);
            Integer rightSort = right.getValue().get("sort", Integer.class);
            
            if(leftSort == null) {
                return 1;
            }
            
            if(rightSort == null) {
                return -1;
            }
            
            if(leftSort.equals(rightSort)) {
                return 1;
            }
            
            return leftSort.compareTo(rightSort);
        });
        sortedSections.addAll(INI_FILE.entrySet());
        
        portsBox = new VBox();
        iwadsBox = new VBox();
        modsBox = new VBox();
        
        modsBox.getChildren().add(new LaunchItemPane(LaunchItemEventHandler.STANDARD_MOD_NAME, "Standard", "Run the selected Port/TC with no mods.", null, true, new LaunchItemEventHandler(LaunchItemEventHandler.STANDARD_MOD_NAME)));
        
        for(Entry<String, Section> iniEntry : sortedSections) {
            System.out.println("Section=" + iniEntry.getKey());
            String section = iniEntry.getKey();
            String type = INI_FILE.get(section, "type");
            
            if(type != null) {
                type = type.toLowerCase();
                switch(type) {
                    case TYPE_PORT:
                    case TYPE_TC:
                        portsBox.getChildren().add(new LaunchItemPane(
                                section,
                                INI_FILE.get(section, "name"),
                                INI_FILE.get(section, "desc"),
                                getImagePath(section),
                                false,
                                new LaunchItemEventHandler(section)));
                        break;
                    case TYPE_IWAD:
                        iwadsBox.getChildren().add(new LaunchItemPane(
                                section,
                                INI_FILE.get(section, "name"),
                                INI_FILE.get(section, "desc"),
                                getImagePath(section),
                                true,
                                new LaunchItemEventHandler(section)));
                        break;
                    case TYPE_MOD:
                        modsBox.getChildren().add(new LaunchItemPane(
                                section,
                                INI_FILE.get(section, "name"),
                                INI_FILE.get(section, "desc"),
                                getImagePath(section),
                                true,
                                new LaunchItemEventHandler(section)));
                        break;
                    default:
                        break;
                }
            }
        }
        
        EventHandler<ActionEvent> launchHandler = (event) -> {
            addArgsToProcess(INI_FILE.get(selectedPort, "args"));
            
            WarpListItem warpItem = warpListView.getSelectionModel().getSelectedItem();
            if(warpItem != null && warpItem != DO_NOT_WARP) {
                addArgsToProcess("-warp " + warpItem.arg);
            }
            File workingDir = new File(processCommand.get(0)).getParentFile();
            
            String workingDirPath = INI_FILE.get(selectedPort, "workingdir");
            if(workingDirPath != null) {
                workingDir = new File(convertWorkingDirPath(workingDirPath));
            }
            
            ProcessBuilder processBuilder = new ProcessBuilder(processCommand);
            processBuilder.directory(workingDir);
            
            System.out.println("command=" + processBuilder.command() + ", workingdir=" + processBuilder.directory());
            try {
                Process p = processBuilder.start();
                p.waitFor();
                
                doCancel();
            }
            catch (IOException | InterruptedException ex) {
                Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        
        launchNowButton = new Button("Launch Now!");
        launchNowButton.setDisable(true);
        launchNowButton.addEventHandler(ActionEvent.ACTION, launchHandler);
        
        cancelButton = new Button("Cancel");
        cancelButton.setDisable(true);
        cancelButton.addEventHandler(ActionEvent.ACTION, (event) -> {
            doCancel();
        });
        
        FlowPane buttonPane = new FlowPane(launchNowButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setPadding(new Insets(4));
        buttonPane.setHgap(8);
        
        pwadListView = new ListView<>();
        pwadListView.setMinWidth(200);
        pwadListView.setMinHeight(450);
        pwadListView.setDisable(true);
        
        continueToWarpButton = new Button("Continue >>>");
        continueToWarpButton.setMinSize(200, 200);
        continueToWarpButton.setDisable(true);
        continueToWarpButton.addEventHandler(ActionEvent.ACTION, (event) -> {
            PWadListItem chosenPwad = pwadListView.getSelectionModel().getSelectedItem();
            if(chosenPwad != NO_PWAD) {
                addArgsToProcess("-file " + chosenPwad.path);
            }
            chooseWarp();
        });
        
        FlowPane pwadPane = new FlowPane(Orientation.HORIZONTAL, pwadListView, continueToWarpButton);
        pwadPane.setAlignment(Pos.CENTER);
        pwadPane.setPadding(new Insets(8));
        pwadPane.setHgap(8);
        
        warpListView = new ListView<>();
        warpListView.setMinSize(200, 450);
        warpListView.setDisable(true);
        
        launchButton = new Button("Launch!");
        launchButton.setMinSize(200, 200);
        launchButton.setDisable(true);
        launchButton.addEventHandler(ActionEvent.ACTION, launchHandler);
        
        FlowPane warpPane = new FlowPane(Orientation.HORIZONTAL, warpListView, launchButton);
        warpPane.setAlignment(Pos.CENTER);
        warpPane.setPadding(new Insets(8));
        warpPane.setHgap(8);
        
        portsTab = new Tab("Ports & TC's", portsBox);
        portsTab.setClosable(false);
        iwadsTab = new Tab("IWADS", iwadsBox);
        iwadsTab.setClosable(false);
        modsTab = new Tab("Mods", modsBox);
        modsTab.setClosable(false);
        pwadsTab = new Tab("PWADS", new BorderPane(null, pwadPane, null, null, null));
        pwadsTab.setClosable(false);
        warpTab = new Tab("Warp", new BorderPane(null, warpPane, null, null, null));
        warpTab.setClosable(false);
        
        tabPane = new TabPane();
        tabPane.getTabs().add(portsTab);
        tabPane.getTabs().add(iwadsTab);
        tabPane.getTabs().add(modsTab);
        tabPane.getTabs().add(pwadsTab);
        tabPane.getTabs().add(warpTab);
        
        ScrollPane scrollPane = new ScrollPane(new VBox(tabPane));
        
        VBox root = new VBox(scrollPane, buttonPane);
        root.setMinSize(600, 550);
        Scene scene = new Scene(root, 600, 550);
        
        primaryStage.setTitle("LauncherFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    private String getImagePath(String section) {
        String pathStr = INI_FILE.get(section, "img");
        if(pathStr == null) {
            return null;
        }
        
        Path imgPath = Paths.get(pathStr);
        if(imgPath.isAbsolute()) {
            return imgPath.toString();
        }
        
        return Paths.get(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_IMAGES, imgPath.toString()).toString();
    }
    
    private String getIwadPath(String section) {
        String pathStr = INI_FILE.get(section, "file");
        if(pathStr == null) {
            return null;
        }
        
        Path iwadPath = Paths.get(pathStr);
        if(iwadPath.isAbsolute()) {
            return iwadPath.toString();
        }
        
        return Paths.get(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_IWAD, iwadPath.toString()).toString();
    }
    
    private String getModFilePath(String section) {
        String pathStr = INI_FILE.get(section, "file");
        if(pathStr == null) {
            return null;
        }
        
        Path modPath = Paths.get(pathStr);
        if(modPath.isAbsolute()) {
            return modPath.toString();
        }
        
        return Paths.get(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_MODS, modPath.toString()).toString();
    }
    
    private String convertWorkingDirPath(String pathStr) {
        assert pathStr != null;
        
        Path modPath = Paths.get(pathStr);
        if(modPath.isAbsolute()) {
            return modPath.toString();
        }
        
        return Paths.get(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_MODS, modPath.toString()).toString();
    }
    
    private void chooseIwad() {
        portsTab.setDisable(true);
        iwadsTab.setDisable(false);
        modsTab.setDisable(true);
        pwadsTab.setDisable(true);
        warpTab.setDisable(true);
        
        cancelButton.setDisable(false);
        tabPane.getSelectionModel().select(iwadsTab);
        setItemsDisable(iwadsBox, false);
        
        if(TYPE_TC.equals(INI_FILE.get(selectedPort, "type"))) {
            for(Node launchItem : iwadsBox.getChildren()) {
                if(isIwadCompatible(INI_FILE.get(selectedPort, "iwad"), ((LaunchItemPane)launchItem).sectionName)) {
                    ((LaunchItemPane)launchItem).setButtonDisable(false);
                }
                else {
                    ((LaunchItemPane)launchItem).setButtonDisable(true);
                }
            }
        }
    }
    
    private void chooseMod() {
        portsTab.setDisable(true);
        iwadsTab.setDisable(true);
        modsTab.setDisable(false);
        pwadsTab.setDisable(true);
        warpTab.setDisable(true);

        launchNowButton.setDisable(false);
        tabPane.getSelectionModel().select(modsTab);
        setItemsDisable(modsBox, false);
        
        for(Node launchItem : modsBox.getChildren()) {
            if(isModCompatible(INI_FILE.get(((LaunchItemPane)launchItem).sectionName))) {
                ((LaunchItemPane)launchItem).setButtonDisable(false);
            }
            else {
                ((LaunchItemPane)launchItem).setButtonDisable(true);
            }
        }
    }
    
    private void choosePwad() throws IOException {
        portsTab.setDisable(true);
        iwadsTab.setDisable(true);
        modsTab.setDisable(true);
        pwadsTab.setDisable(false);
        warpTab.setDisable(true);
        
        continueToWarpButton.setDisable(false);
        launchNowButton.setDisable(false);
        tabPane.getSelectionModel().select(pwadsTab);
        pwadListView.setDisable(false);
        
        loadPwadList();
    }
    
    private void chooseWarp() {
        portsTab.setDisable(true);
        iwadsTab.setDisable(true);
        modsTab.setDisable(true);
        pwadsTab.setDisable(true);
        warpTab.setDisable(false);
        
        launchButton.setDisable(false);
        launchNowButton.setDisable(false);
        tabPane.getSelectionModel().select(warpTab);
        warpListView.setDisable(false);
        
        loadWarpList();
    }
    
    private void loadWarpList() {
        String wadfolder = INI_FILE.get(selectedIwad, "wadfolder");
        switch (wadfolder) {
            case CONFIG_DIR_DOOM:
                warpListView.setItems(FXCollections.observableArrayList(DOOM_WARP_LIST));
                warpListView.getSelectionModel().selectFirst();
                break;
            case CONFIG_DIR_DOOM2:
                warpListView.setItems(FXCollections.observableArrayList(DOOM2_WARP_LIST));
                warpListView.getSelectionModel().selectFirst();
                break;
            default:
                break;
        }
    }
    
    private void loadPwadList() throws IOException {
        String skipWads = INI_FILE.get(selectedPort, "skipwads");
        if("true".equals(skipWads)) {
            chooseWarp();
        }
        else {
            String wadFolder = INI_FILE.get(selectedIwad, "wadfolder");
            
            SortedSet<PWadListItem> pwadList = new TreeSet<>();
            pwadList.add(NO_PWAD);
            
            FileSystem fs = FileSystems.getDefault();
            Path wadBasePath = fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_VANILLAWADS, wadFolder);
            if(Files.exists(wadBasePath)) {
                Files.list(wadBasePath).forEach((path) -> {
                    pwadList.add(new PWadListItem(path.getFileName().toString(), path.toString()));
                });
            }
            
            String vanilla = INI_FILE.get(selectedPort, "vanilla");
            if(vanilla == null || !"true".equals(vanilla)) {
                // if the port is not vanilla only then we can add boom wads too!
                wadBasePath = fs.getPath(CONFIG_HOME, CONFIG_DIR, CONFIG_DIR_BOOMWADS, wadFolder);
                if(Files.exists(wadBasePath)) {
                    Files.list(wadBasePath).forEach((path) -> {
                        pwadList.add(new PWadListItem(path.getFileName().toString(), path.toString()));
                    });
                }
            }
        
            pwadListView.setItems(FXCollections.observableArrayList(pwadList));
            pwadListView.getSelectionModel().select(NO_PWAD);
        }
    }
    
    private void setItemsDisable(VBox box, boolean b) {
        for(Node launchItem : box.getChildren()) {
            ((LaunchItemPane)launchItem).setButtonDisable(b);
        }
    }
    
    private void doCancel() {
        continueToWarpButton.setDisable(true);
        launchButton.setDisable(true);
        launchNowButton.setDisable(true);
        cancelButton.setDisable(true);
        setItemsDisable(iwadsBox, true);
        setItemsDisable(modsBox, true);
        
        pwadListView.setDisable(true);
        warpListView.setDisable(true);
        
        portsTab.setDisable(false);
        iwadsTab.setDisable(false);
        modsTab.setDisable(false);
        pwadsTab.setDisable(false);
        warpTab.setDisable(false);
        
        tabPane.getSelectionModel().select(portsTab);

        processCommand = null;
    }
    
    /**
     * Is the given IWAD defined to be compatible with the currently selected port/TC.
     * 
     * @param iwadSection
     * @return 
     */
    private boolean isIwadCompatible(String supportedIwadList, String iwadSectionName) {
        if(supportedIwadList == null) {
            //if there's no list, then presumably there's no limitation.
            return true;
        }
        
        String[] splitIwads = supportedIwadList.split(",");
        for(String iwad : splitIwads) {
            if(iwad.equals(iwadSectionName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Is the given Mod defined to be compatible with the currently selected port/TC and IWAD.
     * 
     * @param modSection
     * @return 
     */
    private boolean isModCompatible(Section modSection) {
        if(modSection == null) {
            // the standard mod (i hope).
            return true;
        }
        
        boolean isSelectedPortCompatible = false;
        String requiredPort = modSection.get("port");
        if(requiredPort == null) {
            isSelectedPortCompatible = true;
        }
        else {
            String[] splitPorts = requiredPort.split(",");
            for(String port : splitPorts) {
                if(port.equals(selectedPort)) {
                    isSelectedPortCompatible = true;
                    break;
                }
            }
        }

        boolean isSelectedIwadCompatible = false;
        String requiredIwad = modSection.get("iwad");
        if(requiredIwad == null) {
            isSelectedIwadCompatible = true;
        }
        else {
            String [] splitIwads = requiredIwad.split(",");

            for(String iwad : splitIwads) {
                if(iwad.equals(selectedIwad)) {
                    isSelectedIwadCompatible = true;
                    break;
                }
            }
        }
        
        return isSelectedIwadCompatible && isSelectedPortCompatible;
    }
    
    private void addArgsToProcess(String args) {
        if(processCommand != null) {
            if(args != null) {
                List<String> argsList = new ArrayList<>();

                String[] splitArgs = args.split(" ");
                String longArg = "";
                for(String arg : splitArgs) {
                    if(!longArg.isEmpty()) {
                        longArg += " " + arg;
                    }
                    if(arg.startsWith("\"")) {
                        longArg += arg;
                        if(!longArg.endsWith("\"")) {
                            continue;
                        }
                    }
                    if(longArg.endsWith("\"")) {
                        longArg = longArg.substring(1, longArg.length() - 1);
                        argsList.add(longArg);
                        longArg = "";
                        continue;
                    }
                    argsList.add(arg);
                }
                processCommand.addAll(argsList);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private class LaunchItemEventHandler implements EventHandler<ActionEvent> {
        public static final String STANDARD_MOD_NAME = "THE_SPECIAL_NAME_FOR_THE_STANDARD_MOD_BUTTON";
                
        private final String sectionName;
        
        public LaunchItemEventHandler(String sectionName) {
            this.sectionName = sectionName;
        }
        
        @Override
        public void handle(ActionEvent e) {
            Section mySection = INI_FILE.get(sectionName);
            String myType = "mod";
            if(mySection != null) {
                myType = mySection.get("type");
            }

            switch(myType) {
                case "port":
                    if(mySection != null) {
                        String myCmd = mySection.get("cmd");
                        if(myCmd != null) {
                            processCommand = new ArrayList<>();
                            addArgsToProcess(myCmd);
                            
                            selectedPort = sectionName;
                            chooseIwad();
                        }
                    }
                    break;
                case "tc":
                    if(mySection != null) {
                        String port = mySection.get("port");
                        String myCmd = INI_FILE.get(port, "cmd");

                        if(myCmd != null) {
                            processCommand = new ArrayList<>();
                            addArgsToProcess(myCmd);
                            
                            selectedPort = sectionName;
                        }
                        chooseIwad();
                    }
                    break;
                case "mod":
                    if(mySection != null && mySection.get("file") != null) {
                        addArgsToProcess("-file " + getModFilePath(sectionName));
                    }
                    
                    try {
                        choosePwad();
                    } catch (IOException ex) {
                        Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case "iwad":
                    addArgsToProcess("-iwad " + getIwadPath(sectionName));

                    selectedIwad = sectionName;
                    chooseMod();
                    break;
                default:
                    break;
            }
        }
    }
    
    private final PWadListItem NO_PWAD = new PWadListItem("No PWAD.", "NOPWADPATH");
    private class PWadListItem implements Comparable<PWadListItem> {
        
        public final String display;
        public final String path;
        
        public PWadListItem(String display, String path) {
            this.display = display;
            this.path = path;
        }
        
        @Override
        public int compareTo(PWadListItem other) {
            if(this == NO_PWAD) {
                return -1;
            }
            if(other == NO_PWAD) {
                return 1;
            }
            return this.display.compareToIgnoreCase(other.display);
        }
        
        @Override
        public String toString() {
            return display;
        }
    }
    
    private class WarpListItem implements Comparable<WarpListItem> {
        public final String display;
        public final String arg;
        
        public WarpListItem(String display, String arg) {
            this.display = display;
            this.arg = arg;
        }
        
        @Override
        public int compareTo(WarpListItem other) {
            if(this == DO_NOT_WARP) {
                return -1;
            }
            if(other == DO_NOT_WARP) {
                return 1;
            }
            return this.display.compareToIgnoreCase(other.display);
        }
        
        @Override
        public String toString() {
            return display;
        }
    }
    
    private final WarpListItem DO_NOT_WARP = new WarpListItem("Do not warp.", "NOWARP");
    private final List<WarpListItem> DOOM_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(DO_NOT_WARP,
                    new WarpListItem("E1M1", "\"1 1\""), 
                    new WarpListItem("E1M2", "\"1 2\""), 
                    new WarpListItem("E1M3", "\"1 3\""), 
                    new WarpListItem("E1M4", "\"1 4\""), 
                    new WarpListItem("E1M5", "\"1 5\""), 
                    new WarpListItem("E1M6", "\"1 6\""), 
                    new WarpListItem("E1M7", "\"1 7\""), 
                    new WarpListItem("E1M8", "\"1 8\""), 
                    new WarpListItem("E1M9", "\"1 9\""), 
                    
                    new WarpListItem("E2M1", "\"2 1\""), 
                    new WarpListItem("E2M2", "\"2 2\""), 
                    new WarpListItem("E2M3", "\"2 3\""), 
                    new WarpListItem("E2M4", "\"2 4\""), 
                    new WarpListItem("E2M5", "\"2 5\""), 
                    new WarpListItem("E2M6", "\"2 6\""), 
                    new WarpListItem("E2M7", "\"2 7\""), 
                    new WarpListItem("E2M8", "\"2 8\""), 
                    new WarpListItem("E2M9", "\"2 9\""), 
                    
                    new WarpListItem("E3M1", "\"3 1\""), 
                    new WarpListItem("E3M2", "\"3 2\""), 
                    new WarpListItem("E3M3", "\"3 3\""), 
                    new WarpListItem("E3M4", "\"3 4\""), 
                    new WarpListItem("E3M5", "\"3 5\""), 
                    new WarpListItem("E3M6", "\"3 6\""), 
                    new WarpListItem("E3M7", "\"3 7\""), 
                    new WarpListItem("E3M8", "\"3 8\""), 
                    new WarpListItem("E3M9", "\"3 9\""), 
                    
                    new WarpListItem("E4M1", "\"4 1\""), 
                    new WarpListItem("E4M2", "\"4 2\""), 
                    new WarpListItem("E4M3", "\"4 3\""), 
                    new WarpListItem("E4M4", "\"4 4\""), 
                    new WarpListItem("E4M5", "\"4 5\""), 
                    new WarpListItem("E4M6", "\"4 6\""), 
                    new WarpListItem("E4M7", "\"4 7\""), 
                    new WarpListItem("E4M8", "\"4 8\""), 
                    new WarpListItem("E4M9", "\"4 9\"")));
    
    private final List<WarpListItem> DOOM2_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(DO_NOT_WARP,
                    new WarpListItem("MAP01", "1"),
                    new WarpListItem("MAP02", "2"),
                    new WarpListItem("MAP03", "3"),
                    new WarpListItem("MAP04", "4"),
                    new WarpListItem("MAP05", "5"),
                    new WarpListItem("MAP06", "6"),
                    new WarpListItem("MAP07", "7"),
                    new WarpListItem("MAP08", "8"),
                    new WarpListItem("MAP09", "9"),
                    new WarpListItem("MAP10", "10"),
                    new WarpListItem("MAP11", "11"),
                    new WarpListItem("MAP12", "12"),
                    new WarpListItem("MAP13", "13"),
                    new WarpListItem("MAP14", "14"),
                    new WarpListItem("MAP15", "15"),
                    new WarpListItem("MAP16", "16"),
                    new WarpListItem("MAP17", "17"),
                    new WarpListItem("MAP18", "18"),
                    new WarpListItem("MAP19", "19"),
                    new WarpListItem("MAP20", "20"),
                    new WarpListItem("MAP21", "21"),
                    new WarpListItem("MAP22", "22"),
                    new WarpListItem("MAP23", "23"),
                    new WarpListItem("MAP24", "24"),
                    new WarpListItem("MAP25", "25"),
                    new WarpListItem("MAP26", "26"),
                    new WarpListItem("MAP27", "27"),
                    new WarpListItem("MAP28", "28"),
                    new WarpListItem("MAP29", "29"),
                    new WarpListItem("MAP30", "30"),
                    new WarpListItem("MAP31", "31"),
                    new WarpListItem("MAP32", "32")));
}
