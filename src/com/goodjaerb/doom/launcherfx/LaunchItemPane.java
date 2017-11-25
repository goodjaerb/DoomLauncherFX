/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 *
 * @author skuri
 */
public class LaunchItemPane extends BorderPane {
    
    private final Button launchButton;
    private final Text descriptionArea;
    
    public LaunchItemPane() {
        launchButton = new Button("button");
        descriptionArea = new Text("label");
        layoutPane();
    }
    
    public LaunchItemPane(String name, String description, String cmd, String img) throws MalformedURLException {
        launchButton = new Button(name);
        if(img != null && Files.exists(Paths.get(img))) {
            launchButton.setText(null);
            
            ImageView icon = new ImageView(Paths.get(img).toUri().toURL().toString());
            icon.setPreserveRatio(true);
            icon.setFitHeight(150);
            icon.setFitWidth(150);
            
            launchButton.setGraphic(icon);
        }
        descriptionArea = new Text(description);
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
//        descriptionArea.setMinSize(420, 125);
//        descriptionArea.setMaxSize(420, 125);

        setLeft(launchButton);
        setCenter(descriptionArea);
        
        setMargin(launchButton, new Insets(4));
        setMargin(descriptionArea, new Insets(12, 4, 4, 4));
        setAlignment(descriptionArea, Pos.TOP_LEFT);
    }
}
