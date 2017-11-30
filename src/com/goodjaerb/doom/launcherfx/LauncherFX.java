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
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    
    private VBox portsBox;
    private VBox iwadsBox;
    private VBox modsBox;
        
    private Button cancelButton;
    
    private ProcessBuilder processBuilder;
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
                writer.println("; If 'img=' is not an absolute path, the 'images' folder above will be checked for the image file. If defining an absolute path or path with subdirectories, use '\\' or '/' as the path separator. a single '\' will not parse well.");
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
                writer.println("; Use 'cmd=' if you want the mod to appear in the menu as a means of direct launching. Optional.");
                writer.println("; Use 'args=' if the mod has to run with a source port (defined in 'port=') and needs to pass extra parameters. Optional. If using this, can only list one port in 'port=' and that port's 'cmd' will be run with these args.");
                writer.println("; If neither 'cmd' nor 'args' are defined, the mod will be listed in the Mods tab and apply itself to whatever port/iwad is selected.");
                writer.println("; Use 'workingdir=' to point to the mod folder in the event you have to run with 'args=' that point to files in said working directory. Like for 'img=', if not an absolute path, the mods folder defined above will be used as the root of the given working directory.");
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
                writer.println("; Defines base iwad files required to play Doom. These files are to be stored in /<user home directory>/.launcherfx/iwad/");
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
        
        cancelButton = new Button("Cancel");
        cancelButton.setDisable(true);
        cancelButton.addEventHandler(ActionEvent.ACTION, (event) -> {
            doCancel();
        });
        
        FlowPane buttonPane = new FlowPane(cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setPadding(new Insets(4));
        buttonPane.setHgap(8);
        
        portsTab = new Tab("Ports & TC's", portsBox);
        iwadsTab = new Tab("IWADS", iwadsBox);
        modsTab = new Tab("Mods", modsBox);
        pwadsTab = new Tab("PWADS", new Text("Nothing to see here."));
        
        tabPane = new TabPane();
        tabPane.getTabs().add(portsTab);
        tabPane.getTabs().add(iwadsTab);
        tabPane.getTabs().add(modsTab);
        tabPane.getTabs().add(pwadsTab);
        
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
    
    public void chooseIwad() {
        portsTab.setDisable(true);
        modsTab.setDisable(true);
        iwadsTab.setDisable(false);
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
    
    public void chooseMod() {
        portsTab.setDisable(true);
        iwadsTab.setDisable(true);
        modsTab.setDisable(false);
        cancelButton.setDisable(false);
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
    
    public void setItemsDisable(VBox box, boolean b) {
        for(Node launchItem : box.getChildren()) {
            ((LaunchItemPane)launchItem).setButtonDisable(b);
        }
    }
    
    private void doCancel() {
        cancelButton.setDisable(true);
        setItemsDisable(iwadsBox, true);
        setItemsDisable(modsBox, true);
        portsTab.setDisable(false);
        iwadsTab.setDisable(false);
        modsTab.setDisable(false);
        tabPane.getSelectionModel().select(portsTab);

        processBuilder = null;
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
        if(args != null) {
            List<String> argsList = new ArrayList<>();
            
            String[] splitArgs = args.split(" ");
//            String longArg = "";
            for(String arg : splitArgs) {
//                if(arg.startsWith("\"")) {
//                    longArg += arg;
//                    continue;
//                }
//                if(!longArg.isEmpty()) {
//                    longArg += " " + arg;
//                }
//                if(longArg.endsWith("\"")) {
//                    longArg = longArg.substring(1, longArg.length() - 1);
//                    argsList.add(longArg);
//                    longArg = "";
//                    continue;
//                }
                argsList.add(arg);
            }
            
            System.out.println(argsList);
            List<String> command = processBuilder.command();
            command.addAll(argsList);
            processBuilder = new ProcessBuilder(command);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public class LaunchItemEventHandler implements EventHandler<ActionEvent> {
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

            List<String> command;
            switch(myType) {
                case "port":
                    if(mySection != null) {
                        String myCmd = mySection.get("cmd");
                        if(myCmd != null) {
                            processBuilder = new ProcessBuilder(myCmd);
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
                            processBuilder = new ProcessBuilder(myCmd);
                            selectedPort = sectionName;

                            chooseIwad();
                        }
                    }
                    break;
                case "mod":
                    command = processBuilder.command();
                    if(mySection != null && mySection.get("file") != null) {
                        command.add("-file");
                        command.add(getModFilePath(sectionName));
                        processBuilder = new ProcessBuilder(command);
                    }
                    addArgsToProcess(INI_FILE.get(selectedPort, "args"));

                    File workingDir = new File(command.get(0)).getParentFile();

                    String workingDirPath = INI_FILE.get(selectedPort, "workingdir");
                    if(workingDirPath != null) {
                        workingDir = new File(convertWorkingDirPath(workingDirPath));
                    }
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
                    break;
                case "iwad":
                    if(processBuilder != null) {
                        command = processBuilder.command();
                        command.add("-iwad");
                        command.add(getIwadPath(sectionName));

                        processBuilder = new ProcessBuilder(command);
                        selectedIwad = sectionName;

                        chooseMod();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
