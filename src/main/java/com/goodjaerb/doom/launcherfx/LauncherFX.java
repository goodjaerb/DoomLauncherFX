/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx;

import com.goodjaerb.doom.launcherfx.data.Game;
import com.goodjaerb.doom.launcherfx.config.Config;
import com.goodjaerb.doom.launcherfx.config.Field;
import com.goodjaerb.doom.launcherfx.config.IniConfigurableItem;
import com.goodjaerb.doom.launcherfx.scene.control.LaunchButton;
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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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

/**
 *
 * @author goodjaerb
 */
public class LauncherFX extends Application {
    private static final Config CONFIG = Config.getInstance();
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
    
    private CheckBox showHiddenPwadItemsCheckBox;
    private ListView<PWadListItem> pwadListView;
    private ListView<WarpListItem> warpListView;
        
    private Button launchNowButton;
    private Button clearSelectionsButton;
    
    private List<IniConfigurableItem> portsList;
    private List<IniConfigurableItem> iwadsList;
    private List<IniConfigurableItem> modsList;
    private List<IniConfigurableItem> selectedModsList;
    private List<Path> removeFromWadList;
    
    private List<String> processCommand;
    private Game selectedGame = Game.UNKNOWN_GAME;
    private IniConfigurableItem selectedIwad = IniConfigurableItem.EMPTY_ITEM;
    private IniConfigurableItem selectedPort = IniConfigurableItem.EMPTY_ITEM;
    private PWadListItem selectedPwad = PWadListItem.NO_PWAD;
    
    @Override
    public void start(Stage primaryStage) throws MalformedURLException, IOException {
        portsList = new ArrayList<>();
        iwadsList = new ArrayList<>();
        modsList = new ArrayList<>();
        selectedModsList = new ArrayList<>();
        removeFromWadList = new ArrayList<>();
        
        portsBox = new VBox();
        iwadsBox = new VBox();
        modsBox = new VBox();
        
        EventHandler<ActionEvent> launchHandler = (event) -> {
            String iwadPath = resolvePathRelativeToConfig(selectedIwad.get(Field.FILE), Config.DIR_IWAD);
            addArgsToProcess("-iwad " + iwadPath);
            
            String portArgs = selectedPort.get(Field.ARGS);
            if(portArgs != null) {
                Matcher m = Pattern.compile("\"(.*?)\"").matcher(portArgs);
                while(m.find()) {
                    String group = m.group(1);
                    String absPath = resolvePathRelativeToConfig(group, Config.DIR_MODS);
                    portArgs = portArgs.replace(group, absPath);
                }
                addArgsToProcess(portArgs);
            }
            
            for(IniConfigurableItem mod : selectedModsList) {
                if(mod.get(Field.FILE) != null) {
                    addArgsToProcess("-file " + resolvePathRelativeToConfig(mod.get(Field.FILE), Config.DIR_MODS));
                }
            }
            
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
            
//            File workingDir = null;
//            String workingDirStr = getAbsolutePath(selectedPort.get(Field.WORKINGDIR), Config.DIR_MODS);
//            if(workingDirStr != null) {
//                workingDir = new File(workingDirStr);
//            }
//            if(workingDir == null) {
//                workingDir = new File(processCommand.get(0)).getParentFile();
//            }
            
            ProcessBuilder processBuilder = new ProcessBuilder(processCommand);
            processBuilder.directory(new File(processCommand.get(0)).getParentFile());
            
            System.out.println("command=" + processBuilder.command() + ", workingdir=" + processBuilder.directory());
            try {
                Process p = processBuilder.start();
                p.waitFor();
            }
            catch (IOException | InterruptedException ex) {
                Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                new Alert(Alert.AlertType.ERROR, "An error occured accessing or running the program '" + processBuilder.command() + "'.", ButtonType.CLOSE).showAndWait();
            }
            finally {
                processCommand = processCommand.subList(0, 1);
            }
        };
        
        launchNowButton = new Button("Launch Now!");
        launchNowButton.addEventHandler(ActionEvent.ACTION, launchHandler);
        
        clearSelectionsButton = new Button("Clear Selections");
        clearSelectionsButton.addEventHandler(ActionEvent.ACTION, (event) -> {
            reset();
        });
        
        FlowPane buttonPane = new FlowPane(launchNowButton, clearSelectionsButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setPadding(new Insets(4));
        buttonPane.setHgap(8);
        
        pwadListView = new ListView<>();
        pwadListView.setMinSize(350, 450);
        pwadListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pwadListView.setCellFactory((ListView<PWadListItem> list) -> new PWadListCell());
        pwadListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends PWadListItem> c) -> {
            List<PWadListItem> selectedItems = pwadListView.getSelectionModel().getSelectedItems();
            PWadListItem selectedWad = null;

            int wadCount = 0;
            for(PWadListItem selectedItem : selectedItems) {
                if(selectedItem.type == PWadListItem.Type.WAD) {
                    selectedWad = selectedItem;
                    wadCount++;
                }
            }

            if(wadCount == 1) {
                selectedPwad = selectedWad;
                loadWarpList();
            }
        });
        
