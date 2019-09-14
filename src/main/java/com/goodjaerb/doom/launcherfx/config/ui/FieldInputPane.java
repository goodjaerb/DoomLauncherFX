/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config.ui;

import com.goodjaerb.doom.launcherfx.config.Config;
import com.goodjaerb.doom.launcherfx.config.Field;
import com.goodjaerb.doom.launcherfx.config.IniConfigurableItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author goodjaerb<goodjaerb @ gmail.com>
 */
final class FieldInputPane extends FlowPane {
    private final IniConfigurableItem item;
    private final Config.Type         type;
    private final Field               field;

    private Label              label;
    private TextField          textField;
    private Button             browseButton;
    private FileChooser        chooser;
    private DirectoryChooser   dirChooser;
    private ListView<ListItem> listView;
    private CheckBox           checkBox;

    private Path pwadPath;

    FieldInputPane(Config.Type type, Field field) {
        this(type, field, null);
    }

    FieldInputPane(Config.Type type, Field field, Path pwadPath) {
        this.item = null;
        this.type = type;
        this.field = field;
        this.pwadPath = pwadPath;
        doLayout();
    }

    FieldInputPane(IniConfigurableItem item, Field field) {
        this(item, field, null);
    }

    FieldInputPane(IniConfigurableItem item, Field field, Path pwadPath) {
        this.item = item;
        this.type = item.getType();
        this.field = field;
        this.pwadPath = pwadPath;
        doLayout();

        switch(field.inputType) {
            case TEXT:
            case BROWSE:
            case BROWSE_DIR:
                textField.setText(item.get(field));
                break;
            case BOOLEAN:
                checkBox.setSelected(item.getBoolean(field));
                break;
            case LIST:
            case MULTI_LIST:
                if(item.get(field) != null) {
                    String[] split = item.get(field).split(",");
                    for(String s : split) {
                        for(ListItem i : listView.getItems()) {
                            if(i.value.equals(s)) {
                                listView.getSelectionModel().select(i);
                                break;
                            }
                        }
                    }
                }
                break;
            default:
        }
    }

    boolean isRequired() {
        return field.isRequired(type);
    }

    Field getField() {
        return field;
    }

