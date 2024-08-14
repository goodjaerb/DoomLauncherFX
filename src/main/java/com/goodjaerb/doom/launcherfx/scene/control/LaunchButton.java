/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control;

import com.luciad.imageio.webp.WebPReadParam;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private final ImageView checkmarkView = new ImageView(getClass().getResource("/images/checkmark.png").toExternalForm());
    private final ImageView exclamationView = new ImageView(getClass().getResource("/images/exclamation-mark.png").toExternalForm());

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

        exclamationView.setPreserveRatio(true);
        exclamationView.setFitHeight(25);
        exclamationView.setFitWidth(25);
        exclamationView.setVisible(false);

        StackPane graphicStack = new StackPane();
        graphicStack.getChildren().add(label);
        graphicStack.getChildren().add(icon);
        graphicStack.getChildren().add(checkmarkView);
        graphicStack.getChildren().add(exclamationView);
        StackPane.setAlignment(exclamationView, Pos.TOP_RIGHT);

        setGraphic(graphicStack);
    }


    void setIcon(String imgPathStr) {
        if(imgPathStr == null || !Files.exists(Paths.get(imgPathStr))) {
            icon.setImage(null);
            label.setVisible(true);
        }
        else {
            try {
                Image image;
                if(imgPathStr.toLowerCase().endsWith("webp")) {
                    ImageReader reader = ImageIO.getImageReadersByMIMEType("image/webp").next();

                    WebPReadParam readParam = new WebPReadParam();
                    readParam.setBypassFiltering(true);

                    reader.setInput(new FileImageInputStream(new File(Paths.get(imgPathStr).toString())));

                    BufferedImage bImage = reader.read(0, readParam);
                    image = SwingFXUtils.toFXImage(bImage, null);
                }
                else {
                    image = new Image(Paths.get(imgPathStr).toUri().toURL().toString());
                }
                icon.setImage(image);
                label.setVisible(false);
            }
            catch(MalformedURLException ex) {
                Logger.getLogger(LaunchItemPane.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }
        }
    }

    public void setExclamationMarkVisible(boolean b) {
        exclamationView.setVisible(b);
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
