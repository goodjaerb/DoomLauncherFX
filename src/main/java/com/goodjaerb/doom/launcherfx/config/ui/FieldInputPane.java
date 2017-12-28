/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config.ui;

import com.goodjaerb.doom.launcherfx.config.Config;
import com.goodjaerb.doom.launcherfx.config.IniConfigurableItem;
import com.goodjaerb.doom.launcherfx.config.Field;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;

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
    private ListView<ListItem> listView;
    private CheckBox checkBox;
    
    FieldInputPane(Config.Type type, Field field) {
        this.item = null;
        this.type = type;
        this.field = field;
        doLayout();
    }
    
    FieldInputPane(IniConfigurableItem item, Field field) {
        this.item = item;
        this.type = item.getType();
        this.field = field;
        doLayout();
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
                browseButton = new Button("Browse...");
                
                getChildren().addAll(label, textField, browseButton);
                break;
            case LIST:
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
                    getChildren().addAll(label, listView);
                }
                break;
            case MULTI_LIST:
                initLabel();
                initMultiListView();
                
                if(field == Field.PORT) {
                    ObservableList<ListItem> list = FXCollections.observableArrayList();
                    Config.getInstance().getPorts().forEach((port) -> {
                        list.add(new ListItem(port.get(Field.NAME), port.sectionName()));
                    });
                    listView.setItems(list);
                }
                
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
    
    private void initLabel() {
        label = new Label(field.label + " (?)");
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
        listView.setPrefSize(200, 50);
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
    
    void applyValue() {
        switch(field.inputType) {
            case BOOLEAN:
                if(checkBox.isSelected()) {
                    System.out.println(field.name().toLowerCase() + "=true");
                }
                break;
            case BROWSE:
                System.out.println(field.name().toLowerCase() + "=" + textField.getText());
                break;
            case LIST:
                if(listView.getSelectionModel().getSelectedItem() != null) {
                    System.out.println(field.name().toLowerCase() + "=" + listView.getSelectionModel().getSelectedItem().getValue());
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
                    System.out.println(field.name().toLowerCase() + "=" + value);
                }
                break;
            case HIDDEN:
                if(field == Field.TYPE) {
                    System.out.println(field.name().toLowerCase() + "=" + type.name().toLowerCase());
                }
                break;
            case TEXT:
                System.out.println(field.name().toLowerCase() + "=" + textField.getText());
                break;
            default:
        }
    }
    
    private class ListItem {
        private final String display;
        private final String value;
        
        public ListItem(String display, String value) {
            this.display = display;
            this.value = value;
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
