/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public final class LaunchButton extends Button {
    
    private final ImageView icon;
    private final ImageView checkmarkView;
    private final StackPane graphicStack;
    
    public LaunchButton(String imgPathStr) {
        super();
        icon = new ImageView();
        icon.setPreserveRatio(true);
        icon.setFitHeight(150);
        icon.setFitWidth(150);
        setIcon(imgPathStr);
        
        checkmarkView = new ImageView("images/checkmark.png");
        checkmarkView.setPreserveRatio(true);
        checkmarkView.setFitHeight(150);
        checkmarkView.setFitWidth(150);
        checkmarkView.setVisible(false);
        
        Label label = new Label();
        label.textProperty().bind(textProperty());
        
        graphicStack = new StackPane();
        graphicStack.getChildren().add(label);
        graphicStack.getChildren().add(icon);
        graphicStack.getChildren().add(checkmarkView);
        
        setGraphic(graphicStack);
    }
    
    void setIcon(String imgPathStr) {
        if(imgPathStr != null && Files.exists(Paths.get(imgPathStr))) {
            try {
                icon.setImage(new Image(Paths.get(imgPathStr).toUri().toURL().toString()));
            } catch (MalformedURLException ex) {
                Logger.getLogger(LaunchItemPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public boolean isChecked() {
        return checkmarkView.isVisible();
    }
    
    public void toggleCheckmark() {
        checkmarkView.setVisible(!checkmarkView.isVisible());
    }
    
    public void setCheckmarkVisible(boolean b) {
        checkmarkView.setVisible(b);
    }
}
