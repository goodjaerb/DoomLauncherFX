/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx;

import com.goodjaerb.doom.launcherfx.config.Config;
import com.goodjaerb.doom.launcherfx.config.Field;
import com.goodjaerb.doom.launcherfx.config.IniConfigurableItem;
import com.goodjaerb.doom.launcherfx.config.ui.ConfigurableItemDialog;
import com.goodjaerb.doom.launcherfx.data.Game;
import com.goodjaerb.doom.launcherfx.scene.control.LaunchButton;
import com.goodjaerb.doom.launcherfx.scene.control.LaunchItemPane;
import com.goodjaerb.doom.launcherfx.scene.control.list.PWadListCell;
import com.goodjaerb.doom.launcherfx.scene.control.list.PWadListItem;
import com.goodjaerb.doom.launcherfx.scene.control.list.WarpListCell;
import com.goodjaerb.doom.launcherfx.scene.control.list.WarpListItem;
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
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author goodjaerb
 */
public class LauncherFX extends Application {
    private static final Config CONFIG   = Config.getInstance();
    private final        String APP_NAME = "DoomLauncherFX";

    private VBox portsBox = new VBox();
    private VBox iwadsBox = new VBox();
    private VBox modsBox  = new VBox();

    private TabPane tabPane  = new TabPane();
    private Tab     portsTab = new Tab("Ports & TC's", new ScrollPane(portsBox));
    private Tab     iwadsTab = new Tab("IWADS", new ScrollPane(iwadsBox));
    private Tab     modsTab  = new Tab("Mods", new ScrollPane(modsBox));
    private Tab     pwadsTab = new Tab("PWADS");
    private Tab     warpTab  = new Tab("Warp");

    private CheckBox               showHiddenPwadItemsCheckBox = new CheckBox("Show Hidden Items");
    private ListView<PWadListItem> pwadListView                = new ListView<>();
    private ListView<WarpListItem> warpListView                = new ListView<>();

    private Button launchNowButton       = new Button("Launch Now!");
    private Button clearSelectionsButton = new Button("Clear Selections");

    private List<IniConfigurableItem> portsList         = new ArrayList<>();
    private List<IniConfigurableItem> iwadsList         = new ArrayList<>();
    private List<IniConfigurableItem> modsList          = new ArrayList<>();
    private List<IniConfigurableItem> selectedModsList  = new ArrayList<>();
    private Set<Path>                 removeFromWadList = new HashSet<>();

    private Game                selectedGame = Game.UNKNOWN_GAME;
    private IniConfigurableItem selectedIwad = IniConfigurableItem.EMPTY_ITEM;
    private IniConfigurableItem selectedPort = IniConfigurableItem.EMPTY_ITEM;
    private IniConfigurableItem tcPortToUse  = IniConfigurableItem.EMPTY_ITEM;
    private PWadListItem        selectedPwad = PWadListItem.NO_PWAD;

    private List<String> processCommand;
    private boolean      showHidden;
    private boolean      reset;

