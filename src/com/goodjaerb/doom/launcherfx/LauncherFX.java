/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/**
 *
 * @author skuri
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
    
    private static final String TYPE_PORT = "port";
    private static final String TYPE_MOD = "mod";
    private static final String TYPE_IWAD = "iwad";
    
    public static final Ini INI_FILE = new Ini();
    static {
        try {
            FileSystem fs = FileSystems.getDefault();
            Path configFile = fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_FILE);
            
            //check that .launcherfx directory exists.
            if(!Files.exists(configFile.getParent())) {
                Files.createDirectory(configFile.getParent());
            }
            
            //create the directory structure
            Path[] configDirs = {
                fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_DIR_BOOMWADS),
                fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_DIR_BOOMWADS, CONFIG_DIR_DOOM),
                fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_DIR_BOOMWADS, CONFIG_DIR_DOOM2),
                fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_DIR_IMAGES),
                fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_DIR_IWAD),
                fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_DIR_MODS),
                fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_DIR_VANILLAWADS),
                fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_DIR_VANILLAWADS, CONFIG_DIR_DOOM),
                fs.getPath(USER_HOME, CONFIG_DIR, CONFIG_DIR_VANILLAWADS, CONFIG_DIR_DOOM2),
            };
            for(Path p : configDirs) {
                Files.createDirectories(p);
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
                    writer.println("; A section describing a Doom source port. The section name may be referenced from other options.");
                    writer.println("; Use 'vanilla=' to denote whether a source port emulates the Vanilla Doom experience and should not be used with mods/wads that require 'limit-removing' source ports. Optional; default assumption is 'false'.");
                    writer.println("; If 'img=' is not an absolute path, the 'images' folder will be checked for the image file. If defining an absolute path or path with subdirectories, use '\\' or '/' as the path separator. a single '\' will not parse well.");
                    writer.println("[Example1]");
                    writer.println("name=Example Source Port");
                    writer.println("desc=Describe the port and its features.");
                    writer.println("type=port");
                    writer.println("vanilla=true");
                    writer.println("cmd=/path/to/run/port");
                    writer.println("img=/optional/path/to/image/for/button.png");
                    writer.println();
                    writer.println("; A section describing a mod that relies on a source port.");
                    writer.println("; Use the 'requires=' field to define the source port(s) that can play this mod using the section name. ");
                    writer.println("; Use 'iwad=' to list the iwads the mod is compatible with, separated by semi-colons if more than one, again using section names defining the iwads.");
                    writer.println("; Use 'cmd=' if you want the mod to appear in the menu as a means of direct launching. If not present, the mod will be presented as an option after choosing a compatible source port and iwad.");
                    writer.println("[Example2]");
                    writer.println("name=Mod Name");
                    writer.println("desc=Mod description.");
                    writer.println("type=mod");
                    writer.println("requires=Example1");
                    writer.println("iwad=Ultimate;Doom2");
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
            
            INI_FILE.load(Files.newBufferedReader(configFile));
        } catch (IOException ex) {
            Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws MalformedURLException {
//        Button btn = new Button();
//        btn.setText("Say 'Hello World'");
//        btn.setOnAction(new EventHandler<ActionEvent>() {
//            
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("Hello World!");
//            }
//        });
//        

        Set<Entry<String, Section>> sortedSections = new TreeSet<>((Entry<String, Section> left, Entry<String, Section> right) -> {
            Integer leftSort = left.getValue().get("sort", Integer.class);
            Integer rightSort = right.getValue().get("sort", Integer.class);
            
            if(leftSort == null || rightSort == null) {
                return 0;
            }
            return leftSort.compareTo(rightSort);
        });
        sortedSections.addAll(INI_FILE.entrySet());
        
        VBox vbox = new VBox();
        for(Entry<String, Section> iniEntry : sortedSections) {
            System.out.println("Section=" + iniEntry.getKey());
            String section = iniEntry.getKey();
            String type = INI_FILE.get(section, "type");
            
            if(type != null) {
                type = type.toLowerCase();
                if(TYPE_PORT.equals(type) || (TYPE_MOD.equals(type) && INI_FILE.get(section, "cmd") != null)) {
                    vbox.getChildren().add(new LaunchItemPane(
                            INI_FILE.get(section, "name"),
                            INI_FILE.get(section, "desc"),
                            INI_FILE.get(section, "cmd"),
                            getImagePath(section)));
                }
            }
        }
        
        ScrollPane root = new ScrollPane(vbox);
        root.setMinSize(600, 550);
        Scene scene = new Scene(root, 600, 550);
        
//        primaryStage.setTitle("Hello World!");
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
        
        return Paths.get(USER_HOME, CONFIG_DIR, "images", imgPath.toString()).toString();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
