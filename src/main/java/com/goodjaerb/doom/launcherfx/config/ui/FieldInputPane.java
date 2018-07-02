/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config.ui;

import com.goodjaerb.doom.launcherfx.config.Config;
import com.goodjaerb.doom.launcherfx.config.IniConfigurableItem;
import com.goodjaerb.doom.launcherfx.config.Field;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public final class FieldInputPane extends FlowPane {
    private final IniConfigurableItem item;
    private final Config.Type type;
    private final Field field;
    
    private Label label;
    private TextField textField;
    private Button browseButton;
    private FileChooser chooser;
    private ListView<ListItem> listView;
    private CheckBox checkBox;
    
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
                textField.setText(item.get(field));
                break;
            case BOOLEAN:
                if("true".equals(item.get(field))) {
                    checkBox.setSelected(true);
                }
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
    
    public boolean isRequired() {
        return field.isRequired(type);
    }
    
    public Field getField() {
        return field;
    }
    
    private void doLayout() {
        setHgap(4);
        switch(field.inputType) {
            case BOOLEAN:
                initLabel();
                initCheckBox();
                
                getChildren().addAll(checkBox, label);
                break;
            case BROWSE:
                initLabel();
                initTextField();
                initBrowseButton();
                
                switch(field) {
                    case FILE:
                        switch(type) {
                            case IWAD:
                                chooser.setInitialDirectory(Paths.get(Config.getInstance().getConfigHome(), Config.DIR_IWAD).toFile());
                                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".WAD files", "*.WAD"));
                                break;
                            case MOD:
                                chooser.setInitialDirectory(Paths.get(Config.getInstance().getConfigHome(), Config.DIR_MODS).toFile());
                                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
                                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".PK3 files", "*.PK3"));
                                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".PK7 files", "*.PK7"));
                                break;
                            default:
                        }
                        break;
                    case IMG:
                        chooser.setInitialDirectory(Paths.get(Config.getInstance().getConfigHome(), Config.DIR_IMAGES).toFile());
                        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File", "*.PNG", "*.JPG", "*.JPEG", "*.GIF"));
                        break;
                    case TXT:
                        if(pwadPath != null) {
                            chooser.setInitialDirectory(pwadPath.getParent().toFile());
                        }
                        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.TXT"));
                        break;
                    default:
                }
                getChildren().addAll(label, textField, browseButton);
                break;
            case LIST:
                initLabel();
                initListView();
                
                switch(field) {
                    case GAME:
                        ObservableList<ListItem> list = FXCollections.observableArrayList(
                                new ListItem("Doom", "DOOM"),
                                new ListItem("Ultimate Doom", "ULTIMATE"),
                                new ListItem("Doom II", "DOOM2"),
                                new ListItem("Heretic", "HERETIC"),
                                new ListItem("Heretic: Shadow of the Serpent Riders", "HERETIC_EXP"));

                        listView.setItems(list);
                        break;
                    default:
                }
                getChildren().addAll(label, listView);
                break;
            case MULTI_LIST:
                initLabel();
                initMultiListView();
                
                ObservableList<ListItem> list = FXCollections.observableArrayList();
                switch(field) {
                    case PORT:
                        Config.getInstance().getPorts().forEach((port) -> {
                            list.add(new ListItem(port.get(Field.NAME), port.sectionName()));
                        });
                        break;
                    case IWAD:
                        Config.getInstance().getIwads().forEach((iwad) -> {
                            list.add(new ListItem(iwad.get(Field.NAME), iwad.sectionName()));
                        });
                        break;
                    case WADFOLDER:
                        Path wadPath = FileSystems.getDefault().getPath(Config.getInstance().getConfigHome(), Config.DIR_WADS);
                        try {
                            Files.list(wadPath).forEach((wadPathItem) -> {
                                if(Files.isDirectory(wadPathItem)) {
                                    list.add(new ListItem(wadPathItem.getFileName().toString()));
                                }
                            });
                        } catch (IOException ex) {
                            Logger.getLogger(FieldInputPane.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    default:
                }
                
                listView.setItems(list);
                getChildren().addAll(label, listView);
                break;
            case HIDDEN:
                break;
            case TEXT:
                initLabel();
                initTextField();
                
                getChildren().addAll(label, textField);
                break;
            default:
        }
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
                files = chooser.showOpenMultipleDialog(((Node)event.getTarget()).getScene().getWindow());
            }
            else {
                File file = chooser.showOpenDialog(((Node)event.getTarget()).getScene().getWindow());
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
                        case FILE:
                            switch(type) {
                                case IWAD:
                                    configRootPath = Paths.get(Config.getInstance().getConfigHome(), Config.DIR_IWAD);
                                    break;
                                case MOD:
                                    configRootPath = Paths.get(Config.getInstance().getConfigHome(), Config.DIR_MODS);
                                    break;
                                default:
                            }
                            break;
                        case IMG:
                            configRootPath = Paths.get(Config.getInstance().getConfigHome(), Config.DIR_IMAGES);
                            break;
                        case TXT:
                            if(pwadPath != null) {
                                configRootPath = pwadPath.getParent();
                            }
                            break;
                        default:
                    }

                    if(configRootPath == null || !filePath.startsWith(configRootPath)) {
                        switch(field) {
                            case WIN_CMD:
                            case LINUX_CMD:
                                // CMD will only be single file.
                                textField.setText("\"" + filePath.toString() + "\" ");
                                break;
                            case TXT:
                            case IMG:
                                // don't quote images or text files.
                                textField.setText(filePath.toString());
                                break;
                            default:
                                textField.setText(textField.getText() + "\"" + filePath.toString() + "\" ");
                        }
                    }
                    else {
                        switch(field) {
                            case TXT:
                            case IMG:
                                // don't quote images or text files.
                                textField.setText(configRootPath.relativize(filePath).toString());
                                break;
                            default:
                                textField.setText(textField.getText() + "\"" + configRootPath.relativize(filePath).toString() + "\" ");
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
        switch(field.inputType) {
            case TEXT:
            case BROWSE:
                return textField.getText();
            case BOOLEAN:
                if(checkBox.isSelected()) {
                    return "true";
                }
                break;
            case LIST:
                if(listView.getSelectionModel().getSelectedItem() != null) {
                    return listView.getSelectionModel().getSelectedItem().getValue();
                }
                break;
            case MULTI_LIST:
                String value = null;
                if(!listView.getSelectionModel().getSelectedItems().isEmpty()) {
                    value = "";
                    value = listView.getSelectionModel().getSelectedItems().stream().map((listitem) -> listitem.getValue() + ",").reduce(value, String::concat);
                    value = value.substring(0, value.length() - 1);
                }
                
                if(value != null) {
                    return value;
                }
                break;
            case HIDDEN:
                switch(field) {
                    case TYPE:
                        return type.iniValue();
                    case SORT:
                        if(item != null) {
                            return item.get(Field.SORT);
                        }
                    default:
                }
                break;
            default:
        }
        return null;
    }
    
    private class ListItem {
        private final String display;
        private final String value;
        
        public ListItem(String display, String value) {
            this.display = display;
            this.value = value;
        }
        
        public ListItem(String displayAndValue) {
            this.display = displayAndValue;
            this.value = displayAndValue;
        }
        
        @Override
        public String toString() {
            return display;
        }
        
        public String getValue() {
            return value;
        }
    }
}
