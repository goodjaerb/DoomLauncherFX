/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx;

import com.goodjaerb.doom.launcherfx.scene.control.list.PWadListItem;
import com.goodjaerb.doom.launcherfx.scene.control.list.PWadListCell;
import com.goodjaerb.doom.launcherfx.scene.control.LaunchItemPane;
import com.goodjaerb.doom.launcherfx.scene.control.list.WarpListCell;
import com.goodjaerb.doom.launcherfx.scene.control.list.WarpListItem;
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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
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
    
    private static final String CONFIG_DIR_IMAGES = "images";
    private static final String CONFIG_DIR_IWAD = "iwad";
    private static final String CONFIG_DIR_MODS = "mods";
    private static final String CONFIG_DIR_WADS = "wads";
    private static final String CONFIG_DIR_BOOM = "boom";
    private static final String CONFIG_DIR_LIMIT_REMOVING = "limit-removing";
    private static final String CONFIG_DIR_VANILLA = "vanilla";
    private static final String CONFIG_DIR_DOOM = "doom";
    private static final String CONFIG_DIR_DOOM2 = "doom2";
    private static final String CONFIG_DIR_HERETIC = "heretic";
    
    public static final String TYPE_PORT = "port";
    public static final String TYPE_TC = "tc";
    public static final String TYPE_MOD = "mod";
    public static final String TYPE_IWAD = "iwad";
    
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
    private PWadListItem selectedPwad;
    
    public LauncherFX() throws IOException {
        FileSystem fs = FileSystems.getDefault();
        Path configFile = fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_FILE);

        createConfigFile(configFile);

        INI_FILE.load(Files.newBufferedReader(configFile));
        String datadir = INI_FILE.get("LauncherFXDataDir", "launcher-data");

        if(datadir != null) {
            CONFIG_HOME = datadir;
            configFile = fs.getPath(CONFIG_HOME, CONFIG_FILE);
            createConfigFile(configFile);

            INI_FILE.clear();
            INI_FILE.load(Files.newBufferedReader(configFile));
        }
        else {
            CONFIG_HOME = USER_HOME + File.separator + CONFIG_DIR;
        }

        //create the directory structure
        Path[] configDirs = {
            fs.getPath(CONFIG_HOME, CONFIG_DIR_IMAGES),
            fs.getPath(CONFIG_HOME, CONFIG_DIR_IWAD),
            fs.getPath(CONFIG_HOME, CONFIG_DIR_MODS),
            fs.getPath(CONFIG_HOME, CONFIG_DIR_WADS, CONFIG_DIR_BOOM, CONFIG_DIR_DOOM),
            fs.getPath(CONFIG_HOME, CONFIG_DIR_WADS, CONFIG_DIR_BOOM, CONFIG_DIR_DOOM2),
            fs.getPath(CONFIG_HOME, CONFIG_DIR_WADS, CONFIG_DIR_LIMIT_REMOVING, CONFIG_DIR_DOOM),
            fs.getPath(CONFIG_HOME, CONFIG_DIR_WADS, CONFIG_DIR_LIMIT_REMOVING, CONFIG_DIR_DOOM2),
            fs.getPath(CONFIG_HOME, CONFIG_DIR_WADS, CONFIG_DIR_VANILLA, CONFIG_DIR_DOOM),
            fs.getPath(CONFIG_HOME, CONFIG_DIR_WADS, CONFIG_DIR_VANILLA, CONFIG_DIR_DOOM2),
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
                writer.println("; +-> " + CONFIG_DIR_IMAGES + "/");
                writer.println("; +-> " + CONFIG_DIR_IWAD + "/");
                writer.println("; +-> " + CONFIG_DIR_MODS + "/");
                writer.println("; +-> " + CONFIG_DIR_WADS + "/");
                writer.println("; |    +-> " + CONFIG_DIR_BOOM + "/\t\tUsed for wads requiring Boom-compatible editing extensions source ports.");
                writer.println("; |         +-> " + CONFIG_DIR_DOOM + "/\t\tWads that require the iwad for Doom");
                writer.println("; |         +-> " + CONFIG_DIR_DOOM2 + "/\t\tWads that require the iwad for Doom 2");
                writer.println("; |    +-> " + CONFIG_DIR_LIMIT_REMOVING + "/\t\tUsed for wads requiring limit-removing source ports but no further extensions.");
                writer.println("; |         +-> " + CONFIG_DIR_DOOM + "/\t\tWads that require the iwad for Doom");
                writer.println("; |         +-> " + CONFIG_DIR_DOOM2 + "/\t\tWads that require the iwad for Doom 2");
                writer.println("; |    +-> " + CONFIG_DIR_VANILLA + "/\t\tUsed for wads that do not require 'limit-removing' source ports.");
                writer.println("; |         +-> " + CONFIG_DIR_DOOM + "/");
                writer.println("; |         +-> " + CONFIG_DIR_DOOM2 + "/");
                writer.println("; +-> " + CONFIG_FILE);
                writer.println();
                writer.println("; if you want to keep the above data structures in another location, uncomment the following two lines and give the absolute path to the location you want. Leave THIS ini here with this line in it, and copy the rest of your configuration below into the ini at your new location.");
                writer.println("; The application will see this line pointing to the other location, then open the ini that is over there.");
                writer.println("; [LauncherFXDataDir]");
                writer.println("; launcher-data=/path/to/launcher/data");
                writer.println();
                writer.println("; A section describing a Doom source port. The section name may be referenced from other options.");
                writer.println("; Use 'wadfolder=' in a port section to limit which wads folder to search for pwads. By default the application creates folders called 'boom', 'limit-removing', and 'vanilla'. If you need more you can create them and use the name in the ini. Optional. No value for wadfolder will assume all wads are legal.");
                writer.println("; Each section must have a 'type=', defining each section as a 'port', 'mod', or 'iwad'");
                writer.println("; Use quotes (\"...\") around the value for cmd if there are spaces in the path.");
                writer.println("; If 'img=' is not an absolute path, the 'images' folder above will be checked for the image file. If defining an absolute path or path with subdirectories, use '\\' or '/' as the path separator. a single '\' will not parse well. Do not use quotes for 'img' even if there are spaces in the path.");
                writer.println("; 'sort=' can be used to create an order of the ports/mods in the user interface. Optional. Sort order is undefined if not specified or if sorts are not all unique.");
                writer.println("[Example1]");
                writer.println("name=Example Source Port");
                writer.println("desc=Describe the port and its features.");
                writer.println("type=port");
                writer.println("sort=1");
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
                writer.println("sort=2");
                writer.println("port=Example1");
                writer.println("iwad=Ultimate,Doom2");
                writer.println("img=/optional/path/to/img.png");
                writer.println();
                writer.println("; A section describing a Total Conversion (TC) that relies on a source port.");
                writer.println("; Use the 'port=' field to define the source port(s) that can play this mod using the section name.");
                writer.println("; Use 'iwad=' to list the iwads the mod is compatible with, separated by commas if more than one, again using section names defining the iwads. Optional.");
                writer.println("; Use 'cmd=' if you want the mod to appear in the menu as a means of direct launching. Optional. Use quotes (\"...\") around the value if there are spaces in the path.");
                writer.println("; Use 'args=' if the mod has to run with a source port (defined in 'port=') and needs to pass extra parameters. Optional. Use quotes (\"...\") around individual argument values that have spaces in them.");
                writer.println("; Use 'workingdir=' to point to the mod folder in the event you have to run with 'args=' that point to files in said working directory. Like for 'img=', if not an absolute path, the mods folder defined above will be used as the root of the given working directory. Do not use quotes for 'workingdir' even if there are spaces in the path.");
                writer.println("; Use 'skipwads=true' if you don't want to be offered to load a pwad.");
                writer.println("[Example2]");
                writer.println("name=Mod Name");
                writer.println("desc=Mod description.");
                writer.println("type=tc");
                writer.println("sort=3");
                writer.println("port=Example1");
                writer.println("iwad=Ultimate,Doom2");
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
                writer.println();
                writer.println("; Helper lists for warp= values.");
                writer.println("; E1M1,E1M2,E1M3,E1M4,E1M5,E1M6,E1M7,E1M8,E1M9,E2M1,E2M2,E2M3,E2M4,E2M5,E2M6,E2M7,E2M8,E2M9,E3M1,E3M2,E3M3,E3M4,E3M5,E3M6,E3M7,E3M8,E3M9,E4M1,E4M2,E4M3,E4M4,E4M5,E4M6,E4M7,E4M8,E4M9");
                writer.println("; MAP01,MAP02,MAP03,MAP04,MAP05,MAP06,MAP07,MAP08,MAP09,MAP10,MAP11,MAP12,MAP13,MAP14,MAP15,MAP16,MAP17,MAP18,MAP19,MAP20,MAP21,MAP22,MAP23,MAP24,MAP25,MAP26,MAP27,MAP28,MAP29,MAP30,MAP31,MAP32");
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
            
            List<PWadListItem> selectedPwadItems = pwadListView.getSelectionModel().getSelectedItems();
            if(selectedPwadItems.size() == 1) {
                PWadListItem pwadItem = pwadListView.getSelectionModel().getSelectedItem();
                if(pwadItem != PWadListItem.NO_PWAD) {
                    if(pwadItem.args != null) {
                        addArgsToProcess(pwadItem.args);
                    }
                    else {
                        if(pwadItem.type == PWadListItem.Type.WAD) {
                            addArgsToProcess("-file \"" + pwadItem.path.toString() + "\"");
                        }
                        else if(pwadItem.type == PWadListItem.Type.DEH) {
                            // idk if this is a thing that people would do, but it's possible so i'll handle it.
                            addArgsToProcess("-deh \"" + pwadItem.path.toString() + "\"");
                        }
                    }
                }
            }
            else {
                //if they chose multiple files, ignore item.args.
                String dehPaths = "";
                String wadPaths = "";
                for(PWadListItem item : selectedPwadItems) {
                    if(item.type == PWadListItem.Type.DEH) {
                        dehPaths += " \"" + item.path.toString() + "\"";
                    }
                    else if(item.type == PWadListItem.Type.WAD) {
                        wadPaths += " \"" + item.path.toString() + "\"";
                    }
                }
                if(!dehPaths.isEmpty()) {
                    addArgsToProcess("-deh" + dehPaths);
                }
                if(!wadPaths.isEmpty()) {
                    addArgsToProcess("-file" + wadPaths);
                }
            }
            
            WarpListItem warpItem = warpListView.getSelectionModel().getSelectedItem();
            if(warpItem != null && warpItem != WarpListItem.DO_NOT_WARP) {
                addArgsToProcess("-warp " + warpItem.arg);
                
                List<String> skillList;
                String wadfolder = INI_FILE.get(selectedIwad, "wadfolder");
                switch (wadfolder) {
                    case CONFIG_DIR_DOOM:
                    case CONFIG_DIR_DOOM2:
                        skillList = DOOM_SKILL_LIST;
                        break;
                    case CONFIG_DIR_HERETIC:
                        skillList = HERETIC_SKILL_LIST;
                        break;
                    default:
                        skillList = DOOM_SKILL_LIST;
                        break;
                }
                ChoiceDialog<String> dialog = new ChoiceDialog<>(skillList.get(2), skillList);
                dialog.setTitle("Select Skill Level");
                dialog.setHeaderText("Hey, I see you want to warp directly to a level.\nWould you like to set the difficulty too?");
                dialog.setContentText("Difficulty:");
                dialog.setResultConverter((buttonType) -> {
                    if(buttonType == ButtonType.OK) {
                        return dialog.getSelectedItem();
                    }
                    return null;
                });
                String skill = dialog.showAndWait().orElse(null);
                if(skill != null) {
                    addArgsToProcess("-skill " + (skillList.indexOf(skill) + 1));
                }
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
                new Alert(Alert.AlertType.ERROR, "An error occured accessing or running the program '" + processBuilder.command() + "'.", ButtonType.CLOSE).showAndWait();
                doCancel();
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
        pwadListView.setMinSize(350, 450);
        pwadListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pwadListView.setDisable(true);
        pwadListView.setCellFactory((ListView<PWadListItem> list) -> new PWadListCell());
        
        continueToWarpButton = new Button("Continue >>>");
        continueToWarpButton.setMinSize(200, 200);
        continueToWarpButton.setDisable(true);
        continueToWarpButton.addEventHandler(ActionEvent.ACTION, (event) -> {
            selectedPwad = pwadListView.getSelectionModel().getSelectedItem();
            chooseWarp();
        });
        
        FlowPane pwadPane = new FlowPane(Orientation.HORIZONTAL, pwadListView, continueToWarpButton);
        pwadPane.setAlignment(Pos.CENTER);
        pwadPane.setPadding(new Insets(8));
        pwadPane.setHgap(8);
        
        warpListView = new ListView<>();
        warpListView.setMinSize(200, 450);
        warpListView.setDisable(true);
        warpListView.setCellFactory((ListView<WarpListItem> list) -> new WarpListCell());
        
        launchButton = new Button("Launch!");
        launchButton.setMinSize(200, 200);
        launchButton.setDisable(true);
        launchButton.addEventHandler(ActionEvent.ACTION, launchHandler);
        
        FlowPane warpPane = new FlowPane(Orientation.HORIZONTAL, warpListView, launchButton);
        warpPane.setAlignment(Pos.CENTER);
        warpPane.setPadding(new Insets(8));
        warpPane.setHgap(8);
        
        portsTab = new Tab("Ports & TC's", new ScrollPane(portsBox));
        portsTab.setClosable(false);
        iwadsTab = new Tab("IWADS", new ScrollPane(iwadsBox));
        iwadsTab.setClosable(false);
        modsTab = new Tab("Mods", new ScrollPane(modsBox));
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
        
        
        VBox root = new VBox(tabPane, buttonPane);
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
        
        return Paths.get(CONFIG_HOME, CONFIG_DIR_IMAGES, imgPath.toString()).toString();
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
        
        return Paths.get(CONFIG_HOME, CONFIG_DIR_IWAD, iwadPath.toString()).toString();
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
        
        return Paths.get(CONFIG_HOME, CONFIG_DIR_MODS, modPath.toString()).toString();
    }
    
    private String convertWorkingDirPath(String pathStr) {
        assert pathStr != null;
        
        Path modPath = Paths.get(pathStr);
        if(modPath.isAbsolute()) {
            return modPath.toString();
        }
        
        return Paths.get(CONFIG_HOME, CONFIG_DIR_MODS, modPath.toString()).toString();
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
        
        for(Node launchItem : iwadsBox.getChildren()) {
            if(isIwadCompatible(INI_FILE.get(selectedPort, "iwad"), ((LaunchItemPane)launchItem).sectionName)) {
                ((LaunchItemPane)launchItem).setButtonDisable(false);
            }
            else {
                ((LaunchItemPane)launchItem).setButtonDisable(true);
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
                populateWarpList(DOOM_WARP_LIST);
                break;
            case CONFIG_DIR_DOOM2:
                populateWarpList(DOOM2_WARP_LIST);
                break;
            case CONFIG_DIR_HERETIC:
                populateWarpList(HERETIC_WARP_LIST);
                break;
            default:
                break;
        }
    }
    
    private void populateWarpList(List<WarpListItem> list) {
        ObservableList<WarpListItem> olist = FXCollections.observableArrayList(list);
        
        String warp;
        String tcWarp = INI_FILE.get(selectedPort, "warp");
        if(tcWarp == null) {
            warp = selectedPwad.warp;
        }
        else {
            warp = tcWarp;
        }
        
        WarpListItem selectThis = WarpListItem.DO_NOT_WARP;
        //have to do this no matter what to clear any potential prior highlights.
        for(WarpListItem item : olist) {
            if(warp.contains(item.display)) {
                item.highlight = true;
//                selectThis = item; //idk if i want to auto-select. plus would select last level in a multi-level wad...
            }
            else {
                item.highlight = false;
            }
        }
        warpListView.getItems().clear();
        warpListView.setItems(olist);
        warpListView.getSelectionModel().select(selectThis);
    }
    
    private void loadPwadList() throws IOException {
        String skipWads = INI_FILE.get(selectedPort, "skipwads");
        if("true".equals(skipWads)) {
            selectedPwad = PWadListItem.NO_PWAD;
            chooseWarp();
        }
        else {
            SortedSet<PWadListItem> pwadList = new TreeSet<>();
            pwadList.add(PWadListItem.NO_PWAD);
            
            String wadFolder = INI_FILE.get(selectedIwad, "wadfolder");
            String portCompatibleFolders = INI_FILE.get(selectedPort, "wadfolder");
            if(portCompatibleFolders == null) {
                // parse all folders.
                FileSystem fs = FileSystems.getDefault();
                Path wadBasePath = fs.getPath(CONFIG_HOME, CONFIG_DIR_WADS);
                Files.list(wadBasePath).forEach((path) -> {
                    try {
                        addFilesToPwadList(path.resolve(wadFolder), pwadList);
                    }
                    catch (IOException ex) {
                        Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
            else {
                String[] splitPortFolders = portCompatibleFolders.split(",");
                for(String wadDir : splitPortFolders) {
                    FileSystem fs = FileSystems.getDefault();
                    Path wadBasePath = fs.getPath(CONFIG_HOME, CONFIG_DIR_WADS, wadDir);
                    addFilesToPwadList(wadBasePath.resolve(wadFolder), pwadList);
                }
            }
        
            pwadListView.setItems(FXCollections.observableArrayList(pwadList));
            pwadListView.getSelectionModel().select(PWadListItem.NO_PWAD);
        }
    }
    
    private void addFilesToPwadList(Path wadPath, Set<PWadListItem> theWadSet) throws IOException {
        if(Files.exists(wadPath)) {
            Files.list(wadPath).forEach((file) -> {
                String filename = file.getFileName().toString().toLowerCase();
                if(Files.isRegularFile(file)) {
                    if(filename.endsWith(".txt")) {
                        theWadSet.add(new PWadListItem(PWadListItem.Type.TXT, file.getFileName().toString(), file));
                    }
                    else if(filename.endsWith(".deh")) {
                        String ignore = INI_FILE.get(file.getFileName().toString(), "ignore");
                        if(ignore == null || !"true".equals(ignore)) {
                            theWadSet.add(new PWadListItem(PWadListItem.Type.DEH, file.getFileName().toString(), file));
                        }
                    }
                    else if(filename.endsWith(".wad")) {
                        PWadListItem item = handlePwad(file);
                        if(item != null) {
                            theWadSet.add(item);
                        }
                    }
                }
            });
        }
    }
    
    private PWadListItem handlePwad(Path pwadPath) {
        String filename = pwadPath.getFileName().toString();

        Section pwadSection = INI_FILE.get(filename);
        if(pwadSection != null) {
            String ignore = pwadSection.get("ignore");
            if(ignore != null && "true".equals(ignore)) {
                return null;
            }
            else {
                String name = pwadSection.get("name") + " (" + filename + ")";
                String author = pwadSection.get("author");
                if(author != null) {
                    name += " by " + author;
                }

                PWadListItem item = new PWadListItem(PWadListItem.Type.WAD, name, pwadPath);

                String warp = pwadSection.get("warp");
                if(warp != null) {
                    item.warp = warp;
                }

                String args = pwadSection.get("args");
                if(args != null) {
                    item.args = args.replace("%WADPATH%", pwadPath.getParent().toString());
                }
                else {
                    //if args isn't defined, innocently check for a .deh file that matches the wad filename and create the args for it.
                    Path dehPath = pwadPath.resolveSibling(filename.replace(".wad", ".deh"));
                    if(Files.exists(dehPath)) {
                        item.args = "-deh \"" + dehPath.toString() + "\" -file \"" + pwadPath.toString() + "\"";
                    }
                }
                return item;
            }
        }
        else {
            PWadListItem item = new PWadListItem(PWadListItem.Type.WAD, pwadPath.getFileName().toString(), pwadPath);
            
            //if args isn't defined, innocently check for a .deh file that matches the wad filename and create the args for it.
            Path dehPath = pwadPath.resolveSibling(filename.replace(".wad", ".deh"));
            if(Files.exists(dehPath)) {
                item.args = "-deh \"" + dehPath.toString() + "\" -file \"" + pwadPath.toString() + "\"";
            }
            return item;
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
        pwadListView.getItems().clear();
        warpListView.setDisable(true);
        warpListView.getItems().clear();
        
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
        String supportedPorts = INI_FILE.get(iwadSectionName, "port");
        if(supportedPorts != null) {
            String[] splitPorts = supportedPorts.split(",");
            for(String port : splitPorts) {
                if(port.equals(selectedPort)) {
                    return true;
                }
            }
        }
        else {
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
                        String[] splitPort = port.split(",");
                        if(splitPort.length == 1) {
                            port = splitPort[0];
                        }
                        else {
                            ChoiceDialog<String> dialog = new ChoiceDialog<>(splitPort[0], splitPort);
                            dialog.setTitle("Select Port");
                            dialog.setHeaderText("Select the Source Port you would like to open this TC with.");
                            dialog.setContentText("Source Port:");
                            dialog.setResultConverter((buttonType) -> {
                                if(buttonType == ButtonType.OK) {
                                    return dialog.getSelectedItem();
                                }
                                return null;
                            });
                            port = dialog.showAndWait().orElse(null);
                        }
                        if(port == null) {
                            doCancel();
                        }
                        else {
                            String myCmd = INI_FILE.get(port, "cmd");

                            if(myCmd != null) {
                                processCommand = new ArrayList<>();
                                addArgsToProcess(myCmd);

                                selectedPort = sectionName;
                            }
                            chooseIwad();
                        }
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
    
    private final List<String> DOOM_SKILL_LIST = 
            Collections.unmodifiableList(Arrays.asList(
                    "I'm Too Young To Die",
                    "Hey, Not Too Rough",
                    "Hurt Me Plenty",
                    "Ultra-Violence",
                    "Nightmare!"));
    
    private final List<String> HERETIC_SKILL_LIST = 
            Collections.unmodifiableList(Arrays.asList(
                    "Thou Needeth a Wet-Nurse",
                    "Yellowbellies-R-Us",
                    "Bringest Them Oneth",
                    "Thou Art a Smite-Meister",
                    "Black Plague Possesses Thee"));
    
    private final List<WarpListItem> DOOM_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(WarpListItem.DO_NOT_WARP,
                    new WarpListItem("E1M1", "1 1"), 
                    new WarpListItem("E1M2", "1 2"), 
                    new WarpListItem("E1M3", "1 3"), 
                    new WarpListItem("E1M4", "1 4"), 
                    new WarpListItem("E1M5", "1 5"), 
                    new WarpListItem("E1M6", "1 6"), 
                    new WarpListItem("E1M7", "1 7"), 
                    new WarpListItem("E1M8", "1 8"), 
                    new WarpListItem("E1M9", "1 9"), 
                    
                    new WarpListItem("E2M1", "2 1"), 
                    new WarpListItem("E2M2", "2 2"), 
                    new WarpListItem("E2M3", "2 3"), 
                    new WarpListItem("E2M4", "2 4"), 
                    new WarpListItem("E2M5", "2 5"), 
                    new WarpListItem("E2M6", "2 6"), 
                    new WarpListItem("E2M7", "2 7"), 
                    new WarpListItem("E2M8", "2 8"), 
                    new WarpListItem("E2M9", "2 9"), 
                    
                    new WarpListItem("E3M1", "3 1"), 
                    new WarpListItem("E3M2", "3 2"), 
                    new WarpListItem("E3M3", "3 3"), 
                    new WarpListItem("E3M4", "3 4"), 
                    new WarpListItem("E3M5", "3 5"), 
                    new WarpListItem("E3M6", "3 6"), 
                    new WarpListItem("E3M7", "3 7"), 
                    new WarpListItem("E3M8", "3 8"), 
                    new WarpListItem("E3M9", "3 9"), 
                    
                    new WarpListItem("E4M1", "4 1"), 
                    new WarpListItem("E4M2", "4 2"), 
                    new WarpListItem("E4M3", "4 3"), 
                    new WarpListItem("E4M4", "4 4"), 
                    new WarpListItem("E4M5", "4 5"), 
                    new WarpListItem("E4M6", "4 6"), 
                    new WarpListItem("E4M7", "4 7"), 
                    new WarpListItem("E4M8", "4 8"), 
                    new WarpListItem("E4M9", "4 9")));
    
    private final List<WarpListItem> DOOM2_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(WarpListItem.DO_NOT_WARP,
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
    
    private final List<WarpListItem> HERETIC_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(WarpListItem.DO_NOT_WARP,
                    new WarpListItem("E1M1", "1 1"), 
                    new WarpListItem("E1M2", "1 2"), 
                    new WarpListItem("E1M3", "1 3"), 
                    new WarpListItem("E1M4", "1 4"), 
                    new WarpListItem("E1M5", "1 5"), 
                    new WarpListItem("E1M6", "1 6"), 
                    new WarpListItem("E1M7", "1 7"), 
                    new WarpListItem("E1M8", "1 8"), 
                    new WarpListItem("E1M9", "1 9"), 
                    
                    new WarpListItem("E2M1", "2 1"), 
                    new WarpListItem("E2M2", "2 2"), 
                    new WarpListItem("E2M3", "2 3"), 
                    new WarpListItem("E2M4", "2 4"), 
                    new WarpListItem("E2M5", "2 5"), 
                    new WarpListItem("E2M6", "2 6"), 
                    new WarpListItem("E2M7", "2 7"), 
                    new WarpListItem("E2M8", "2 8"), 
                    new WarpListItem("E2M9", "2 9"), 
                    
                    new WarpListItem("E3M1", "3 1"), 
                    new WarpListItem("E3M2", "3 2"), 
                    new WarpListItem("E3M3", "3 3"), 
                    new WarpListItem("E3M4", "3 4"), 
                    new WarpListItem("E3M5", "3 5"), 
                    new WarpListItem("E3M6", "3 6"), 
                    new WarpListItem("E3M7", "3 7"), 
                    new WarpListItem("E3M8", "3 8"), 
                    new WarpListItem("E3M9", "3 9"),
                    
                    new WarpListItem("E4M1", "4 1"), 
                    new WarpListItem("E4M2", "4 2"), 
                    new WarpListItem("E4M3", "4 3"), 
                    new WarpListItem("E4M4", "4 4"), 
                    new WarpListItem("E4M5", "4 5"), 
                    new WarpListItem("E4M6", "4 6"), 
                    new WarpListItem("E4M7", "4 7"), 
                    new WarpListItem("E4M8", "4 8"), 
                    new WarpListItem("E4M9", "4 9"),
                    
                    new WarpListItem("E5M1", "5 1"), 
                    new WarpListItem("E5M2", "5 2"), 
                    new WarpListItem("E5M3", "5 3"), 
                    new WarpListItem("E5M4", "5 4"), 
                    new WarpListItem("E5M5", "5 5"), 
                    new WarpListItem("E5M6", "5 6"), 
                    new WarpListItem("E5M7", "5 7"), 
                    new WarpListItem("E5M8", "5 8"), 
                    new WarpListItem("E5M9", "5 9"),
                    
                    new WarpListItem("E6M1", "6 1"), 
                    new WarpListItem("E6M2", "6 2"), 
                    new WarpListItem("E6M3", "6 3")));
    
    private final List<String> DOOM_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "7742089b4468a736cadb659a7deca3320fe6dcbd", // Doom Version 1.9
            "2c8212631b37f21ad06d18b5638c733a75e179ff", // Doom Version 1.8
            "2e89b86859acd9fc1e552f587b710751efcffa8e", // Doom Version 1.666
            "b5f86a559642a2b3bdfb8a75e91c8da97f057fe6", // Doom Version 1.2
            "df0040ccb29cc1622e74ceb3b7793a2304cca2c8")); // Doom Version 1.1
    
    private final List<String> ULTIMATE_DOOM_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "9b07b02ab3c275a6a7570c3f73cc20d63a0e3833", // Doom Version 1.9ud
            "e5ec79505530e151ff0e6f517f3ce1fd65969c46")); // Doom BFG Edition
    
    private final List<String> DOOM2_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "7ec7652fcfce8ddc6e801839291f0e28ef1d5ae7", // Doom 2 Version 1.9
            "a59548125f59f6aa1a41c22f615557d3dd2e85a9", // Doom 2 BFG Edition
            "d510c877031bbd5f3d198581a2c8651e09b9861f", // Doom 2 Version 1.8f
            "79c283b18e61b9a989cfd3e0f19a42ea98fda551", // Doom 2 Version 1.8
            "70192b8d5aba65c7e633a7c7bcfe7e3e90640c97", // Doom 2 Version 1.7a
            "78009057420b792eacff482021db6fe13b370dcc", // Doom 2 Version 1.7
            "6d559b7ceece4f5ad457415049711992370d520a", // Doom 2 Version 1.666
            "a4ce5128d57cb129fdd1441c12b58245be55c8ce")); // Doom 2 Version 1.666g
    
    private final List<String> HERETIC_EXP_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "f489d479371df32f6d280a0cb23b59a35ba2b833")); // Heretic Version 1.3 (Shadow of the Serpent Riders)
    
    private final List<String> HERETIC_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "A54C5D30629976A649119C5CE8BABAE2DDFB1A60", // Heretic Version 1.2
            "B5A6CC79CDE48D97905B44282E82C4C966A23A87")); // Heretic Version 1.0
    
    private final List<String> HEXEN_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "4b53832f0733c1e29e5f1de2428e5475e891af29")); // Hexen Version 1.1
            
    private final List<String> HEXEN_EXP_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "081f6a2024643b54ef4a436a85508539b6d20a1e", // Hexen: Deathkings of the Dark Citadel Version 1.1
            "c3065527d62b05a930fe75fe8181a64fb1982976")); // Hexen: Deathkings of the Dark Citadel Version 1.0
}
