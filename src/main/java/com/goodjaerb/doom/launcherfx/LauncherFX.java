/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx;

import com.goodjaerb.doom.launcherfx.config.Config;
import com.goodjaerb.doom.launcherfx.config.Port;
import com.goodjaerb.doom.launcherfx.scene.control.list.PWadListItem;
import com.goodjaerb.doom.launcherfx.scene.control.list.PWadListCell;
import com.goodjaerb.doom.launcherfx.scene.control.LaunchItemPane;
import com.goodjaerb.doom.launcherfx.scene.control.list.WarpListCell;
import com.goodjaerb.doom.launcherfx.scene.control.list.WarpListItem;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.ini4j.Profile.Section;

/**
 *
 * @author goodjaerb
 */
public class LauncherFX extends Application {
    private final Config CONFIG = Config.getInstance();
    private final String APP_NAME = "DoomLauncherFX";
    
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
    private GameData selectedGame;
    private String selectedIwad;
    private Port selectedPort;
    private PWadListItem selectedPwad;
    
    @Override
    public void start(Stage primaryStage) throws MalformedURLException, IOException {
        portsBox = new VBox();
        iwadsBox = new VBox();
        modsBox = new VBox();
        
        EventHandler<ActionEvent> launchHandler = (event) -> {
            addArgsToProcess(selectedPort.get(Port.Field.ARGS));//CONFIG.get(selectedPort, "args"));
            
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
                
                List<String> skillList = selectedGame.skillList;
                if(!skillList.isEmpty()) {
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
            }
            
            File workingDir = null;
            String type = selectedPort.get(Port.Field.TYPE);//CONFIG.get(selectedPort, "type");
            if(Config.TYPE_MOD.equals(type) || Config.TYPE_TC.equals(type)) {
                String workingDirStr = getAbsolutePath(selectedPort.get(Port.Field.WORKINGDIR), Config.DIR_MODS);
                if(workingDirStr != null) {
                    workingDir = new File(workingDirStr);
                }
            }
            if(workingDir == null) {
                workingDir = new File(processCommand.get(0)).getParentFile();
            }
            
            ProcessBuilder processBuilder = new ProcessBuilder(processCommand);
            processBuilder.directory(workingDir);
            
            System.out.println("command=" + processBuilder.command() + ", workingdir=" + processBuilder.directory());
            try {
                Process p = processBuilder.start();
                p.waitFor();
                
                reset();
            }
            catch (IOException | InterruptedException ex) {
                Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                new Alert(Alert.AlertType.ERROR, "An error occured accessing or running the program '" + processBuilder.command() + "'.", ButtonType.CLOSE).showAndWait();
                reset();
            }
        };
        
        launchNowButton = new Button("Launch Now!");
        launchNowButton.addEventHandler(ActionEvent.ACTION, launchHandler);
        
        cancelButton = new Button("Cancel");
        cancelButton.addEventHandler(ActionEvent.ACTION, (event) -> {
            reset();
        });
        
        FlowPane buttonPane = new FlowPane(launchNowButton, cancelButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setPadding(new Insets(4));
        buttonPane.setHgap(8);
        
        pwadListView = new ListView<>();
        pwadListView.setMinSize(350, 450);
        pwadListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pwadListView.setCellFactory((ListView<PWadListItem> list) -> new PWadListCell());
        
        continueToWarpButton = new Button("Continue >>>");
        continueToWarpButton.setMinSize(200, 200);
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
        warpListView.setCellFactory((ListView<WarpListItem> list) -> new WarpListCell());
        
        launchButton = new Button("Launch!");
        launchButton.setMinSize(200, 200);
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
        
        MenuItem fileMenuItemReloadIni = new MenuItem("Reload launcherfx.ini");
        fileMenuItemReloadIni.addEventHandler(ActionEvent.ACTION, (event) -> {
            try {
                CONFIG.loadConfig();
                refreshFromIni();
            } catch (IOException ex) {
                Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                
                Alert exceptionAlert = new Alert(Alert.AlertType.ERROR, "There was a problem loading the configuration file.\nMake sure you have a " + Config.CONFIG_FILE + " located in " + Config.USER_HOME + File.separator + Config.CONFIG_DIR + ", even if you use a custom data location.\nRestart the application to recreate it and if necessary point it to your custom directory.", ButtonType.OK);
                exceptionAlert.showAndWait();
            }
        });
        MenuItem fileMenuItemExit = new MenuItem("Exit");
        fileMenuItemExit.addEventHandler(ActionEvent.ACTION, (event) -> {
            Platform.exit();
        });
        
        MenuItem menuSeparator = new SeparatorMenuItem();
        
        Menu fileMenu = new Menu("File", null, fileMenuItemReloadIni, menuSeparator, fileMenuItemExit);
        MenuBar menuBar = new MenuBar(fileMenu);
        
        VBox root = new VBox(menuBar, tabPane, buttonPane);
        root.setMinSize(600, 550);
        Scene scene = new Scene(root, 600, 550);
        
        primaryStage.setTitle(APP_NAME);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, (event) -> {
            if(CONFIG.isFirstRun()) {
                ButtonType homeButton = new ButtonType("Use Home Directory");
                ButtonType otherButton = new ButtonType("Choose Other Directory");

                Alert firstRunAlert = new Alert(Alert.AlertType.INFORMATION, "Would you like to initialize the configurtion in your User Home directory, or choose another location?", homeButton, otherButton, ButtonType.CANCEL);
                firstRunAlert.setTitle(APP_NAME);
                firstRunAlert.setHeaderText("Configuration Not Found.");

                Optional<ButtonType> result = firstRunAlert.showAndWait();
                if(!result.isPresent() || result.get() == ButtonType.CANCEL) {
                    System.out.println("Cancelled configuration alert. Exiting.");
                    Platform.exit();
                }
                else {
                    if(result.get() == homeButton) {
                        try {
                            CONFIG.initializeConfig();
                        } catch (IOException ex) {
                            Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else if(result.get() == otherButton) {
                        DirectoryChooser chooser = new DirectoryChooser();
                        chooser.setInitialDirectory(FileSystems.getDefault().getPath(System.getProperty("user.home")).toFile());

                        File dir = chooser.showDialog(primaryStage);
                        if(dir == null) {
                            System.out.println("No directory chosen. Exiting.");
                            Platform.exit();
                        }
                        else {
                            try {
                                CONFIG.initializeConfig(dir.toPath());
                            } catch (IOException ex) {
                                Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
            else {
                try {
                    CONFIG.loadConfig();
                } catch (IOException ex) {
                    Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            refreshFromIni();
        });
        
        primaryStage.show();
    }
    
    private void refreshPorts() {
        for(Port p : CONFIG.getPorts()) {
            portsBox.getChildren().add(new LaunchItemPane(p, new LaunchItemEventHandler2(p)));
        }
    }
    
    private void refreshFromIni() {
        reset();
        refreshPorts();
        
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
        sortedSections.addAll(CONFIG.entrySet());
        
        portsBox.getChildren().clear();
        iwadsBox.getChildren().clear();
        modsBox.getChildren().clear();
        modsBox.getChildren().add(new LaunchItemPane(LaunchItemEventHandler.STANDARD_MOD_NAME, "Standard", "Run the selected Port/TC with no mods.", null, true, new LaunchItemEventHandler(LaunchItemEventHandler.STANDARD_MOD_NAME)));
        
        for(Entry<String, Section> iniEntry : sortedSections) {
            System.out.println("Section=" + iniEntry.getKey());
            String section = iniEntry.getKey();
            String type = CONFIG.get(section, "type");
            
            if(type != null) {
                type = type.toLowerCase();
                switch(type) {
//                    case Config.TYPE_PORT:
//                    case Config.TYPE_TC:
//                        portsBox.getChildren().add(new LaunchItemPane(
//                                section,
//                                CONFIG.get(section, "name"),
//                                CONFIG.get(section, "desc"),
//                                getAbsolutePath(section, "img", Config.DIR_IMAGES),
//                                false,
//                                new LaunchItemEventHandler(section)));
//                        break;
                    case Config.TYPE_IWAD:
                        iwadsBox.getChildren().add(new LaunchItemPane(
                                section,
                                CONFIG.get(section, "name"),
                                CONFIG.get(section, "desc"),
                                getAbsolutePath(section, "img", Config.DIR_IMAGES),
                                true,
                                new LaunchItemEventHandler(section)));
                        break;
                    case Config.TYPE_MOD:
                        modsBox.getChildren().add(new LaunchItemPane(
                                section,
                                CONFIG.get(section, "name"),
                                CONFIG.get(section, "desc"),
                                getAbsolutePath(section, "img", Config.DIR_IMAGES),
                                true,
                                new LaunchItemEventHandler(section)));
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    private void reset() {
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
    
    private String getAbsolutePath(String pathStr, String configSubDir) {
        if(pathStr == null) {
            return null;
        }
        
        Path path = Paths.get(pathStr);
        if(path.isAbsolute()) {
            return path.toString();
        }
        return Paths.get(CONFIG.getConfigHome(), configSubDir, path.toString()).toString();
    }
    
    //to be replaced with the above version
    private String getAbsolutePath(String section, String key, String configSubDir) {
        String pathStr = CONFIG.get(section, key);
        if(pathStr == null) {
            return null;
        }
        
        Path path = Paths.get(pathStr);
        if(path.isAbsolute()) {
            return path.toString();
        }
        return Paths.get(CONFIG.getConfigHome(), configSubDir, path.toString()).toString();
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
            if(isIwadCompatible(selectedPort.get(Port.Field.IWAD), ((LaunchItemPane)launchItem).sectionName)) {
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
            if(isModCompatible(CONFIG.get(((LaunchItemPane)launchItem).sectionName))) {
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
        populateWarpList(selectedGame.warpList);
    }
    
    private void populateWarpList(List<WarpListItem> list) {
        ObservableList<WarpListItem> olist = FXCollections.observableArrayList(list);
        
        String warp;
        String tcWarp = selectedPort.get(Port.Field.WARP);//CONFIG.get(selectedPort, "warp");
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
        String skipWads = selectedPort.get(Port.Field.SKIPWADS);//CONFIG.get(selectedPort, "skipwads");
        if("true".equals(skipWads)) {
            selectedPwad = PWadListItem.NO_PWAD;
            chooseWarp();
        }
        else {
            SortedSet<PWadListItem> pwadList = new TreeSet<>();
            pwadList.add(PWadListItem.NO_PWAD);
            
            String gameWadFolder = selectedGame.wadfolder;//CONFIG.get(selectedIwad, "wadfolder");
            String portCompatibleFolders = selectedPort.get(Port.Field.WADFOLDER);//CONFIG.get(selectedPort, "wadfolder");
            if(portCompatibleFolders == null) {
                // parse all folders.
                FileSystem fs = FileSystems.getDefault();
                Path wadBasePath = fs.getPath(CONFIG.getConfigHome(), Config.DIR_WADS);
                Files.list(wadBasePath).forEach((path) -> {
                    if(Files.isDirectory(path)) {
                        try {
                            addFilesToPwadList(path.resolve(gameWadFolder), pwadList);
                        }
                        catch (IOException ex) {
                            Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
            else {
                String[] splitPortFolders = portCompatibleFolders.split(",");
                for(String wadDir : splitPortFolders) {
                    FileSystem fs = FileSystems.getDefault();
                    Path wadBasePath = fs.getPath(CONFIG.getConfigHome(), Config.DIR_WADS, wadDir);
                    addFilesToPwadList(wadBasePath.resolve(gameWadFolder), pwadList);
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
                        String ignore = CONFIG.get(file.getFileName().toString(), "ignore");
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

        Section pwadSection = CONFIG.get(filename);
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
    
    /**
     * Is the given IWAD defined to be compatible with the currently selected port/TC.
     * 
     * @param iwadSection
     * @return 
     */
    private boolean isIwadCompatible(String supportedIwadList, String iwadSectionName) {
        String supportedPorts = CONFIG.get(iwadSectionName, "port");
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
    
    private class LaunchItemEventHandler2 implements EventHandler<ActionEvent> {
        private final Port port;
        
        public LaunchItemEventHandler2(Port port) {
            this.port = port;
        }
        
        @Override
        public void handle(ActionEvent e) {
//            Section mySection = CONFIG.get(sectionName);
//            String myType = "mod";
//            if(mySection != null) {
//                myType = mySection.get("type");
//            }

            switch(port.get(Port.Field.TYPE)) {
                case Config.TYPE_PORT:
//                    if(mySection != null) {
                        String portCmd = port.get(Port.Field.CMD);
                        if(portCmd != null) {
                            processCommand = new ArrayList<>();
                            addArgsToProcess(portCmd);
                            
                            selectedPort = port;
                            chooseIwad();
                        }
//                    }
                    break;
                case Config.TYPE_TC:
//                    if(mySection != null) {
                        String portStr = port.get(Port.Field.PORT);
                        String[] splitPort = portStr.split(",");
                        if(splitPort.length == 1) {
                            portStr = splitPort[0];
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
                            portStr = dialog.showAndWait().orElse(null);
                        }
                        if(portStr == null) {
                            reset();
                        }
                        else {
                            String tcCmd = CONFIG.getPort(portStr).get(Port.Field.CMD);//CONFIG.get(portStr, "cmd");

                            if(tcCmd != null) {
                                processCommand = new ArrayList<>();
                                addArgsToProcess(tcCmd);

                                selectedPort = port;
                            }
                            chooseIwad();
                        }
//                    }
                    break;
//                case Config.TYPE_MOD:
//                    if(mySection != null && mySection.get("file") != null) {
//                        addArgsToProcess("-file " + getAbsolutePath(sectionName, "file", Config.DIR_MODS));
//                    }
//                    
//                    try {
//                        choosePwad();
//                    } catch (IOException ex) {
//                        Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    break;
//                case "iwad":
//                    String iwadPath = getAbsolutePath(sectionName, "file", Config.DIR_IWAD);
//                    addArgsToProcess("-iwad " + iwadPath);
//
//                    selectedIwad = sectionName;
//                    selectedGame = GameData.getGameData(iwadPath);
//                    chooseMod();
//                    break;
                default:
                    break;
            }
        }
    }
    
    private class LaunchItemEventHandler implements EventHandler<ActionEvent> {
        public static final String STANDARD_MOD_NAME = "THE_SPECIAL_NAME_FOR_THE_STANDARD_MOD_BUTTON";
                
        private final String sectionName;
        
        public LaunchItemEventHandler(String sectionName) {
            this.sectionName = sectionName;
        }
        
        @Override
        public void handle(ActionEvent e) {
            Section mySection = CONFIG.get(sectionName);
            String myType = "mod";
            if(mySection != null) {
                myType = mySection.get("type");
            }

            switch(myType) {
//                case "port":
//                    if(mySection != null) {
//                        String myCmd = mySection.get("cmd");
//                        if(myCmd != null) {
//                            processCommand = new ArrayList<>();
//                            addArgsToProcess(myCmd);
//                            
//                            selectedPort = sectionName;
//                            chooseIwad();
//                        }
//                    }
//                    break;
//                case "tc":
//                    if(mySection != null) {
//                        String port = mySection.get("port");
//                        String[] splitPort = port.split(",");
//                        if(splitPort.length == 1) {
//                            port = splitPort[0];
//                        }
//                        else {
//                            ChoiceDialog<String> dialog = new ChoiceDialog<>(splitPort[0], splitPort);
//                            dialog.setTitle("Select Port");
//                            dialog.setHeaderText("Select the Source Port you would like to open this TC with.");
//                            dialog.setContentText("Source Port:");
//                            dialog.setResultConverter((buttonType) -> {
//                                if(buttonType == ButtonType.OK) {
//                                    return dialog.getSelectedItem();
//                                }
//                                return null;
//                            });
//                            port = dialog.showAndWait().orElse(null);
//                        }
//                        if(port == null) {
//                            reset();
//                        }
//                        else {
//                            String myCmd = CONFIG.get(port, "cmd");
//
//                            if(myCmd != null) {
//                                processCommand = new ArrayList<>();
//                                addArgsToProcess(myCmd);
//
//                                selectedPort = sectionName;
//                            }
//                            chooseIwad();
//                        }
//                    }
//                    break;
                case "mod":
                    if(mySection != null && mySection.get("file") != null) {
                        addArgsToProcess("-file " + getAbsolutePath(sectionName, "file", Config.DIR_MODS));
                    }
                    
                    try {
                        choosePwad();
                    } catch (IOException ex) {
                        Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case "iwad":
                    String iwadPath = getAbsolutePath(sectionName, "file", Config.DIR_IWAD);
                    addArgsToProcess("-iwad " + iwadPath);

                    selectedIwad = sectionName;
                    selectedGame = GameData.getGameData(iwadPath);
                    chooseMod();
                    break;
                default:
                    break;
            }
        }
    }
}
