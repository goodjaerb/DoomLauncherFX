/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author goodjaerb<goodjaerb @ gmail.com>
 */
public final class LaunchButton extends Button {

    private final Label     label         = new Label();
    private final ImageView icon          = new ImageView();
    private final ImageView checkmarkView = new ImageView("images/checkmark.png");

    LaunchButton(String imgPathStr) {
        super();
        label.setMaxWidth(175);
        label.setAlignment(Pos.CENTER);
        label.textProperty().bind(textProperty());

        icon.setPreserveRatio(true);
        icon.setFitHeight(150);
        icon.setFitWidth(150);
        setIcon(imgPathStr);

        checkmarkView.setPreserveRatio(true);
        checkmarkView.setFitHeight(150);
        checkmarkView.setFitWidth(150);
        checkmarkView.setVisible(false);

        StackPane graphicStack = new StackPane();
        graphicStack.getChildren().add(label);
        graphicStack.getChildren().add(icon);
        graphicStack.getChildren().add(checkmarkView);

        setGraphic(graphicStack);
    }


    void setIcon(String imgPathStr) {
        if(imgPathStr == null || !Files.exists(Paths.get(imgPathStr))) {
            icon.setImage(null);
            label.setVisible(true);
        }
        else {
            try {
                icon.setImage(new Image(Paths.get(imgPathStr).toUri().toURL().toString()));
                label.setVisible(false);
            }
            catch(MalformedURLException ex) {
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

    void setCheckmarkVisible(boolean b) {
        checkmarkView.setVisible(b);
    }
}