    @Override
    public void start(Stage primaryStage) {
        EventHandler<ActionEvent> launchHandler = (event) -> {
            String iwadPath = resolvePathRelativeToConfig(selectedIwad.get(Field.FILE), Config.DIR_IWAD);
            if(iwadPath != null) {
                addArgsToProcess("-iwad " + iwadPath);
            }

            String currentSaveDir = null;
            if(selectedIwad.getBoolean(Field.SAVEDIR)) {
                currentSaveDir = "saves/" + selectedIwad.sectionName();
            }

            String portArgs = selectedPort.get(Field.ARGS);
            if(portArgs != null) {
                Matcher m = Pattern.compile("\"(.*?)\"").matcher(portArgs);
                while(m.find()) {
                    String group = m.group(1);
                    String absPath = resolvePathRelativeToConfig(group, Config.DIR_MODS);
                    if(absPath == null) {
                        absPath = resolvePathRelativeToConfig(group, Config.DIR_IWAD);
                    }
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
                    String modFiles = mod.get(Field.FILE);
                    if(modFiles != null) {
                        Matcher m = Pattern.compile("\"(.*?)\"").matcher(modFiles);
                        while(m.find()) {
                            String group = m.group(1);
                            String absPath = resolvePathRelativeToConfig(group, Config.DIR_MODS);
                            modFiles = modFiles.replace(group, absPath);
                        }
                        addArgsToProcess("-file " + modFiles);
                    }
                }

                if(mod.getBoolean(Field.SAVEDIR)) {
                    currentSaveDir = "saves/" + mod.sectionName();
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
                StringBuilder dehPaths = new StringBuilder();
                StringBuilder wadPaths = new StringBuilder();
                for(PWadListItem item : selectedPwadItems) {
                    if(item.type == PWadListItem.Type.DEH) {
                        dehPaths.append(" \"").append(item.path.toString()).append("\"");
                    }
                    else if(item.type == PWadListItem.Type.WAD) {
                        wadPaths.append(" \"").append(item.path.toString()).append("\"");
                    }
                }
                if(dehPaths.length() > 0) {
                    addArgsToProcess("-deh" + dehPaths);
                }
                if(wadPaths.length() > 0) {
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
                    dialog.showAndWait().ifPresent(skill -> addArgsToProcess("-skill " + (skillList.indexOf(skill) + 1)));
                }
            }

            if(selectedPort.isType(Config.Type.TC) && selectedPort.getBoolean(Field.SAVEDIR)) {
                currentSaveDir = "saves/" + selectedPort.sectionName();
            }

            if(currentSaveDir != null && ((selectedPort.isType(Config.Type.TC) && selectedPort.getBoolean(Field.SAVEDIR) && tcPortToUse.getBoolean(Field.SAVEDIR)) || (selectedPort.isType(Config.Type.PORT) && selectedPort.getBoolean(Field.SAVEDIR)))) {
                if(selectedPort.isType(Config.Type.TC) && tcPortToUse.getSaveLoc() != null && !tcPortToUse.getSaveLoc().isEmpty()) {
                    currentSaveDir = tcPortToUse.getSaveLoc() + File.separator + currentSaveDir;
                }
                else if(selectedPort.isType(Config.Type.PORT) && selectedPort.getSaveLoc() != null && !selectedPort.getSaveLoc().isEmpty()) {
                    currentSaveDir = selectedPort.getSaveLoc() + File.separator + currentSaveDir;
                }

                addArgsToProcess("-savedir " + currentSaveDir);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(processCommand);
            processBuilder.directory(new File(processCommand.get(0)).getParentFile());

            LauncherFX.info("command=" + processBuilder.command() + ", workingdir=" + processBuilder.directory());
            try {
                Process p = processBuilder.start();
                p.waitFor();
            }
            catch(IOException | InterruptedException ex) {
                LauncherFX.error(ex);
                new Alert(Alert.AlertType.ERROR, "An error occured accessing or running the program '" + processBuilder.command() + "'.", ButtonType.CLOSE).showAndWait();
            }
            finally {
                processCommand = processCommand.subList(0, 1);
            }
        };

        launchNowButton.addEventHandler(ActionEvent.ACTION, launchHandler);

        clearSelectionsButton.addEventHandler(ActionEvent.ACTION, (event) -> reset());

        FlowPane buttonPane = new FlowPane(launchNowButton, clearSelectionsButton);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setPadding(new Insets(4));
        buttonPane.setHgap(8);

        MenuItem editPwadItem = new MenuItem("Edit");
        editPwadItem.setOnAction(new EditMenuConfigDialogEventHandler(Config.Type.PWAD, "Edit PWAD"));

        MenuItem ignorePwadItem = new MenuItem("Ignore");
        ignorePwadItem.setOnAction((event) -> setPwadItemsToIgnore(pwadListView.getSelectionModel().getSelectedItems(), true));

        MenuItem unignorePwadItem = new MenuItem("Unignore");
        unignorePwadItem.setOnAction((event) -> setPwadItemsToIgnore(pwadListView.getSelectionModel().getSelectedItems(), false));

        MenuItem deletePwadItem = new MenuItem("Delete Config");
        deletePwadItem.setOnAction((event) -> {
            List<PWadListItem> selectedItems = pwadListView.getSelectionModel().getSelectedItems();
            selectedItems.stream().filter((listItem) -> (listItem != PWadListItem.NO_PWAD)).forEachOrdered((listItem) -> CONFIG.deleteSection(listItem.path.getFileName().toString()));
            try {
                CONFIG.writeIni();
                loadPwadList();
            }
            catch(IOException ex) {
                LauncherFX.error(ex);
                LauncherFX.info("Error writing ini.");
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
                    ((EditMenuConfigDialogEventHandler) editPwadItem.getOnAction()).setItem(pwadItem);
                    ((EditMenuConfigDialogEventHandler) editPwadItem.getOnAction()).setPwadPath(listItem.path);
                    ((EditMenuConfigDialogEventHandler) editPwadItem.getOnAction()).setSectionName(listItem.path.getFileName().toString());
                    ((EditMenuConfigDialogEventHandler) editPwadItem.getOnAction()).setTitle("Edit PWAD: " + listItem.path.getFileName().toString());
                }
            }
            else {
                event.consume();
            }
        });

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

        showHiddenPwadItemsCheckBox.setSelected(false);
        showHiddenPwadItemsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> loadPwadList());

        FlowPane pwadPane = new FlowPane(Orientation.HORIZONTAL, pwadListView, showHiddenPwadItemsCheckBox);
        pwadPane.setAlignment(Pos.CENTER);
        pwadPane.setPadding(new Insets(8));
        pwadPane.setHgap(8);

        warpListView.setMinSize(200, 450);
        warpListView.setCellFactory((ListView<WarpListItem> list) -> new WarpListCell());

        FlowPane warpPane = new FlowPane(Orientation.HORIZONTAL, warpListView);
        warpPane.setAlignment(Pos.CENTER);
        warpPane.setPadding(new Insets(8));
        warpPane.setHgap(8);

        portsTab.setClosable(false);
        iwadsTab.setClosable(false);
        modsTab.setClosable(false);
        pwadsTab.setContent(new BorderPane(null, pwadPane, null, null, null));
        pwadsTab.setClosable(false);
        warpTab.setContent(new BorderPane(null, warpPane, null, null, null));
        warpTab.setClosable(false);

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
            }
            catch(IOException ex) {
                LauncherFX.error(ex);

                Alert exceptionAlert = new Alert(Alert.AlertType.ERROR, "There was a problem loading the configuration file.\nMake sure you have a " + Config.CONFIG_FILE + " located in " + Config.USER_HOME + File.separator + Config.CONFIG_DIR + ", even if you use a custom data location.\nRestart the application to recreate it and if necessary point it to your custom directory.", ButtonType.OK);
                exceptionAlert.showAndWait();
            }
        });

        MenuItem fileMenuResetSelections = new MenuItem("Reset Selections");
        fileMenuResetSelections.addEventHandler(ActionEvent.ACTION, (event) -> reset());

        CheckMenuItem fileMenuShowHiddenSections = new CheckMenuItem("Show Hidden");
        fileMenuShowHiddenSections.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showHidden = fileMenuShowHiddenSections.isSelected();
            try {
                CONFIG.writeIni();
                refreshFromIni();
            }
            catch(IOException ex) {
                LauncherFX.error(ex);
            }
        });

        MenuItem fileMenuItemExit = new MenuItem("Exit");
        fileMenuItemExit.addEventHandler(ActionEvent.ACTION, (event) -> Platform.exit());

        MenuItem editMenuItemAddPort = new MenuItem("Add Port");
        editMenuItemAddPort.addEventHandler(ActionEvent.ACTION, new EditMenuConfigDialogEventHandler(Config.Type.PORT, "Add New Port"));

        MenuItem editMenuItemAddTc = new MenuItem("Add Total Conversion");
        editMenuItemAddTc.addEventHandler(ActionEvent.ACTION, new EditMenuConfigDialogEventHandler(Config.Type.TC, "Add New TC"));

        MenuItem editMenuItemAddIwad = new MenuItem("Add IWAD");
        editMenuItemAddIwad.addEventHandler(ActionEvent.ACTION, new EditMenuConfigDialogEventHandler(Config.Type.IWAD, "Add New IWAD"));

        MenuItem editMenuItemAddMod = new MenuItem("Add Mod");
        editMenuItemAddMod.addEventHandler(ActionEvent.ACTION, new EditMenuConfigDialogEventHandler(Config.Type.MOD, "Add New Mod"));

        MenuItem menuSeparator = new SeparatorMenuItem();

        Menu fileMenu = new Menu("File", null, fileMenuItemReloadIni, fileMenuResetSelections, fileMenuShowHiddenSections, menuSeparator, fileMenuItemExit);
        Menu editMenu = new Menu("Edit", null, editMenuItemAddPort, editMenuItemAddTc, editMenuItemAddIwad, editMenuItemAddMod);
        MenuBar menuBar = new MenuBar(fileMenu, editMenu);

        VBox root = new VBox(menuBar, tabPane, buttonPane);
        root.setMinSize(600, 550);
        Scene scene = new Scene(root, 600, 550);

        primaryStage.setTitle(APP_NAME);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, (event) -> Platform.exit());
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, (event) -> {
            if(CONFIG.isFirstRun()) {
                ButtonType homeButton = new ButtonType("Use Home Directory");
                ButtonType otherButton = new ButtonType("Choose Other Directory");

                Alert firstRunAlert = new Alert(Alert.AlertType.INFORMATION, "Would you like to initialize the configurtion in your User Home directory, or choose another location?", homeButton, otherButton, ButtonType.CANCEL);
                firstRunAlert.setTitle(APP_NAME);
                firstRunAlert.setHeaderText("Configuration Not Found.");

                Optional<ButtonType> result = firstRunAlert.showAndWait();
                if(result.isEmpty() || result.get() == ButtonType.CANCEL) {
                    LauncherFX.info("Cancelled configuration alert. Exiting.");
                    Platform.exit();
                }
                else {
                    if(result.get() == homeButton) {
                        try {
                            CONFIG.initializeConfig();
                        }
                        catch(IOException ex) {
                            LauncherFX.error(ex);
                        }
                    }
                    else if(result.get() == otherButton) {
                        DirectoryChooser chooser = new DirectoryChooser();
                        chooser.setInitialDirectory(FileSystems.getDefault().getPath(System.getProperty("user.home")).toFile());

                        File dir = chooser.showDialog(primaryStage);
                        if(dir == null) {
                            LauncherFX.info("No directory chosen. Exiting.");
                            Platform.exit();
                        }
                        else {
                            try {
                                CONFIG.initializeConfig(dir.toPath());
                            }
                            catch(IOException ex) {
                                LauncherFX.error(ex);
                            }
                        }
                    }
                }
            }
            else {
                try {
                    CONFIG.loadConfig();
                }
                catch(IOException ex) {
                    LauncherFX.error(ex);
                }
            }

            refreshFromIni();
        });

        primaryStage.setResizable(true);
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
            if(!CONFIG.isHidden(ic.sectionName()) || showHidden) {
                Config.Type type = ic.getType();
                LaunchItemPane lip = new LaunchItemPane(ic);
                lip.addLaunchHandler(new LaunchItemEventHandler(ic));

                switch(type) {
                    case PORT -> {
                        portsList.add(ic);
                        portsBox.getChildren().add(lip);//new LaunchItemPane(ic, new LaunchItemEventHandler(ic), new EditMenuConfigDialogEventHandler(ic, "Edit Port")));
                        lip.setContextMenu(createLaunchButtonContextMenu(ic, "Edit Port"));
                        ic.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if(!reset) {
                                if(!newValue) { //false. this port has been deselected.
                                    // check mods/iwads with currently selected port in the event they were disabled
                                    // because of this port but may now be available.
                                    if(selectedPort == IniConfigurableItem.EMPTY_ITEM) {
                                        for(IniConfigurableItem iwad : iwadsList) {
                                            iwad.setEnabled(true);
                                        }

                                        for(IniConfigurableItem mod : modsList) {
                                            mod.setEnabled(true);
                                        }
                                    }
                                    else {
                                        if(selectedPort.getBoolean(Field.SKIPMODS) || tcPortToUse.getBoolean(Field.SKIPMODS)) {
                                            for(IniConfigurableItem mod : modsList) {
                                                mod.setEnabled(false);
                                            }
                                        }
                                        else {
                                            IniConfigurableItem checkAgainstPort = selectedPort;
                                            if(selectedPort.isType(Config.Type.TC)) {
                                                checkAgainstPort = tcPortToUse;
                                            }
                                            for(IniConfigurableItem mod : modsList) {
                                                String modSupportedPorts = mod.get(Field.PORT);
                                                if(modSupportedPorts == null || modSupportedPorts.toLowerCase().contains(checkAgainstPort.sectionName().toLowerCase())) {
                                                    mod.setEnabled(true);
                                                }
                                                else {
                                                    mod.setEnabled(false);
                                                }
                                            }
                                        }

                                        //i thought this was not needed then i thought it should be here but upon testing i'm thinking it is not needed.
//                                        String portSupportedIwads = selectedPort.get(Field.IWAD);
//                                        for(IniConfigurableItem iwad : iwadsList) {
//
//                                            String iwadSupportedPorts = iwad.get(Field.PORT);
//                                            if(iwadSupportedPorts == null || iwadSupportedPorts.toLowerCase().contains(selectedPort.sectionName().toLowerCase())
//                                                    && (portSupportedIwads == null || portSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase()))) {
//                                                iwad.setEnabled(true);
//                                            }
//                                            else {
//                                                iwad.setEnabled(false);
//                                            }
//                                        }
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
                                        for(IniConfigurableItem mod : modsList) {
                                            String modSupportedPorts = mod.get(Field.PORT);
                                            if(modSupportedPorts == null || modSupportedPorts.toLowerCase().contains(ic.sectionName().toLowerCase())) {
                                                mod.setEnabled(true);

                                                // mod may have redisabled itself if not compatible with something else so recheck.
                                                if(mod.isEnabled() && mod.getBoolean(Field.DEFAULT_ON)) {
                                                    mod.setSelected(true);
                                                }
                                            }
                                            else {
                                                mod.setEnabled(false);
                                            }
                                        }
                                    }

                                    String portSupportedIwads = ic.get(Field.IWAD);
                                    for(IniConfigurableItem iwad : iwadsList) {

                                        String iwadSupportedPorts = iwad.get(Field.PORT);
                                        if((iwadSupportedPorts == null || iwadSupportedPorts.toLowerCase().contains(ic.sectionName().toLowerCase()))
                                                && (portSupportedIwads == null || portSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase()))) {
                                            iwad.setEnabled(true);
                                        }
                                        else {
                                            iwad.setEnabled(false);
                                        }
                                    }
                                }
                            }
                        });
                        ic.enabledProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                            // if this port is enabled, reverse check that this port is compatible with currently selected mods/iwads and disable if it is not.
                            if(newValue && !reset) {
                                String portSupportedIwads = ic.get(Field.IWAD);
                                if(portSupportedIwads != null && !portSupportedIwads.toLowerCase().contains(selectedIwad.sectionName().toLowerCase())) {
                                    ic.setEnabled(false);
                                    return;
                                }

                                if(ic.getBoolean(Field.SKIPMODS) && !selectedModsList.isEmpty()) {
                                    ic.setEnabled(false);
                                }
                                else {
                                    for(IniConfigurableItem mod : selectedModsList) {
                                        String modSupportedPorts = mod.get(Field.PORT);
                                        if(modSupportedPorts != null && !modSupportedPorts.toLowerCase().contains(ic.sectionName().toLowerCase())) {
                                            ic.setEnabled(false);
                                            return;
                                        }
                                    }
                                }
                            }
                        });
                    }
                    case TC -> {
                        portsList.add(ic);
                        portsBox.getChildren().add(lip);//new LaunchItemPane(ic, new LaunchItemEventHandler(ic), new EditMenuConfigDialogEventHandler(ic, "Edit TC")));
                        lip.setContextMenu(createLaunchButtonContextMenu(ic, "Edit TC"));
                        ic.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if(!reset) {
                                if(!newValue) { //false. this port has been deselected.
                                    // check mods/iwads with currently selected port in the event they were disabled
                                    // because of this port but may now be available.
                                    if(selectedPort == IniConfigurableItem.EMPTY_ITEM) {
                                        for(IniConfigurableItem iwad : iwadsList) {
                                            iwad.setEnabled(true);
                                        }

                                        for(IniConfigurableItem mod : modsList) {
                                            mod.setEnabled(true);
                                        }
                                    }
                                    else {
                                        if(selectedPort.getBoolean(Field.SKIPMODS) || tcPortToUse.getBoolean(Field.SKIPMODS)) {
                                            for(IniConfigurableItem mod : modsList) {
                                                mod.setEnabled(false);
                                            }
                                        }
                                        else {
                                            IniConfigurableItem checkAgainstPort = selectedPort;
                                            if(selectedPort.isType(Config.Type.TC)) {
                                                checkAgainstPort = tcPortToUse;
                                            }
                                            for(IniConfigurableItem mod : modsList) {
                                                String modSupportedPorts = mod.get(Field.PORT);
                                                if(modSupportedPorts == null || modSupportedPorts.toLowerCase().contains(checkAgainstPort.sectionName().toLowerCase())) {
                                                    mod.setEnabled(true);
                                                }
                                                else {
                                                    mod.setEnabled(false);
                                                }
                                            }
                                        }

                                        //i thought this was not needed then i thought it should be here but upon testing i'm thinking it is not needed.
//                                        String portSupportedIwads = selectedPort.get(Field.IWAD);
//                                        for(IniConfigurableItem iwad : iwadsList) {
//                                            String iwadSupportedPorts = iwad.get(Field.PORT);
//                                            if(iwadSupportedPorts == null || iwadSupportedPorts.toLowerCase().contains(selectedPort.sectionName().toLowerCase())
//                                                    && (portSupportedIwads == null || portSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase()))) {
//                                                iwad.setEnabled(true);
//                                            }
//                                            else {
//                                                iwad.setEnabled(false);
//                                            }
//                                        }
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
                                        for(IniConfigurableItem mod : modsList) {
                                            String modSupportedPorts = mod.get(Field.PORT);
                                            if(modSupportedPorts == null || modSupportedPorts.toLowerCase().contains(tcPortToUse.sectionName().toLowerCase())) {
                                                mod.setEnabled(true);
                                            }
                                            else {
                                                mod.setEnabled(false);
                                            }
                                        }
                                    }

                                    if(ic.getBoolean(Field.SKIPIWAD)) {
                                        for(IniConfigurableItem iwad : iwadsList) {
                                            iwad.setEnabled(false);
                                        }
                                    }
                                    else {
                                        String portSupportedIwads = ic.get(Field.IWAD);
                                        for(IniConfigurableItem iwad : iwadsList) {
                                            String iwadSupportedPorts = iwad.get(Field.PORT);
                                            if((iwadSupportedPorts == null || iwadSupportedPorts.toLowerCase().contains(ic.sectionName().toLowerCase()))
                                                    && (portSupportedIwads == null || portSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase()))) {
                                                iwad.setEnabled(true);
                                            }
                                            else {
                                                iwad.setEnabled(false);
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        ic.enabledProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                            // if this port is enabled, reverse check that this tc is compatible with currently selected mods/iwads and disable if it is not.
                            if(newValue && !reset) {
                                String portSupportedIwads = ic.get(Field.IWAD);
                                if(portSupportedIwads != null && !portSupportedIwads.toLowerCase().contains(selectedIwad.sectionName().toLowerCase())) {
                                    ic.setEnabled(false);
                                    return;
                                }

                                if((ic.getBoolean(Field.SKIPMODS) || tcPortToUse.getBoolean(Field.SKIPMODS)) && !selectedModsList.isEmpty()) {
                                    ic.setEnabled(false);
                                }
                                else {
                                    for(IniConfigurableItem mod : selectedModsList) {
                                        String modSupportedPorts = mod.get(Field.PORT);
                                        if(modSupportedPorts != null && !modSupportedPorts.toLowerCase().contains(tcPortToUse.sectionName().toLowerCase())) {
                                            ic.setEnabled(false);
                                            return;
                                        }
                                    }
                                }
                            }
                        });
                    }
                    case IWAD -> {
                        iwadsList.add(ic);
                        iwadsBox.getChildren().add(lip);//new LaunchItemPane(ic, new LaunchItemEventHandler(ic), new EditMenuConfigDialogEventHandler(ic, "Edit IWAD")));
                        lip.setContextMenu(createLaunchButtonContextMenu(ic, "Edit IWAD"));
                        ic.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if(!reset) {
                                if(!newValue) { //false. this iwad has been deselected.
                                    // check ports/mods with currently selected iwad in the event they were disabled
                                    // because of this iwad but may now be available.
                                    if(selectedIwad == IniConfigurableItem.EMPTY_ITEM) {
                                        for(IniConfigurableItem port : portsList) {
//                                            port.setEnabled(true);
                                            port.incompatibleProperty().set(false);
                                        }

                                        for(IniConfigurableItem mod : modsList) {
                                            mod.setEnabled(true);
                                        }
                                    }
                                    else {
                                        for(IniConfigurableItem mod : modsList) {
                                            String modSupportedIwads = mod.get(Field.IWAD);
                                            if(modSupportedIwads == null || modSupportedIwads.toLowerCase().contains(selectedIwad.sectionName().toLowerCase())) {
                                                mod.setEnabled(true);
                                            }
                                            else {
                                                mod.setEnabled(false);
                                            }
                                        }

                                        String iwadSupportedPorts = selectedIwad.get(Field.PORT);
                                        for(IniConfigurableItem port : portsList) {
                                            String portSupportedIwads = port.get(Field.IWAD);
                                            if(iwadSupportedPorts == null || iwadSupportedPorts.toLowerCase().contains(port.sectionName().toLowerCase())
                                                    && (portSupportedIwads == null || portSupportedIwads.toLowerCase().contains(selectedIwad.sectionName().toLowerCase()))) {
//                                                port.setEnabled(true);
                                                port.incompatibleProperty().set(false);
                                            }
                                            else {
//                                            port.setEnabled(false);
                                                port.incompatibleProperty().set(true);
                                            }
                                        }
                                    }
                                }
                                else { // true. this iwad has been selected.
                                    // deselect other iwads
                                    for(IniConfigurableItem iwad : iwadsList) {
                                        if(iwad != ic) {
                                            iwad.setSelected(false);
                                        }
                                    }

                                    // enabled/disable ports/mods according to this iwad's compatibility settings.
                                    for(IniConfigurableItem mod : modsList) {
                                        String modSupportedIwads = mod.get(Field.IWAD);
                                        if(modSupportedIwads == null || modSupportedIwads.toLowerCase().contains(ic.sectionName().toLowerCase())) {
                                            mod.setEnabled(true);
                                        }
                                        else {
                                            mod.setEnabled(false);
                                        }
                                    }

                                    String iwadSupportedPorts = ic.get(Field.PORT);
                                    for(IniConfigurableItem port : portsList) {
                                        String portSupportedIwads = port.get(Field.IWAD);
                                        if((iwadSupportedPorts == null || iwadSupportedPorts.toLowerCase().contains(port.sectionName().toLowerCase()))
                                                && (portSupportedIwads == null || portSupportedIwads.toLowerCase().contains(ic.sectionName().toLowerCase()))) {
//                                            port.setEnabled(true);
                                        }
                                        else {
                                            //port.setEnabled(false);
                                            port.incompatibleProperty().set(true);
                                        }
                                    }
                                }
                            }
                        });
                        ic.enabledProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                            // if this iwad is enabled, reverse check that this iwad is compatible with currently selected ports/mods and disable if it is not.
                            if(newValue && !reset) {
                                String portSupportedIwads = selectedPort.get(Field.IWAD);
                                if(portSupportedIwads != null && !portSupportedIwads.toLowerCase().contains(ic.sectionName().toLowerCase())) {
                                    ic.setEnabled(false);
                                    return;
                                }

                                for(IniConfigurableItem mod : selectedModsList) {
                                    String modSupportedIwads = mod.get(Field.IWAD);
                                    if(modSupportedIwads != null && !modSupportedIwads.toLowerCase().contains(ic.sectionName().toLowerCase())) {
                                        ic.setEnabled(false);
                                        return;
                                    }
                                }
                            }
                        });
                    }
                    case MOD -> {
                        modsList.add(ic);
                        modsBox.getChildren().add(lip);//new LaunchItemPane(ic, new LaunchItemEventHandler(ic), new EditMenuConfigDialogEventHandler(ic, "Edit Mod")));
                        lip.setContextMenu(createLaunchButtonContextMenu(ic, "Edit Mod"));
                        ic.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if(!reset) {
                                if(!newValue) { //false. this mod has been deselected.
                                    // check ports/iwads with currently selected mod in the event they were disabled
                                    // because of this mod but may now be available.
                                    selectedModsList.remove(ic);
                                    if(selectedModsList.isEmpty()) {
                                        for(IniConfigurableItem port : portsList) {
//                                            port.setEnabled(true);
                                            port.incompatibleProperty().set(false);
                                        }

                                        for(IniConfigurableItem iwad : iwadsList) {
                                            iwad.setEnabled(true);
                                            iwad.incompatibleProperty().set(false);
                                        }
                                    }
                                    else {
                                        for(IniConfigurableItem port : portsList) {
                                            boolean enablePort = true;
                                            for(IniConfigurableItem mod : selectedModsList) {
                                                String modSupportedPorts = mod.get(Field.PORT);
                                                if(modSupportedPorts != null && !modSupportedPorts.toLowerCase().contains(port.sectionName().toLowerCase())) {
                                                    enablePort = false;
                                                }
                                            }
                                            if(enablePort) {
//                                                port.setEnabled(true);
                                                port.incompatibleProperty().set(false);
                                            }
                                            else {
                                                port.incompatibleProperty().set(true);
                                            }
                                        }

                                        for(IniConfigurableItem iwad : iwadsList) {
                                            boolean enableIwad = true;
                                            for(IniConfigurableItem mod : selectedModsList) {
                                                String modSupportedIwads = mod.get(Field.IWAD);
                                                if(modSupportedIwads != null && !modSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase())) {
                                                    enableIwad = false;
                                                }
                                            }
                                            iwad.setEnabled(enableIwad);
                                            iwad.incompatibleProperty().set(false);
                                        }
                                    }
                                }
                                else { // true. this mod has been selected.
                                    // enabled/disable ports/iwads according to this mod's compatibility settings.
                                    selectedModsList.add(ic);
                                    String modSupportedPorts = ic.get(Field.PORT);
                                    for(IniConfigurableItem port : portsList) {
                                        if(modSupportedPorts != null && !modSupportedPorts.toLowerCase().contains(port.sectionName().toLowerCase())) {
//                                        port.setEnabled(false);
                                            port.incompatibleProperty().set(true);
                                        }
                                        else {
//                                            port.setEnabled(true);
                                        }
                                    }

                                    String modSupportedIwads = ic.get(Field.IWAD);
                                    for(IniConfigurableItem iwad : iwadsList) {
                                        if(modSupportedIwads != null && !modSupportedIwads.toLowerCase().contains(iwad.sectionName().toLowerCase())) {
                                            //iwad.setEnabled(false);
                                            iwad.incompatibleProperty().set(true);
                                        }
                                        else {
                                            iwad.setEnabled(true);
                                        }
                                    }
                                }
                            }
                        });
                        ic.enabledProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                            // if this mod is enabled, reverse check that this mod is compatible with currently selected ports/iwads and disable if it is not.
                            if(newValue && !reset) {
                                if(selectedPort.getBoolean(Field.SKIPMODS) || tcPortToUse.getBoolean(Field.SKIPMODS)) {
                                    ic.setEnabled(false);
                                }
                                else {
                                    String modSupportedPorts = ic.get(Field.PORT);
                                    if(modSupportedPorts != null && !modSupportedPorts.toLowerCase().contains(selectedPort.sectionName().toLowerCase())) {
                                        ic.setEnabled(false);
                                    }
                                }

                                String modSupportedIwads = ic.get(Field.IWAD);
                                if(modSupportedIwads != null && !modSupportedIwads.toLowerCase().contains(selectedIwad.sectionName().toLowerCase())) {
                                    ic.setEnabled(false);
                                }
                            }
                        });
                    }
                }
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
                    LauncherFX.info("Deleted INI Section '" + ic.sectionName());
                }
                catch(IOException ex) {
                    LauncherFX.error(ex);
                    LauncherFX.info("An error occured writing to launcherfx.ini");
                }
            }
        });

        CheckMenuItem hideItem = new CheckMenuItem("Hide");
        hideItem.setSelected(CONFIG.isHidden(ic.sectionName()));
        hideItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                CONFIG.unHideSection(ic.sectionName());
            }
            else {
                CONFIG.hideSection(ic.sectionName());
                try {
                    CONFIG.writeIni();
                    refreshFromIni();
                }
                catch(IOException ex) {
                    LauncherFX.error(ex);
                }
            }
        });

        MenuItem moveUpItem = new MenuItem("Move Up");
        moveUpItem.addEventHandler(ActionEvent.ACTION, (event) -> {
            List<IniConfigurableItem> items;
            switch(ic.getType()) {
                case PORT, TC -> items = CONFIG.getPortsAndTcs();
                case IWAD -> items = CONFIG.getIwads();
                case MOD -> items = CONFIG.getMods();
                default -> {
                    LauncherFX.info("Unknown Type. Cannot move up.");
                    return;
                }
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
                }
                catch(IOException ex) {
                    LauncherFX.error(ex);
                    LauncherFX.info("An error occured writing to launcherfx.ini");
                }
            }
        });

        MenuItem moveDownItem = new MenuItem("Move Down");
        moveDownItem.addEventHandler(ActionEvent.ACTION, (event) -> {
            List<IniConfigurableItem> items;
            switch(ic.getType()) {
                case PORT, TC -> items = CONFIG.getPortsAndTcs();
                case IWAD -> items = CONFIG.getIwads();
                case MOD -> items = CONFIG.getMods();
                default -> {
                    LauncherFX.info("Unknown Type. Cannot move down.");
                    return;
                }
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
                }
                catch(IOException ex) {
                    LauncherFX.error(ex);
                    LauncherFX.info("An error occured writing to launcherfx.ini");
                }
            }
        });

        return new ContextMenu(editItem, deleteItem, hideItem, new SeparatorMenuItem(), moveUpItem, moveDownItem);
    }

    private void reset() {
        reset = true;

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

        CONFIG.getConfigurables().forEach(ic -> {
            ic.setEnabled(true);
            ic.setSelected(false);
            ic.incompatibleProperty().set(false);
        });

        selectedPort = IniConfigurableItem.EMPTY_ITEM;
        tcPortToUse = IniConfigurableItem.EMPTY_ITEM;
        selectedIwad = IniConfigurableItem.EMPTY_ITEM;
        selectedModsList.clear();

        reset = false;
    }

    /**
     * If pathStr is a relative path, it will be resolved to an absolute path stemming from
     * the subdir configSubDir of configHome.
     * <p>
     * If pathStr is absolute, it is returned unchanged.
     *
     * @param pathStr
     * @param configSubDir
     * @return
     */
    public static String resolvePathRelativeToConfig(String pathStr, String configSubDir) {
        return resolveRelativePathToAbsolute(pathStr, Paths.get(CONFIG.getConfigHome(), configSubDir).toString());
    }

    /**
     * If pathStr is a relative path, it will be resolved to an absolute path with the parent
     * of parentStr.
     * <p>
     * If pathStr is absolute, it is returned unchanged.
     *
     * @param pathStr
     * @param parentStr
     * @return
     */
    private static String resolveRelativePathToAbsolute(String pathStr, String parentStr) {
        String result;
        if(pathStr == null || "".equals(pathStr)) {
            return null;
        }
        else {
//            System.out.println("-----------------------");
//            System.out.println("resolveRelativePathToAbsolue");
            Path path = Paths.get(pathStr);
            if(path.isAbsolute()) {
//                System.out.println("pathStr='" + pathStr + "' is ABSOLUTE.");
                result = path.toString();
            }
            else {
                Path retPath = Paths.get(parentStr, path.toString());
//                System.out.println("pathStr='" + pathStr + "' is NOT ABSOLUTE, returning '" + retPath.toString() + "'.");
                result = retPath.toString();
            }
        }

        if(Files.exists(Paths.get(result))) {
            return result;
        }
        else {
            return null;
        }
    }

    private void checkLaunchNowAvailable() {
        if(selectedPort.isSelected() && (selectedPort.getBoolean(Field.SKIPIWAD) || selectedIwad.isSelected())) {
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
            //                selectThis = item; //idk if i want to auto-select. plus would select last level in a multi-level wad...
            item.highlight = warp.contains(item.display);
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
                if(o1 == PWadListItem.NO_PWAD) {
                    return -1;
                }
                if(o2 == PWadListItem.NO_PWAD) {
                    return 1;
                }
                return o1.display.compareToIgnoreCase(o2.display);
            });

            pwadList.add(PWadListItem.NO_PWAD);

            final boolean skipWads = selectedPort.getBoolean(Field.SKIPWADS);
            if(skipWads) {
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
                                catch(IOException ex) {
                                    LauncherFX.error(ex);
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
                }
                catch(IOException ex) {
                    LauncherFX.error(ex);
                }
            }
            removeFromWadList.forEach((toRemove) -> {
                Iterator<PWadListItem> i = pwadList.iterator();
                while(i.hasNext()) {
                    PWadListItem item = i.next();
                    if(item.path != null && item.path.equals(toRemove)) {
                        if(showHiddenPwadItemsCheckBox.isSelected()) {
                            if(!item.display.endsWith("(ignored)")) {
                                item.display = item.display.concat(" (auto-hidden)");
                            }
                        }
                        else {
                            i.remove();
                        }
                    }
                }
            });
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
                        if(pwadItem != null && pwadItem.getBoolean(Field.IGNORE)) {
                            removeFromWadList.add(file);
                            txtListItem.display += " (ignored)";
                        }
                        theWadSet.add(txtListItem);
                    }
                    else if(filename.endsWith(".deh")) {
                        PWadListItem dehListItem = new PWadListItem(PWadListItem.Type.DEH, file.getFileName().toString(), file, null);

                        IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(file.getFileName().toString());
                        if(pwadItem != null && pwadItem.getBoolean(Field.IGNORE)) {
                            removeFromWadList.add(file);
                            dehListItem.display += " (ignored)";
                        }
                        theWadSet.add(dehListItem);
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
            final boolean ignore = pwadItem.getBoolean(Field.IGNORE);
            if(ignore && !showHiddenPwadItemsCheckBox.isSelected()) {
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

                if(ignore) {
                    name += " (ignored)";
                }

                String txt = pwadItem.get(Field.TXT);
                if(txt != null) {
                    name += " (.txt)";
                    removeFromWadList.add(pwadPath.resolveSibling(txt));
                }
                else {
                    Path txtPath = pwadPath.resolveSibling(changeExtensionRetainingCase(fileName, "txt"));
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
                }
                else {
                    //if args isn't defined, innocently check for a .deh file that matches the wad filename and create the args for it.
                    Path dehPath = pwadPath.resolveSibling(changeExtensionRetainingCase(fileName, "deh"));
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
            Path dehPath = pwadPath.resolveSibling(changeExtensionRetainingCase(fileName, "deh"));
            if(Files.exists(dehPath)) {
                item.args = "-deh \"" + dehPath.toString() + "\" -file \"" + pwadPath.toString() + "\"";
                removeFromWadList.add(dehPath); // do i want to do this?
            }

            Path txtPath = pwadPath.resolveSibling(changeExtensionRetainingCase(fileName, "txt"));
            if(Files.exists(txtPath)) {
                item.display += " (.txt)";
                item.txt = txtPath.getFileName().toString();
                removeFromWadList.add(txtPath);
            }
            return item;
        }
    }

    private void setPwadItemsToIgnore(List<PWadListItem> items, boolean ignore) {
        items.stream().filter((listItem) -> (listItem != PWadListItem.NO_PWAD)).map((listItem) -> listItem.path.getFileName().toString()).forEachOrdered((sectionName) -> {
            IniConfigurableItem pwadItem = CONFIG.getConfigurableByName(sectionName);
            if(ignore) {
                if(pwadItem == null) {
                    CONFIG.addNewSection(sectionName);
                    CONFIG.update(sectionName, Field.TYPE, Config.Type.PWAD.iniValue());
                }
                CONFIG.update(sectionName, Field.IGNORE, true);
            }
            else {
                CONFIG.update(sectionName, Field.IGNORE, null);
            }
        });

        try {
            CONFIG.writeIni();
            loadPwadList();
        }
        catch(IOException ex) {
            LauncherFX.error(ex);
            LauncherFX.info("Error writing ini.");
        }
    }

    /**
     * Takes the fileName and checks whether the extension oldExtension is upper or lower case and
     * replaces it with newExtension using the same case as the original.
     * <p>
     * Returns null if oldExtension isn't even the right extension!
     *
     * @param fileName
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
                StringBuilder longArg = new StringBuilder();
                for(String arg : splitArgs) {
                    if(arg.isEmpty()) {
                        continue;
                    }
                    if(longArg.length() > 0) {
                        longArg.append(" ").append(arg);
                    }
                    if(arg.startsWith("\"")) {
                        longArg.append(arg);
                        if(!longArg.toString().endsWith("\"")) {
                            continue;
                        }
                    }
                    if(longArg.toString().endsWith("\"")) {
                        longArg = new StringBuilder(longArg.substring(1, longArg.length() - 1));
                        argsList.add(longArg.toString());
                        longArg = new StringBuilder();
                        continue;
                    }
                    if((longArg.length() > 0) && !longArg.toString().endsWith("\"")) {
                        continue;
                    }
                    argsList.add(arg);
                }
                processCommand.addAll(argsList);
                LauncherFX.info("current cmd=" + processCommand);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public static void info(String message) {
        Logger.getLogger(LauncherFX.class.getName()).log(Level.INFO, message);
    }

    public static void error(Exception ex) {
        Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
    }

    private class EditMenuConfigDialogEventHandler implements EventHandler<ActionEvent> {

        private final Config.Type         type;
        private       String              title;
        private       IniConfigurableItem item;
        private       String              sectionName;
        private       Path                pwadPath;

        EditMenuConfigDialogEventHandler(Config.Type type, String title) {
            this.type = type;
            this.item = null;
            this.title = title;
            this.sectionName = null;
            this.pwadPath = null;
        }

        EditMenuConfigDialogEventHandler(IniConfigurableItem item, String title) {
            this.type = null;
            this.item = item;
            this.title = title;
            this.sectionName = null;
            this.pwadPath = null;
        }

        void setItem(IniConfigurableItem item) {
            this.item = item;
        }

        void setTitle(String title) {
            this.title = title;
        }

        void setSectionName(String sectionName) {
            this.sectionName = sectionName;
        }

        void setPwadPath(Path pwadPath) {
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
                    if(dialog.isType(Config.Type.PWAD)) {
                        loadPwadList();
                    }
                    else {
                        refreshFromIni();
                    }
                }
                catch(IOException ex) {
                    LauncherFX.error(ex);
                    LauncherFX.info("An error occured writing to launcherfx.ini");
                }
            }
        }
    }

    private class LaunchItemEventHandler implements EventHandler<ActionEvent> {
        private final IniConfigurableItem ic;

        LaunchItemEventHandler(IniConfigurableItem ic) {
            this.ic = ic;
        }

        @Override
        public void handle(ActionEvent e) {
            LaunchButton myButton = (LaunchButton) e.getSource();
            switch(ic.getType()) {
                case PORT -> {
                    if(selectedPort == ic) {
                        selectedPort = IniConfigurableItem.EMPTY_ITEM;
                        ic.setSelected(false);
                    }
                    else {
                        String portCmd = ic.getCmd();
                        if(portCmd != null) {
                            processCommand = new ArrayList<>();
                            addArgsToProcess(portCmd);

                            selectedPort = ic;
                            selectedPort.setSelected(true);
                        }
                    }
                    checkLaunchNowAvailable();
                    loadPwadList();
                    loadWarpList();
                }
                case TC -> {
                    if(selectedPort == ic) {
                        selectedPort = IniConfigurableItem.EMPTY_ITEM;
                        ic.setSelected(false);
                    }
                    else {
                        tcPortToUse = IniConfigurableItem.EMPTY_ITEM;

                        String portStr = ic.get(Field.PORT);
                        assert portStr != null;
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
                            String tcCmd = tcPortToUse.getCmd();

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
                    checkLaunchNowAvailable();
                    loadPwadList();
                    loadWarpList();
                }
                case MOD -> {
                    if(myButton.isChecked()) {
                        ic.setSelected(false);
                    }
                    else {
                        ic.setSelected(true);
                    }
                }
                case IWAD -> {
//                    String iwadPath = resolvePathRelativeToConfig(ic.get(Field.FILE), Config.DIR_IWAD);

//                    selectedIwad.setSelected(false);
                    if(selectedIwad == ic) {
                        selectedIwad = IniConfigurableItem.EMPTY_ITEM;
                        ic.setSelected(false);
                    }
                    else {
                        String iwadPath;
                        if(ic.get(Field.FILE).startsWith("\"") && ic.get(Field.FILE).endsWith("\"")) {
                            iwadPath = resolvePathRelativeToConfig(ic.get(Field.FILE).substring(1, ic.get(Field.FILE).length() - 1), Config.DIR_IWAD);
                        }
                        else {
                            iwadPath = resolvePathRelativeToConfig(ic.get(Field.FILE), Config.DIR_IWAD);
                        }

                        try {
                            selectedGame = Game.getGameData(iwadPath);
                        }
                        catch(IOException ex) {
                            LauncherFX.error(ex);
                            LauncherFX.info("IWAD file not found.");
                            new Alert(Alert.AlertType.ERROR, "IWAD file not found.", ButtonType.CLOSE).showAndWait();
                            selectedGame = Game.UNKNOWN_GAME;
                        }
                        catch(NoSuchAlgorithmException ex) {
                            LauncherFX.error(ex);
                            LauncherFX.info("Error occured detecting game.");
                            new Alert(Alert.AlertType.ERROR, "Error occured detecting game.", ButtonType.CLOSE).showAndWait();
                            selectedGame = Game.UNKNOWN_GAME;
                        }

                        if(selectedGame == Game.UNKNOWN_GAME && ic.get(Field.GAME) != null) {
                            selectedGame = Game.valueOf(ic.get(Field.GAME).toUpperCase());
                        }
                        selectedIwad = ic;
                        selectedIwad.setSelected(true);
                    }
                    checkLaunchNowAvailable();
                    loadPwadList();
                    loadWarpList();
                }
            }
        }
    }
}
