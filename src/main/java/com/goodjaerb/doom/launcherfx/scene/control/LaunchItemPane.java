/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control;

import com.goodjaerb.doom.launcherfx.config.Field;
import com.goodjaerb.doom.launcherfx.config.IniConfigurableItem;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 *
 * @author goodjaerb
 */
public class LaunchItemPane extends BorderPane {
    public final IniConfigurableItem configurableItem;
    
    private final Button launchButton;
    private final Label nameLabel;
    private final Text descriptionArea;
    
    public LaunchItemPane(IniConfigurableItem item, String imgPathStr, EventHandler<ActionEvent> handler) {
        this.configurableItem = item;
        
        launchButton = new Button();
        launchButton.textProperty().bind(item.valueProperty(Field.NAME));
        launchButton.addEventHandler(ActionEvent.ACTION, handler);
        
        nameLabel = new Label();
        nameLabel.textProperty().bind(item.valueProperty(Field.NAME));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px");
        
        descriptionArea = new Text();
        descriptionArea.textProperty().bind(item.valueProperty(Field.DESC));
        
        if(imgPathStr != null && Files.exists(Paths.get(imgPathStr))) {
            ImageView icon = null;
            try {
                icon = new ImageView(Paths.get(imgPathStr).toUri().toURL().toString());
                icon.setPreserveRatio(true);
                icon.setFitHeight(150);
                icon.setFitWidth(150);
            } catch (MalformedURLException ex) {
                Logger.getLogger(LaunchItemPane.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            launchButton.setGraphic(icon);
        }
        
        layoutPane();
    }
    
    private void layoutPane() {
        launchButton.setMinSize(175, 175);
        launchButton.setMaxSize(175, 175);
        
        descriptionArea.setWrappingWidth(400);
        descriptionArea.minWidth(400);
        descriptionArea.maxWidth(400);
        descriptionArea.minHeight(125);
        descriptionArea.maxHeight(170);

        VBox vBox = new VBox();
        vBox.setSpacing(2);
        vBox.getChildren().add(nameLabel);
        vBox.getChildren().add(descriptionArea);
        
        setLeft(launchButton);
        setCenter(vBox);
        
        setMargin(launchButton, new Insets(4));
        setMargin(vBox, new Insets(4, 4, 4, 0));
    }
    
    public void setButtonDisable(boolean b) {
        launchButton.setDisable(b);
    }
}