        showHiddenPwadItemsCheckBox = new CheckBox("Show Hidden Items");
        showHiddenPwadItemsCheckBox.setSelected(false);
        showHiddenPwadItemsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            loadPwadList();
        });
        
        FlowPane pwadPane = new FlowPane(Orientation.HORIZONTAL, pwadListView, showHiddenPwadItemsCheckBox);
        pwadPane.setAlignment(Pos.CENTER);
        pwadPane.setPadding(new Insets(8));
        pwadPane.setHgap(8);
        
        warpListView = new ListView<>();
        warpListView.setMinSize(200, 450);
        warpListView.setCellFactory((ListView<WarpListItem> list) -> new WarpListCell());
        
        FlowPane warpPane = new FlowPane(Orientation.HORIZONTAL, warpListView);
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
        
        MenuItem fileMenuResetSelections = new MenuItem("Reset Selections");
        fileMenuResetSelections.addEventHandler(ActionEvent.ACTION, (event) -> {
            reset();
        });
        
        MenuItem fileMenuItemExit = new MenuItem("Exit");
        fileMenuItemExit.addEventHandler(ActionEvent.ACTION, (event) -> {
            Platform.exit();
        });
        
        MenuItem menuSeparator = new SeparatorMenuItem();
        
        Menu fileMenu = new Menu("File", null, fileMenuItemReloadIni, fileMenuResetSelections, menuSeparator, fileMenuItemExit);
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
    
    private void refreshFromIni() {
        portsList.clear();
        iwadsList.clear();
        modsList.clear();
        portsBox.getChildren().clear();
        iwadsBox.getChildren().clear();
        modsBox.getChildren().clear();
        
        for(IniConfigurableItem ic : CONFIG.getConfigurables()) {
            Config.Type type = ic.getType();
            
            switch(type) {
                case PORT:
                case TC:
                    portsList.add(ic);
                    portsBox.getChildren().add(new LaunchItemPane(ic, new LaunchItemEventHandler(ic)));
                    break;
                case IWAD:
                    iwadsList.add(ic);
                    iwadsBox.getChildren().add(new LaunchItemPane(ic, new LaunchItemEventHandler(ic)));
                    break;
                case MOD:
                    modsList.add(ic);
                    modsBox.getChildren().add(new LaunchItemPane(ic, new LaunchItemEventHandler(ic)));
                    break;
                default:
                    break;
            }
        }
        reset();
    }
    
    private void reset() {
        launchNowButton.setDisable(true);
        
        pwadListView.getItems().clear();
        warpListView.getItems().clear();
        
        portsTab.setDisable(false);
        iwadsTab.setDisable(false);
        modsTab.setDisable(false);
        pwadsTab.setDisable(true);
        warpTab.setDisable(true);
        
        tabPane.getSelectionModel().select(portsTab);

        processCommand = null;
        
        for(IniConfigurableItem ic : portsList) {
            ic.setEnabled(true);
            ic.setSelected(false);
        }
        for(IniConfigurableItem ic : iwadsList) {
            ic.setEnabled(true);
            ic.setSelected(false);
        }
        for(IniConfigurableItem ic : modsList) {
            ic.setEnabled(true);
            ic.setSelected(false);
        }
        
        selectedIwad.setSelected(false);
        selectedIwad = IniConfigurableItem.EMPTY_ITEM;

        selectedPort.setSelected(false);
        selectedPort = IniConfigurableItem.EMPTY_ITEM;

        selectedModsList.forEach((ic) -> {
            ic.setSelected(false);
        });
        selectedModsList.clear();
    }
    
    /**
     * If pathStr is a relative path, it will be resolved to an absolute path stemming from
     * the subdir configSubDir of configHome.
     * 
     * If pathStr is absolute, it is returned unchanged.
     * 
     * @param pathStr
     * @param configSubDir
     * @return 
     */
    public static String resolvePathRelativeToConfig(String pathStr, String configSubDir) {
        return resolveRelativePathToAbsolute(pathStr, Paths.get(CONFIG.getConfigHome(), configSubDir).toString());
//        if(pathStr == null || "".equals(pathStr)) {
//            return null;
//        }
//        
//        Path path = Paths.get(pathStr);
//        if(path.isAbsolute()) {
//            return path.toString();
//        }
//        return Paths.get(CONFIG.getConfigHome(), configSubDir, path.toString()).toString();
    }
    
    /**
     * If pathStr is a relative path, it will be resolved to an absolute path with the parent
     * of parentStr.
     * 
     * If pathStr is absolute, it is returned unchanged.
     * 
     * @param pathStr
     * @param parentStr
     * @return 
     */
    public static String resolveRelativePathToAbsolute(String pathStr, String parentStr) {
        if(pathStr == null || "".equals(pathStr)) {
            return null;
        }
        
        Path path = Paths.get(pathStr);
        if(path.isAbsolute()) {
            return path.toString();
        }
        return Paths.get(parentStr, path.toString()).toString();
    }
    
    private void applyPortCompatibilites() {
        List<IniConfigurableItem> enabledIwadsList = new ArrayList<>(iwadsList);
        String portSupportedIwads = selectedPort.get(Field.IWAD);
        for(IniConfigurableItem iwad : iwadsList) {
            iwad.setEnabled(true);

            String iwadSupportedPorts = iwad.get(Field.PORT);
            if((iwadSupportedPorts != null && !iwadSupportedPorts.toLowerCase().contains(selectedPort.sectionName().toLowerCase()))
                    || (portSupportedIwads != null && !portSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase()))) {
                iwad.setEnabled(false);
                enabledIwadsList.remove(iwad);
            }
        }
        
        if(enabledIwadsList.size() == 1) {
            System.out.println("Only one supported iwad available. Auto-selecting it.");
            enabledIwadsList.get(0).setSelected(true);
            selectedIwad = enabledIwadsList.get(0);
        }

        for(IniConfigurableItem mod : modsList) {
            mod.setEnabled(true);

            String modSupportedPorts = mod.get(Field.PORT);
            if(modSupportedPorts != null && !modSupportedPorts.toLowerCase().contains(selectedPort.sectionName().toLowerCase())) {
                mod.setEnabled(false);
            }
        }
        checkLaunchNowAvailable();
    }
    
    private void applyIwadCompatibilities() {
        for(IniConfigurableItem port : portsList) {
            port.setEnabled(true);
            
            String portSupportedIwads = port.get(Field.IWAD);
            if(portSupportedIwads != null && !portSupportedIwads.toLowerCase().contains(selectedIwad.sectionName().toLowerCase())) {
                port.setEnabled(false);
            }
        }

        for(IniConfigurableItem mod : modsList) {
            mod.setEnabled(true);

            String modSupportedGames = mod.get(Field.GAME);
            String modSupportedIwads = mod.get(Field.IWAD);
            if((modSupportedIwads != null && !modSupportedIwads.toLowerCase().contains(selectedIwad.sectionName().toLowerCase()) || (modSupportedGames != null && !modSupportedGames.toUpperCase().contains(selectedGame.name())))) {
                mod.setEnabled(false);
            }
        }
        checkLaunchNowAvailable();
    }
    
    private void applyModCompatiblities() {
        for(IniConfigurableItem selectedMod : selectedModsList) {
            String modSupportedPorts = selectedMod.get(Field.PORT);
            if(modSupportedPorts != null) {
                for(IniConfigurableItem port : portsList) {
                    port.setEnabled(true);

                    if(!modSupportedPorts.toLowerCase().contains(port.sectionName().toLowerCase())) {
                        port.setEnabled(false);
                    }
                }
            }
            
            String modSupportedIwads = selectedMod.get(Field.IWAD);
            if(modSupportedIwads != null) {
                for(IniConfigurableItem iwad : iwadsList) {
                    iwad.setEnabled(true);

                    if(!modSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase())) {
                        iwad.setEnabled(false);
                    }
                }
            }
        }
        checkLaunchNowAvailable();
    }
    
    private void checkLaunchNowAvailable() {
        if(selectedPort.isSelected() && selectedIwad.isSelected()) {
            launchNowButton.setDisable(false);
        }
        else {
            launchNowButton.setDisable(true);
        }
    }
    
    private void loadWarpList() {
        if(selectedGame == Game.UNKNOWN_GAME) {
            warpTab.setDisable(true);
        }
        else {
            warpTab.setDisable(false);
            populateWarpList(selectedGame.warpList);
        }
    }
    
    private void populateWarpList(List<WarpListItem> list) {
        ObservableList<WarpListItem> olist = FXCollections.observableArrayList(list);
        
        String warp;
        String tcWarp = selectedPort.get(Field.WARP);
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
        WarpListItem selectedWarp = warpListView.getSelectionModel().getSelectedItem();
        
        warpListView.getItems().clear();
        warpListView.setItems(olist);
        warpListView.getSelectionModel().select(selectThis);
        
        int previousSelectedWarpIndex = warpListView.getItems().indexOf(selectedWarp);
        if(previousSelectedWarpIndex != -1) {
            warpListView.getSelectionModel().selectIndices(previousSelectedWarpIndex);
        }
    }
    
    private void loadPwadList() {
        pwadsTab.setDisable(true);
        pwadListView.getItems().clear();
        removeFromWadList.clear();
        if(selectedPort != null && selectedPort != IniConfigurableItem.EMPTY_ITEM && selectedGame != Game.UNKNOWN_GAME) {
            SortedSet<PWadListItem> pwadList = new TreeSet<>();
            pwadList.add(PWadListItem.NO_PWAD);

            String skipWads = selectedPort.get(Field.SKIPWADS);
            if("true".equals(skipWads)) {
                selectedPwad = PWadListItem.NO_PWAD;
            }
            else {
                pwadsTab.setDisable(false);

                try {
                    String gameWadFolder = selectedGame.wadfolder;
                    String portCompatibleFolders = selectedPort.get(Field.WADFOLDER);
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
                } catch (IOException ex) {
                    Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(!showHiddenPwadItemsCheckBox.isSelected()) {
                for(Path toRemove : removeFromWadList) {
                    Iterator<PWadListItem> i = pwadList.iterator();
                    while(i.hasNext()) {
                        PWadListItem item = i.next();
                        if(item.path != null && item.path.equals(toRemove)) {
                            i.remove();
                        }
                    }
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
                        theWadSet.add(new PWadListItem(PWadListItem.Type.TXT, file.getFileName().toString(), file, null));
                    }
                    else if(filename.endsWith(".deh")) {
                        IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(file.getFileName().toString());
                        String ignore = (pwadItem == null) ? null : pwadItem.get(Field.IGNORE);
                        if(ignore == null || !"true".equals(ignore)) {
                            theWadSet.add(new PWadListItem(PWadListItem.Type.DEH, file.getFileName().toString(), file, null));
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

        IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(filename);
        if(pwadItem != null) {
            String ignore = pwadItem.get(Field.IGNORE);
            if(ignore != null && "true".equals(ignore)) {
                return null;
            }
            else {
                String name = pwadItem.get(Field.NAME);
                if(name == null) {
                    name = filename;
                }
                else {
                    name += " (" + filename + ")";
                }
                
                String txt = pwadItem.get(Field.TXT);
                if(txt != null) {
                    name += " (.txt)";
                    removeFromWadList.add(pwadPath.resolveSibling(txt));
                }
                else {
                    Path txtPath = pwadPath.resolveSibling(filename.replace((".wad"), ".txt"));
                    if(Files.exists(txtPath)) {
                        name += (" (.txt)");
                        txt = txtPath.getFileName().toString();
                        removeFromWadList.add(txtPath);
                    }
                }
                
                String author = pwadItem.get(Field.AUTHOR);
                if(author != null) {
                    name += " by " + author;
                }

                PWadListItem item = new PWadListItem(PWadListItem.Type.WAD, name, pwadPath, txt);

                String warp = pwadItem.get(Field.WARP);
                if(warp != null) {
                    item.warp = warp;
                }

                String args = pwadItem.get(Field.ARGS);
                if(args != null) {
                    Matcher m = Pattern.compile("\"(.*?)\"").matcher(args);
                    while(m.find()) {
                        String group = m.group(1);
                        String absPath = resolveRelativePathToAbsolute(group, pwadPath.getParent().toString());
                        args = args.replace(group, absPath);
                    }
                    item.args = args;
//                    item.args = args.replace("%WADPATH%", pwadPath.getParent().toString());
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
            PWadListItem item = new PWadListItem(PWadListItem.Type.WAD, pwadPath.getFileName().toString(), pwadPath, null);
            
            //if args isn't defined, innocently check for a .deh file that matches the wad filename and create the args for it.
            Path dehPath = pwadPath.resolveSibling(filename.replace(".wad", ".deh"));
            if(Files.exists(dehPath)) {
                item.args = "-deh \"" + dehPath.toString() + "\" -file \"" + pwadPath.toString() + "\"";
            }
            
            Path txtPath = pwadPath.resolveSibling(filename.replace((".wad"), ".txt"));
            if(Files.exists(txtPath)) {
                item.display += " (.txt)";
                item.txt = txtPath.getFileName().toString();
                removeFromWadList.add(txtPath);
            }
            return item;
        }
    }
    
    private void addArgsToProcess(String args) {
        if(processCommand != null) {
            if(args != null) {
                List<String> argsList = new ArrayList<>();

                String[] splitArgs = args.split(" ");
                String longArg = "";
                for(String arg : splitArgs) {
                    if(arg.isEmpty()) {
                        continue;
                    }
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
        private final IniConfigurableItem ic;
        
        public LaunchItemEventHandler(IniConfigurableItem ic) {
            this.ic = ic;
        }
        
        @Override
        public void handle(ActionEvent e) {
            LaunchButton myButton = (LaunchButton)e.getSource();
            switch(ic.getType()) {
                case PORT:
                    selectedPort.setSelected(false);
                    if(selectedPort == ic) {
                        selectedPort = IniConfigurableItem.EMPTY_ITEM;
                    }
                    else {
                        String portCmd = ic.get(Field.CMD);
                        if(portCmd != null) {
                            processCommand = new ArrayList<>();
                            addArgsToProcess(portCmd);

                            selectedPort = ic;
                            selectedPort.setSelected(true);
                        }
                    }
                    applyPortCompatibilites();
                    loadPwadList();
                    loadWarpList();
                    break;
                case TC:
                    selectedPort.setSelected(false);
                    if(selectedPort == ic) {
                        selectedPort = IniConfigurableItem.EMPTY_ITEM;
                    }
                    else {
                        IniConfigurableItem portToUse;
                        
                        String portStr = ic.get(Field.PORT);
                        String[] splitPort = portStr.split(",");
                        if(splitPort.length == 1) {
                            portToUse = CONFIG.getConfigurableByName(splitPort[0]);
                        }
                        else {
                            List<String> validChoices = new ArrayList<>();
                            for(String portSectionName : splitPort) {
                                if(CONFIG.getConfigurableByName(portSectionName) != null) {
                                    validChoices.add(portSectionName);
                                }
                            }
                            
                            if(validChoices.isEmpty()) {
                                portToUse = null;
                            }
                            else if(validChoices.size() == 1) {
                                portToUse = CONFIG.getConfigurableByName(validChoices.get(0));
                            }
                            else {
                                ChoiceDialog<String> dialog = new ChoiceDialog<>(validChoices.get(0), validChoices);
                                dialog.setTitle("Select Port");
                                dialog.setHeaderText("Select the Source Port you would like to open this TC with.");
                                dialog.setContentText("Source Port:");
                                dialog.setResultConverter((buttonType) -> {
                                    if(buttonType == ButtonType.OK) {
                                        return dialog.getSelectedItem();
                                    }
                                    return null;
                                });
                                String portChosen = dialog.showAndWait().orElse(null);
                                if(portChosen == null) {
                                    portToUse = IniConfigurableItem.EMPTY_ITEM;
                                }
                                else {
                                    portToUse = CONFIG.getConfigurableByName(portChosen);
                                }
                            }
                        }
                        if(portToUse == null) {
                            new Alert(Alert.AlertType.ERROR, "No valid source port defined for '" + ic.get(Field.NAME) + "'.", ButtonType.CLOSE).showAndWait();
                        }
                        else {
                            String tcCmd = portToUse.get(Field.CMD);

                            if(tcCmd == null) {
                                new Alert(Alert.AlertType.ERROR, "No command set for port.", ButtonType.CLOSE).showAndWait();
                                selectedPort = IniConfigurableItem.EMPTY_ITEM;
                            }
                            else {
                                processCommand = new ArrayList<>();
                                addArgsToProcess(tcCmd);

                                selectedPort = ic;
                                selectedPort.setSelected(true);
                            }
                        }
                    }
                    applyPortCompatibilites();
                    loadPwadList();
                    loadWarpList();
                    break;
                case MOD:
                    if(myButton.isChecked()) {
                        ic.setSelected(false);
                        selectedModsList.remove(ic);
                    }
                    else {
                        ic.setSelected(true);
                        selectedModsList.add(ic);
                    }
                    applyModCompatiblities();
                    break;
                case IWAD:
                    String iwadPath = resolvePathRelativeToConfig(ic.get(Field.FILE), Config.DIR_IWAD);

                    selectedIwad.setSelected(false);
                    if(selectedIwad == ic) {
                        selectedIwad = IniConfigurableItem.EMPTY_ITEM;
                    }
                    else {
                        try {
                            selectedGame = Game.getGameData(iwadPath);
                        } 
                        catch (IOException ex) {
                            Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                            new Alert(Alert.AlertType.ERROR, "IWAD file not found.", ButtonType.CLOSE).showAndWait();
                            selectedGame = Game.UNKNOWN_GAME;
                        } 
                        catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                            new Alert(Alert.AlertType.ERROR, "Error occured detecting game.", ButtonType.CLOSE).showAndWait();
                            selectedGame = Game.UNKNOWN_GAME;
                        }

                        if(selectedGame == Game.UNKNOWN_GAME && ic.get(Field.GAME) != null) {
                            selectedGame = Game.valueOf(ic.get(Field.GAME).toUpperCase());
                        }
                        selectedIwad = ic;
                        selectedIwad.setSelected(true);
                    }
                    applyIwadCompatibilities();
                    loadPwadList();
                    loadWarpList();
                    break;
                default:
                    break;
            }
        }
    }
}