    private void doLayout() {
        setHgap(4);
        switch(field.inputType) {
            case BOOLEAN -> {
                initLabel();
                initCheckBox();
                getChildren().addAll(checkBox, label);
            }
            case BROWSE_DIR -> {
                initLabel();
                initTextField();
                initBrowseDirButton();
                getChildren().addAll(label, textField, browseButton);
            }
            case BROWSE -> {
                initLabel();
                initTextField();
                initBrowseButton();
                switch(field) {
                    case FILE -> {
                        switch(type) {
                            case IWAD -> {
                                chooser.setInitialDirectory(Paths.get(Config.getInstance().getConfigHome(), Config.DIR_IWAD).toFile());
                                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".WAD files", "*.WAD"));
                            }
                            case MOD -> {
                                chooser.setInitialDirectory(Paths.get(Config.getInstance().getConfigHome(), Config.DIR_MODS).toFile());
                                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
                                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".PK3 files", "*.PK3"));
                                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".PK7 files", "*.PK7"));
                            }
                        }
                    }
                    case IMG -> {
                        chooser.setInitialDirectory(Paths.get(Config.getInstance().getConfigHome(), Config.DIR_IMAGES).toFile());
                        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File", "*.PNG", "*.JPG", "*.JPEG", "*.GIF"));
                    }
                    case TXT -> {
                        if(pwadPath != null) {
                            chooser.setInitialDirectory(pwadPath.getParent().toFile());
                        }
                        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.TXT"));
                    }
                }
                getChildren().addAll(label, textField, browseButton);
            }
            case LIST -> {
                initLabel();
                initListView();
                if(field == Field.GAME) {
                    ObservableList<ListItem> list = FXCollections.observableArrayList(
                            new ListItem("Doom", "DOOM"),
                            new ListItem("Ultimate Doom", "ULTIMATE"),
                            new ListItem("Doom II", "DOOM2"),
                            new ListItem("Heretic", "HERETIC"),
                            new ListItem("Heretic: Shadow of the Serpent Riders", "HERETIC_EXP"));

                    listView.setItems(list);
                }
                getChildren().addAll(label, listView);
            }
            case MULTI_LIST -> {
                initLabel();
                initMultiListView();
                ObservableList<ListItem> list = FXCollections.observableArrayList();
                switch(field) {
                    case PORT -> Config.getInstance().getPorts().forEach((port) -> list.add(new ListItem(port.get(Field.NAME), port.sectionName())));
                    case IWAD -> Config.getInstance().getIwads().forEach((iwad) -> list.add(new ListItem(iwad.get(Field.NAME), iwad.sectionName())));
                    case WADFOLDER -> {
                        Path wadPath = FileSystems.getDefault().getPath(Config.getInstance().getConfigHome(), Config.DIR_WADS);
                        try {
                            Files.list(wadPath).forEach((wadPathItem) -> {
                                if(Files.isDirectory(wadPathItem)) {
                                    list.add(new ListItem(wadPathItem.getFileName().toString()));
                                }
                            });
                        }
                        catch(IOException ex) {
                            Logger.getLogger(FieldInputPane.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                listView.setItems(list);
                getChildren().addAll(label, listView);
            }
            case TEXT -> {
                initLabel();
                initTextField();
                getChildren().addAll(label, textField);
            }
        }
    }

    private void initBrowseDirButton() {
        browseButton = new Button("Browse...");
        dirChooser = new DirectoryChooser();

        dirChooser.setInitialDirectory(new File(Config.getInstance().getConfigHome()));

        browseButton.addEventHandler(ActionEvent.ACTION, (event) -> {
            File file = dirChooser.showDialog(((Node) event.getTarget()).getScene().getWindow());
            if(file != null) {
                textField.setText(file.getAbsolutePath());
            }
        });
    }

    private void initBrowseButton() {
        browseButton = new Button("Browse...");
        chooser = new FileChooser();
        if(pwadPath != null) {
            chooser.setInitialDirectory(pwadPath.getParent().toFile());
        }
        else {
            chooser.setInitialDirectory(new File(Config.getInstance().getConfigHome()));
        }
        browseButton.addEventHandler(ActionEvent.ACTION, (event) -> {
            List<File> files = null;
            if(type == Config.Type.MOD) {
                files = chooser.showOpenMultipleDialog(((Node) event.getTarget()).getScene().getWindow());
            }
            else {
                File file = chooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
                if(file != null) {
                    files = new ArrayList<>();
                    files.add(file);
                }
            }

            if(files != null && !files.isEmpty()) {
                textField.clear();
                for(File file : files) {
                    Path filePath = Paths.get(file.toURI());
                    Path configRootPath = null;
                    switch(field) {
                        case FILE -> configRootPath = switch(type) {
                            case IWAD -> Paths.get(Config.getInstance().getConfigHome(), Config.DIR_IWAD);
                            case MOD -> Paths.get(Config.getInstance().getConfigHome(), Config.DIR_MODS);
                            default -> null;
                        };
                        case IMG -> configRootPath = Paths.get(Config.getInstance().getConfigHome(), Config.DIR_IMAGES);
                        case TXT -> {
                            if(pwadPath != null) {
                                configRootPath = pwadPath.getParent();
                            }
                        }
                    }

                    if(configRootPath == null || !filePath.startsWith(configRootPath)) {
                        // CMD will only be single file.
                        // don't quote images or text files.
                        switch(field) {
                            case WIN_CMD, LINUX_CMD -> textField.setText("\"" + filePath.toString() + "\" ");
                            case TXT, IMG -> textField.setText(filePath.toString());
                            default -> textField.setText(textField.getText() + "\"" + filePath.toString() + "\" ");
                        }
                    }
                    else {
                        // don't quote images or text files.
                        switch(field) {
                            case TXT, IMG -> textField.setText(configRootPath.relativize(filePath).toString());
                            default -> textField.setText(textField.getText() + "\"" + configRootPath.relativize(filePath).toString() + "\" ");
                        }
                    }
                }
                if(field != Field.TXT && field != Field.IMG) {
                    textField.setText(textField.getText().substring(0, textField.getText().length() - 1));
                }
            }
        });
    }

    private void initLabel() {
        label = new Label((field.isRequired(type) ? "*" : "") + field.label + " (?)");
        label.setPrefWidth(350);
        label.setTooltip(new Tooltip(field.helpMap.get(type).replace("%CONFIGHOME%", Config.getInstance().getConfigHome())));
    }

    private void initTextField() {
        textField = new TextField();
        textField.setPrefWidth(300);
        if(item != null) {
            item.get(field);
        }
    }

    private void initListView() {
        listView = new ListView<>();
        listView.setPrefSize(200, 75);
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void initMultiListView() {
        initListView();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void initCheckBox() {
        checkBox = new CheckBox();
        checkBox.setSelected(false);
    }

    String getValue() {
        return switch(field.inputType) {
            case TEXT, BROWSE, BROWSE_DIR -> textField.getText();
            case BOOLEAN -> {
                if(checkBox.isSelected()) {
                    break Config.TRUE;
                }
                break null;
            }
            case LIST -> listView.getSelectionModel().getSelectedItem().getValue();
            case MULTI_LIST -> {
                String value = null;
                if(!listView.getSelectionModel().getSelectedItems().isEmpty()) {
                    value = "";
                    value = listView.getSelectionModel().getSelectedItems().stream().map((listitem) -> listitem.getValue() + ",").reduce(value, String::concat);
                    value = value.substring(0, value.length() - 1);
                }

                break value;
            }
            case HIDDEN ->
                    switch(field) {
                        case TYPE -> type.iniValue();
                        case SORT -> {
                            if(item != null) {
                                break item.get(Field.SORT);
                            }
                            break null;
                        }
                        default -> null;
            };
        };
    }

    private static class ListItem {
        private final String display;
        private final String value;

        ListItem(String display, String value) {
            this.display = display;
            this.value = value;
        }

        ListItem(String displayAndValue) {
            this.display = displayAndValue;
            this.value = displayAndValue;
        }

        @Override
        public String toString() {
            return display;
        }

        String getValue() {
            return value;
        }
    }
}
