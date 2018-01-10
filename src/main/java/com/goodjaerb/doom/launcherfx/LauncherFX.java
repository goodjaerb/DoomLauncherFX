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
import com.goodjaerb.doom.launcherfx.config.ui.ConfigurableItemDialog;
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
import java.util.HashSet;
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
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.ContextMenu;
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
    private Set<Path> removeFromWadList;
    
    private List<String> processCommand;
    private Game selectedGame = Game.UNKNOWN_GAME;
    private IniConfigurableItem selectedIwad = IniConfigurableItem.EMPTY_ITEM;
    private IniConfigurableItem selectedPort = IniConfigurableItem.EMPTY_ITEM;
    private IniConfigurableItem tcPortToUse = IniConfigurableItem.EMPTY_ITEM;
    private PWadListItem selectedPwad = PWadListItem.NO_PWAD;
    
    @Override
    public void start(Stage primaryStage) throws MalformedURLException, IOException {
        portsList = new ArrayList<>();
        iwadsList = new ArrayList<>();
        modsList = new ArrayList<>();
        selectedModsList = new ArrayList<>();
        removeFromWadList = new HashSet<>();
        
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
                String modArgs = mod.get(Field.ARGS);
                if(modArgs != null) {
                    Matcher m = Pattern.compile("\"(.*?)\"").matcher(modArgs);
                    while(m.find()) {
                        String group = m.group(1);
                        String absPath = resolvePathRelativeToConfig(group, Config.DIR_MODS);
                        modArgs = modArgs.replace(group, absPath);
                    }
                    addArgsToProcess(modArgs);
                }
                else if(mod.get(Field.FILE) != null) {
//                    addArgsToProcess("-file " + resolvePathRelativeToConfig(mod.get(Field.FILE), Config.DIR_MODS));
                    String modFiles = mod.get(Field.FILE);
                    Matcher m = Pattern.compile("\"(.*?)\"").matcher(modFiles);
                    while(m.find()) {
                        String group = m.group(1);
                        String absPath = resolvePathRelativeToConfig(group, Config.DIR_MODS);
                        modFiles = modFiles.replace(group, absPath);
                    }
                    addArgsToProcess("-file " + modFiles);
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
        
        MenuItem editPwadItem = new MenuItem("Edit");
        editPwadItem.setOnAction(new EditMenuConfigDialogEventHandler(Config.Type.PWAD, "Edit PWAD"));
        
        MenuItem ignorePwadItem = new MenuItem("Ignore");
        ignorePwadItem.setOnAction((event) -> {
            setPwadItemsToIgnore(pwadListView.getSelectionModel().getSelectedItems(), true);
        });
        
        MenuItem unignorePwadItem = new MenuItem("Unignore");
        unignorePwadItem.setOnAction((event) -> {
            setPwadItemsToIgnore(pwadListView.getSelectionModel().getSelectedItems(), false);
        });
        
        MenuItem deletePwadItem = new MenuItem("Delete Config");
        deletePwadItem.setOnAction((event) -> {
            List<PWadListItem> selectedItems = pwadListView.getSelectionModel().getSelectedItems();
            for(PWadListItem listItem : selectedItems) {
                if(listItem != PWadListItem.NO_PWAD) {
                    String section = listItem.path.getFileName().toString();
                    CONFIG.deleteSection(section);
                    try {
                        CONFIG.writeIni();
                        loadPwadList();
                    } catch (IOException ex) {
                        Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("Error writing ini.");
                    }
                }
            }
        });
        
        ContextMenu pwadContextMenu = new ContextMenu(editPwadItem, ignorePwadItem, unignorePwadItem, new SeparatorMenuItem(), deletePwadItem);
        pwadContextMenu.addEventHandler(WindowEvent.WINDOW_SHOWING, (event) -> {
            List<PWadListItem> selectedItems = pwadListView.getSelectionModel().getSelectedItems();
            if(selectedItems.size() > 1) {
                editPwadItem.setDisable(true);
            }
            else if(selectedItems.size() == 1) {
                PWadListItem listItem = selectedItems.get(0);
                if(listItem == PWadListItem.NO_PWAD || listItem.type != PWadListItem.Type.WAD) {
                    editPwadItem.setDisable(true);
                }
                else {
                    editPwadItem.setDisable(false);
                    IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(listItem.path.getFileName().toString());
                    ((EditMenuConfigDialogEventHandler)editPwadItem.getOnAction()).setItem(pwadItem);
                    ((EditMenuConfigDialogEventHandler)editPwadItem.getOnAction()).setPwadPath(listItem.path);
                    ((EditMenuConfigDialogEventHandler)editPwadItem.getOnAction()).setSectionName(listItem.path.getFileName().toString());
                    ((EditMenuConfigDialogEventHandler)editPwadItem.getOnAction()).setTitle("Edit PWAD: " + listItem.path.getFileName().toString());
                }
            }
            else {
                event.consume();
            }
        });
        
        pwadListView = new ListView<>();
        pwadListView.setMinSize(350, 450);
        pwadListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        pwadListView.setCellFactory((ListView<PWadListItem> list) -> new PWadListCell(pwadContextMenu));
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
        
        MenuItem editMenuItemAddPort = new MenuItem("Add Port");
        editMenuItemAddPort.addEventHandler(ActionEvent.ACTION, new EditMenuConfigDialogEventHandler(Config.Type.PORT, "Add New Port"));
        
        MenuItem editMenuItemAddTc = new MenuItem("Add Total Conversion");
        editMenuItemAddTc.addEventHandler(ActionEvent.ACTION, new EditMenuConfigDialogEventHandler(Config.Type.TC, "Add New TC"));
        
        MenuItem editMenuItemAddIwad = new MenuItem("Add IWAD");
        editMenuItemAddIwad.addEventHandler(ActionEvent.ACTION, new EditMenuConfigDialogEventHandler(Config.Type.IWAD, "Add New IWAD"));
        
        MenuItem editMenuItemAddMod = new MenuItem("Add Mod");
        editMenuItemAddMod.addEventHandler(ActionEvent.ACTION, new EditMenuConfigDialogEventHandler(Config.Type.MOD, "Add New Mod"));
        
        MenuItem menuSeparator = new SeparatorMenuItem();
        
        Menu fileMenu = new Menu("File", null, fileMenuItemReloadIni, fileMenuResetSelections, menuSeparator, fileMenuItemExit);
        Menu editMenu = new Menu("Edit", null, editMenuItemAddPort, editMenuItemAddTc, editMenuItemAddIwad, editMenuItemAddMod);
        MenuBar menuBar = new MenuBar(fileMenu, editMenu);
        
        VBox root = new VBox(menuBar, tabPane, buttonPane);
        root.setMinSize(600, 550);
        Scene scene = new Scene(root, 600, 550);
        
        primaryStage.setTitle(APP_NAME);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (event) -> {
            Platform.exit();
        });
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
            LaunchItemPane lip = new LaunchItemPane(ic);
            lip.addLaunchHandler(new LaunchItemEventHandler(ic));
            
            switch(type) {
                case PORT:
                    portsList.add(ic);
                    portsBox.getChildren().add(lip);//new LaunchItemPane(ic, new LaunchItemEventHandler(ic), new EditMenuConfigDialogEventHandler(ic, "Edit Port")));
                    lip.setContextMenu(createLaunchButtonContextMenu(ic, "Edit Port"));
                    ic.selectedProperty().addListener((observable, oldValue, newValue) -> {
                        if(!newValue) { //false. this port has been deselected.
                            // check mods/iwads with currently selected port in the event they were disabled
                            //because of this port but may now be available.
                            for(IniConfigurableItem mod : modsList) {
                                String modSupportedPorts = mod.get(Field.PORT);
                                System.out.println(modSupportedPorts);
                                System.out.println("'" + selectedPort.sectionName() + "'");
                                if(modSupportedPorts == null || modSupportedPorts.toLowerCase().contains(selectedPort.sectionName().toLowerCase())) {
                                    System.out.println("enable mod");
                                    mod.setEnabled(true);
                                }
                                else {
                                    System.out.println("disable mod");
                                    mod.setEnabled(false);
                                }
                            }
                            
                            String portSupportedIwads = selectedPort.get(Field.IWAD);
                            for(IniConfigurableItem iwad : iwadsList) {
    //                                iwad.setEnabled(true);

                                String iwadSupportedPorts = iwad.get(Field.PORT);
                                if(iwadSupportedPorts == null || iwadSupportedPorts.toLowerCase().contains(selectedPort.sectionName().toLowerCase())
                                        && (portSupportedIwads == null || portSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase()))) {
                                    iwad.setEnabled(true);
                                }
                                else {
                                    iwad.setEnabled(false);
    //                                    enabledIwadsList.remove(iwad);
                                }
                            }
                        }
                        else { // true. this port has been selected.
                            // deselect other ports
                            for(IniConfigurableItem port : portsList) {
                                if(port != ic) {
                                    port.setSelected(false);
                                }
                            }
                            
                            // enabled/disable mods/iwads according to this port's compatibility settings.
                            if(ic.getBoolean(Field.SKIPMODS)) {
                                for(IniConfigurableItem mod : modsList) {
                                    mod.setEnabled(false);
                                }
                            }
                            else {
    //                                IniConfigurableItem checkAgainstPort = selectedPort;
    //                                if(selectedPort.getType() == Config.Type.TC) {
    //                                    checkAgainstPort = tcPortToUse;
    //                                }

                                for(IniConfigurableItem mod : modsList) {
    //                                    mod.setEnabled(true);

                                    String modSupportedPorts = mod.get(Field.PORT);
                                    if(modSupportedPorts == null || modSupportedPorts.toLowerCase().contains(ic.sectionName().toLowerCase())) {
                                        mod.setEnabled(true);
                                    }
                                    else {
                                        mod.setEnabled(false);
                                    }
                                }
                            }

                            String portSupportedIwads = ic.get(Field.IWAD);
                            for(IniConfigurableItem iwad : iwadsList) {
    //                                iwad.setEnabled(true);

                                String iwadSupportedPorts = iwad.get(Field.PORT);
                                if(iwadSupportedPorts == null || iwadSupportedPorts.toLowerCase().contains(ic.sectionName().toLowerCase())
                                        && (portSupportedIwads == null || portSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase()))) {
                                    iwad.setEnabled(true);
                                }
                                else {
                                    iwad.setEnabled(false);
    //                                    enabledIwadsList.remove(iwad);
                                }
                            }
                            checkLaunchNowAvailable();
                        }
                    });
                        
                    ic.enabledProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        // if this port is enabled, reverse check that this port is compatible with currently selected mods and disable if it is not.
                        if(newValue) {
                            for(IniConfigurableItem mod : selectedModsList) {
                                String modSupportedPorts = mod.get(Field.PORT);
                                if(modSupportedPorts != null) {
//                                            for(IniConfigurableItem port : portsList) {
//                                                port.setEnabled(true);

                                    if(!modSupportedPorts.toLowerCase().contains(ic.sectionName().toLowerCase())) {
                                        ic.setEnabled(false);
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    break;
                case TC:
                    portsList.add(ic);
                    portsBox.getChildren().add(lip);//new LaunchItemPane(ic, new LaunchItemEventHandler(ic), new EditMenuConfigDialogEventHandler(ic, "Edit TC")));
                    lip.setContextMenu(createLaunchButtonContextMenu(ic, "Edit TC"));
                    break;
                case IWAD:
                    iwadsList.add(ic);
                    iwadsBox.getChildren().add(lip);//new LaunchItemPane(ic, new LaunchItemEventHandler(ic), new EditMenuConfigDialogEventHandler(ic, "Edit IWAD")));
                    lip.setContextMenu(createLaunchButtonContextMenu(ic, "Edit IWAD"));
                    break;
                case MOD:
                    modsList.add(ic);
                    modsBox.getChildren().add(lip);//new LaunchItemPane(ic, new LaunchItemEventHandler(ic), new EditMenuConfigDialogEventHandler(ic, "Edit Mod")));
                    lip.setContextMenu(createLaunchButtonContextMenu(ic, "Edit Mod"));
                    break;
                default:
                    break;
            }
        }
        reset();
    }
    
    private ContextMenu createLaunchButtonContextMenu(IniConfigurableItem ic, String title) {
        MenuItem editItem = new MenuItem("Edit");
        editItem.addEventHandler(ActionEvent.ACTION, new EditMenuConfigDialogEventHandler(ic, title));
        
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.addEventHandler(ActionEvent.ACTION, (event) -> {
            Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete '" + ic.get(Field.NAME) + "'?", ButtonType.YES, ButtonType.CANCEL);
            Optional<ButtonType> result = confirmDelete.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.YES) {
                CONFIG.deleteSection(ic.sectionName());
                try {
                    CONFIG.writeIni();
                    refreshFromIni();
                    System.out.println("Deleted INI Section '" + ic.sectionName());
                } catch (IOException ex) {
                    Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("An error occured writing to launcherfx.ini");
                }
            }
        });
        
        MenuItem moveUpItem = new MenuItem("Move Up");
        moveUpItem.addEventHandler(ActionEvent.ACTION, (event) -> {
            List<IniConfigurableItem> items;
            switch(ic.getType()) {
                case PORT:
                case TC:
                    items = CONFIG.getPortsAndTcs();
                    break;
                case IWAD:
                    items = CONFIG.getIwads();
                    break;
                case MOD:
                    items = CONFIG.getMods();
                    break;
                default:
                    System.out.println("Unknown Type. Cannot move up.");
                    return;
            }
            
            int index = items.indexOf(ic);
            if(index > 0) {
                String mySort = ic.get(Field.SORT);
                
                IniConfigurableItem other = items.get(index - 1);
                String otherSort = other.get(Field.SORT);
                
                CONFIG.update(ic.sectionName(), Field.SORT, otherSort);
                CONFIG.update(other.sectionName(), Field.SORT, mySort);
                
                try {
                    CONFIG.writeIni();
                    refreshFromIni();
                } catch (IOException ex) {
                    Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("An error occured writing to launcherfx.ini");
                }
            }
        });
        
        MenuItem moveDownItem = new MenuItem("Move Down");
        moveDownItem.addEventHandler(ActionEvent.ACTION, (event) -> {
            List<IniConfigurableItem> items;
            switch(ic.getType()) {
                case PORT:
                case TC:
                    items = CONFIG.getPortsAndTcs();
                    break;
                case IWAD:
                    items = CONFIG.getIwads();
                    break;
                case MOD:
                    items = CONFIG.getMods();
                    break;
                default:
                    System.out.println("Unknown Type. Cannot move down.");
                    return;
            }
            
            int index = items.indexOf(ic);
            if(index < items.size() - 1) {
                String mySort = ic.get(Field.SORT);
                
                IniConfigurableItem other = items.get(index + 1);
                String otherSort = other.get(Field.SORT);
                
                CONFIG.update(ic.sectionName(), Field.SORT, otherSort);
                CONFIG.update(other.sectionName(), Field.SORT, mySort);
                
                try {
                    CONFIG.writeIni();
                    refreshFromIni();
                } catch (IOException ex) {
                    Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("An error occured writing to launcherfx.ini");
                }
            }
        });
        
        return new ContextMenu(editItem, deleteItem, new SeparatorMenuItem(), moveUpItem, moveDownItem);
    }
    
    private void reset() {
        launchNowButton.setDisable(true);
        
        pwadListView.getItems().clear();
        warpListView.getItems().clear();
        
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        
        portsTab.setDisable(false);
        iwadsTab.setDisable(false);
        modsTab.setDisable(false);
        pwadsTab.setDisable(true);
        warpTab.setDisable(true);
        
        tabPane.getSelectionModel().select(selectedTab);

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
    
//    private void applyCompatibilities() {
//        
//    }
//    
//    private boolean checkIwadSupported(IniConfigurableItem iwad) {
//        String iwadSupportedPorts = iwad.get(Field.PORT);
//        String portSupportedIwads = selectedPort.get(Field.IWAD);
//        
//        
//    }
    
//    private void applyPortCompatibilites() {
//        List<IniConfigurableItem> enabledIwadsList = new ArrayList<>(iwadsList);
//        String portSupportedIwads = selectedPort.get(Field.IWAD);
//        for(IniConfigurableItem iwad : iwadsList) {
//            iwad.setEnabled(true);
//
//            String iwadSupportedPorts = iwad.get(Field.PORT);
//            if((iwadSupportedPorts != null && !iwadSupportedPorts.toLowerCase().contains(selectedPort.sectionName().toLowerCase()))
//                    || (portSupportedIwads != null && !portSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase()))) {
//                iwad.setEnabled(false);
//                enabledIwadsList.remove(iwad);
//            }
//        }
//        
//        // I don't want to do this like this because then it doesn't do all the other stuff
//        // that manually selecting the iwad does. and i don't want to fix it right now.
////        if(enabledIwadsList.size() == 1) {
////            System.out.println("Only one supported iwad available. Auto-selecting it.");
////            enabledIwadsList.get(0).setSelected(true);
////            selectedIwad = enabledIwadsList.get(0);
////        }
//
//        if(!selectedPort.getBoolean(Field.SKIPMODS)) {
//            IniConfigurableItem checkAgainstPort = selectedPort;
//            if(selectedPort.getType() == Config.Type.TC) {
//                checkAgainstPort = tcPortToUse;
//            }
//            
//            for(IniConfigurableItem mod : modsList) {
//                mod.setEnabled(true);
//
//                String modSupportedPorts = mod.get(Field.PORT);
//                if(modSupportedPorts != null && !modSupportedPorts.toLowerCase().contains(checkAgainstPort.sectionName().toLowerCase())) {
//                    mod.setEnabled(false);
//                }
//            }
//        }
//        checkLaunchNowAvailable();
//    }
//    
//    private void applyIwadCompatibilities() {
//        for(IniConfigurableItem port : portsList) {
//            port.setEnabled(true);
//            
//            String portSupportedIwads = port.get(Field.IWAD);
//            if(portSupportedIwads != null && !portSupportedIwads.toLowerCase().contains(selectedIwad.sectionName().toLowerCase())) {
//                port.setEnabled(false);
//            }
//        }
//
//        for(IniConfigurableItem mod : modsList) {
//            mod.setEnabled(true);
//
//            // i use to have GAME available for Mods but decided not to implement it in the config dialogs, but leaving this here anyway.
//            String modSupportedGames = mod.get(Field.GAME);
//            String modSupportedIwads = mod.get(Field.IWAD);
//            if((modSupportedIwads != null && !modSupportedIwads.toLowerCase().contains(selectedIwad.sectionName().toLowerCase()) || (modSupportedGames != null && !modSupportedGames.toUpperCase().contains(selectedGame.name())))) {
//                mod.setEnabled(false);
//            }
//        }
//        checkLaunchNowAvailable();
//    }
//    
//    private void applyModCompatiblities() {
//        for(IniConfigurableItem selectedMod : selectedModsList) {
//            String modSupportedPorts = selectedMod.get(Field.PORT);
//            if(modSupportedPorts != null) {
//                for(IniConfigurableItem port : portsList) {
//                    port.setEnabled(true);
//
//                    if(!modSupportedPorts.toLowerCase().contains(port.sectionName().toLowerCase())) {
//                        port.setEnabled(false);
//                    }
//                }
//            }
//            
//            String modSupportedIwads = selectedMod.get(Field.IWAD);
//            if(modSupportedIwads != null) {
//                for(IniConfigurableItem iwad : iwadsList) {
//                    iwad.setEnabled(true);
//
//                    if(!modSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase())) {
//                        iwad.setEnabled(false);
//                    }
//                }
//            }
//        }
//        checkLaunchNowAvailable();
//    }
    
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
            SortedSet<PWadListItem> pwadList = new TreeSet<>((o1, o2) -> {
                if (o1 == PWadListItem.NO_PWAD) {
                    return -1;
                }
                if (o2 == PWadListItem.NO_PWAD) {
                    return 1;
                }
                return o1.display.compareToIgnoreCase(o2.display);
            });
            
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
//            if(!showHiddenPwadItemsCheckBox.isSelected()) {
                for(Path toRemove : removeFromWadList) {
                    Iterator<PWadListItem> i = pwadList.iterator();
                    while(i.hasNext()) {
                        PWadListItem item = i.next();
                        if(item.path != null && item.path.equals(toRemove)) {
                            if(showHiddenPwadItemsCheckBox.isSelected()) {
                                if(!item.display.endsWith("(ignored)")) {
                                    item.display += " (auto-hidden)";
                                }
                            }
                            else {
                                i.remove();
                            }
                        }
                    }
                }
//            }
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
                        PWadListItem txtListItem = new PWadListItem(PWadListItem.Type.TXT, file.getFileName().toString(), file, null);
                        
                        IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(file.getFileName().toString());
                        if(pwadItem != null && "true".equals(pwadItem.get(Field.IGNORE))) {
                            removeFromWadList.add(file);
                            txtListItem.display += " (ignored)";
                        }
                        theWadSet.add(txtListItem);
//                        String ignore = (pwadItem == null) ? null : pwadItem.get(Field.IGNORE);
//                        if("true".equals(ignore)) {
//                            removeFromWadList.add()
//                        }
////                        if(ignore == null || (!"true".equals(ignore) || showHiddenPwadItemsCheckBox.isSelected())) {
//                            theWadSet.add(new PWadListItem(PWadListItem.Type.TXT, file.getFileName().toString(), file, null));
////                        }
                    }
                    else if(filename.endsWith(".deh")) {
                        PWadListItem dehListItem = new PWadListItem(PWadListItem.Type.DEH, file.getFileName().toString(), file, null);
                        
                        IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(file.getFileName().toString());
                        if(pwadItem != null && "true".equals(pwadItem.get(Field.IGNORE))) {
                            removeFromWadList.add(file);
                            dehListItem.display += " (ignored)";
                        }
                        theWadSet.add(dehListItem);
//                        IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(file.getFileName().toString());
//                        String ignore = (pwadItem == null) ? null : pwadItem.get(Field.IGNORE);
////                        if(ignore == null || (!"true".equals(ignore) || showHiddenPwadItemsCheckBox.isSelected())) {
//                            theWadSet.add(new PWadListItem(PWadListItem.Type.DEH, file.getFileName().toString(), file, null));
////                        }
                    }
                    else if(filename.endsWith(".wad") || filename.endsWith(".pk3")) {
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
        String fileName = pwadPath.getFileName().toString();

        IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(fileName);
        if(pwadItem != null) {
            String ignore = pwadItem.get(Field.IGNORE);
            if(ignore != null && ("true".equals(ignore) && !showHiddenPwadItemsCheckBox.isSelected())) {
                return null;
            }
            else {
                String name = pwadItem.get(Field.NAME);
                if(name == null || name.equals("")) {
                    name = fileName;
                }
                else {
                    name += " (" + fileName + ")";
                }
                
                if("true".equals(pwadItem.get(Field.IGNORE))) {
                    name += " (ignored)";
                }
                
                String txt = pwadItem.get(Field.TXT);
                if(txt != null) {
                    name += " (.txt)";
                    removeFromWadList.add(pwadPath.resolveSibling(txt));
                }
                else {
                    Path txtPath = pwadPath.resolveSibling(changeExtensionRetainingCase(fileName, "txt"));//fileName.replace((".wad"), ".txt"));
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
                        if(!pwadPath.equals(Paths.get(absPath))) {
                            removeFromWadList.add(Paths.get(absPath));
                        }
                    }
                    item.args = args;
//                    item.args = args.replace("%WADPATH%", pwadPath.getParent().toString());
                }
                else {
                    //if args isn't defined, innocently check for a .deh file that matches the wad filename and create the args for it.
                    Path dehPath = pwadPath.resolveSibling(changeExtensionRetainingCase(fileName, "deh"));//fileName.replace(".wad", ".deh"));
                    if(Files.exists(dehPath)) {
                        item.args = "-deh \"" + dehPath.toString() + "\" -file \"" + pwadPath.toString() + "\"";
                        removeFromWadList.add(dehPath); // do i want to do this?
                    }
                }
                return item;
            }
        }
        else {
            PWadListItem item = new PWadListItem(PWadListItem.Type.WAD, pwadPath.getFileName().toString(), pwadPath, null);
            
            //if args isn't defined, innocently check for a .deh file that matches the wad filename and create the args for it.
            Path dehPath = pwadPath.resolveSibling(changeExtensionRetainingCase(fileName, "deh"));//fileName.replace(".wad", ".deh"));
            if(Files.exists(dehPath)) {
                item.args = "-deh \"" + dehPath.toString() + "\" -file \"" + pwadPath.toString() + "\"";
                removeFromWadList.add(dehPath); // do i want to do this?
            }
            
            Path txtPath = pwadPath.resolveSibling(changeExtensionRetainingCase(fileName, "txt"));//fileName.replace((".wad"), ".txt"));
            if(Files.exists(txtPath)) {
                item.display += " (.txt)";
                item.txt = txtPath.getFileName().toString();
                removeFromWadList.add(txtPath);
            }
            return item;
        }
    }
    
    private void setPwadItemsToIgnore(List<PWadListItem> items, boolean ignore) {
        for(PWadListItem listItem : items) {
            if(listItem != PWadListItem.NO_PWAD) {
                String sectionName = listItem.path.getFileName().toString();
                IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(sectionName);
                if(ignore) {
                    if(pwadItem == null) {
                        CONFIG.addNewSection(sectionName);
                        CONFIG.update(sectionName, Field.TYPE, Config.Type.PWAD.iniValue());
                    }
                    CONFIG.update(sectionName, Field.IGNORE, "true");
                }
                else {
                    CONFIG.update(sectionName, Field.IGNORE, null);
                }
            }
        }
        
        try {
            CONFIG.writeIni();
            loadPwadList();
        } catch (IOException ex) {
            Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error writing ini.");
        }
    }
    
    /**
     * Takes the fileName and checks whether the extension oldExtension is upper or lower case and
     * replaces it with newExtension using the same case as the original.
     * 
     * Returns null if oldExtension isn't even the right extension!
     * 
     * @param fileName
     * @param oldExtension
     * @param newExtension
     * @return 
     */
    private String changeExtensionRetainingCase(String fileName, String newExtension) {
        String oldExtension = fileName.substring(fileName.length() - 3);
        Matcher m = Pattern.compile(".*\\.(" + oldExtension.toLowerCase() + ")$").matcher(fileName);
        if(m.matches()) {
            return fileName.replaceAll("\\." + oldExtension.toLowerCase() + "$", "." + newExtension.toLowerCase());
        }
        
        m = Pattern.compile(".*\\.(" + oldExtension.toUpperCase() + ")$").matcher(fileName);
        if(m.matches()) {
            return fileName.replaceAll("\\." + oldExtension.toUpperCase() + "$", "." + newExtension.toUpperCase());
        }
        
        return null;
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
                    if(!longArg.isEmpty() && !longArg.endsWith("\"")) {
                        continue;
                    }
                    argsList.add(arg);
                }
                processCommand.addAll(argsList);
                System.out.println("current cmd=" + processCommand);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private class EditMenuConfigDialogEventHandler implements EventHandler<ActionEvent> {

        private final Config.Type type;
        private String title;
        private IniConfigurableItem item;
        private String sectionName;
        private Path pwadPath;
        
        public EditMenuConfigDialogEventHandler(Config.Type type, String title) {
            this.type = type;
            this.item = null;
            this.title = title;
            this.sectionName = null;
            this.pwadPath = null;
        }
        
        public EditMenuConfigDialogEventHandler(IniConfigurableItem item, String title) {
            this.type = null;
            this.item = item;
            this.title = title;
            this.sectionName = null;
            this.pwadPath = null;
        }
        
        public void setItem(IniConfigurableItem item) {
            this.item = item;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public void setSectionName(String sectionName) {
            this.sectionName = sectionName;
        }
        
        public void setPwadPath(Path pwadPath) {
            this.pwadPath = pwadPath;
        }
        
        @Override
        public void handle(ActionEvent event) {
            ConfigurableItemDialog dialog;
            if(item == null) {
                dialog = new ConfigurableItemDialog(type, title, pwadPath);
                if(sectionName != null) {
                    dialog.setNewSectionName(sectionName);
                }
            }
            else {
                dialog = new ConfigurableItemDialog(item, title, pwadPath);
            }
            Optional<ButtonType> result = dialog.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    dialog.applyValues();
                    if(dialog.getType() == Config.Type.PWAD) {
                        loadPwadList();
                    }
                    else {
                        refreshFromIni();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("An error occured writing to launcherfx.ini");
                }
            }
        }
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
//                    selectedPort.setSelected(false);
                    if(selectedPort == ic) {
                        selectedPort = IniConfigurableItem.EMPTY_ITEM;
                        ic.setSelected(false);
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
//                    applyPortCompatibilites();
                    loadPwadList();
                    loadWarpList();
                    break;
                case TC:
                    selectedPort.setSelected(false);
                    if(selectedPort == ic) {
                        selectedPort = IniConfigurableItem.EMPTY_ITEM;
                    }
                    else {
//                        IniConfigurableItem portToUse;
                        tcPortToUse = IniConfigurableItem.EMPTY_ITEM;
                        
                        String portStr = ic.get(Field.PORT);
                        String[] splitPort = portStr.split(",");
                        if(splitPort.length == 1) {
                            tcPortToUse = CONFIG.getConfigurableByName(splitPort[0]);
                        }
                        else {
                            List<String> validChoices = new ArrayList<>();
                            for(String portSectionName : splitPort) {
                                if(CONFIG.getConfigurableByName(portSectionName) != null) {
                                    validChoices.add(portSectionName);
                                }
                            }
                            
                            if(validChoices.isEmpty()) {
                                tcPortToUse = IniConfigurableItem.EMPTY_ITEM;
//                                portToUse = null;
                            }
                            else if(validChoices.size() == 1) {
                                tcPortToUse = CONFIG.getConfigurableByName(validChoices.get(0));
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
                                    tcPortToUse = IniConfigurableItem.EMPTY_ITEM;
                                }
                                else {
                                    tcPortToUse = CONFIG.getConfigurableByName(portChosen);
                                }
                            }
                        }
                        if(tcPortToUse == IniConfigurableItem.EMPTY_ITEM) {
                            new Alert(Alert.AlertType.ERROR, "No valid source port defined for '" + ic.get(Field.NAME) + "'.", ButtonType.CLOSE).showAndWait();
                        }
                        else {
                            String tcCmd = tcPortToUse.get(Field.CMD);

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
//                    applyPortCompatibilites();
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
//                    applyModCompatiblities();
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
                            System.out.println("IWAD file not found.");
                            new Alert(Alert.AlertType.ERROR, "IWAD file not found.", ButtonType.CLOSE).showAndWait();
                            selectedGame = Game.UNKNOWN_GAME;
                        }
                        catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Error occured detecting game.");
                            new Alert(Alert.AlertType.ERROR, "Error occured detecting game.", ButtonType.CLOSE).showAndWait();
                            selectedGame = Game.UNKNOWN_GAME;
                        }

                        if(selectedGame == Game.UNKNOWN_GAME && ic.get(Field.GAME) != null) {
                            selectedGame = Game.valueOf(ic.get(Field.GAME).toUpperCase());
                        }
                        selectedIwad = ic;
                        selectedIwad.setSelected(true);
                    }
//                    applyIwadCompatibilities();
                    loadPwadList();
                    loadWarpList();
                    break;
                default:
                    break;
            }
        }
    }
}
