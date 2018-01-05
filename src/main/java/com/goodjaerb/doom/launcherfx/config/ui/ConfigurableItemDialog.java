/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config.ui;

import com.goodjaerb.doom.launcherfx.config.Config;
import com.goodjaerb.doom.launcherfx.config.Field;
import com.goodjaerb.doom.launcherfx.config.IniConfigurableItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class ConfigurableItemDialog extends Dialog<ButtonType> {
    private final IniConfigurableItem item;
    private VBox contentPane;
    
    private List<FieldInputPane> fieldInputPanes;
    
    public ConfigurableItemDialog(Config.Type type, String title) {
        this.item = null;
        setTitle(title);
        layout(type);
    }
    
    public ConfigurableItemDialog(IniConfigurableItem item, String title) {
        this.item = item;
        setTitle(title);
        layout(item.getType());
    }
    
    public void applyValues() throws IOException {
        if(item == null) {
            // it's a new section and i know that NAME should be the first input pane.
            String sectionName = Config.getInstance().addNewSection(fieldInputPanes.get(0).getValue().replaceAll("[\\s\\p{Punct}]*", ""));
            fieldInputPanes.forEach((fip) -> {
                String value = fip.getValue();
                if(value != null && !value.trim().equals("")) {
                    Config.getInstance().update(sectionName, fip.getField(), fip.getValue());
                }
            });
            Config.getInstance().writeIni();
        }
    }
    
    private boolean requiredFieldsArePresent() {
        return fieldInputPanes.stream().noneMatch((fip) -> (fip.isRequired() && (fip.getValue() == null || fip.getValue().trim().equals(""))));
    }
    
    private void layout(Config.Type type) {
        fieldInputPanes = new ArrayList<>();
        
        contentPane = new VBox();
        contentPane.setPadding(new Insets(4));
        contentPane.setSpacing(4);
        
        for(Field f : Field.values()) {
            if(f.validTypes.contains(type)) {
                FieldInputPane fip;
                if(item == null) {
                    fip = new FieldInputPane(type, f);
                }
                else {
                    fip = new FieldInputPane(item, f);
                }
                fieldInputPanes.add(fip);
                contentPane.getChildren().add(fip);
            }
        }
        getDialogPane().setContent(contentPane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        final Button btOk = (Button)getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            if(!requiredFieldsArePresent()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Required fields not entered.", ButtonType.OK);
                alert.showAndWait();
                event.consume();
            }
        });
    }
}