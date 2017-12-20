/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control;

import com.goodjaerb.doom.launcherfx.LauncherFX;
import com.goodjaerb.doom.launcherfx.config.Config;
import com.goodjaerb.doom.launcherfx.config.Field;
import com.goodjaerb.doom.launcherfx.config.IniConfigurableItem;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 *
 * @author goodjaerb
 */
public class LaunchItemPane extends BorderPane {

    public final IniConfigurableItem configurableItem;

    private final LaunchButton launchButton;
    private final Label nameLabel;
    private final Text descriptionArea;

    public LaunchItemPane(IniConfigurableItem item, EventHandler<ActionEvent> handler) {
        launchButton = new LaunchButton(LauncherFX.getAbsolutePath(item.get(Field.IMG), Config.DIR_IMAGES));
        launchButton.textProperty().bind(item.valueProperty(Field.NAME));
        launchButton.addEventHandler(ActionEvent.ACTION, handler);

        configurableItem = item;
        configurableItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            launchButton.setCheckmarkVisible(newValue);
        });
        
        configurableItem.enabledProperty().addListener((observable, oldValue, newValue) -> {
            launchButton.setDisable(!newValue);
        });

        configurableItem.valueProperty(Field.IMG).addListener((observable, oldValue, newValue) -> {
            launchButton.setIcon(LauncherFX.getAbsolutePath(newValue, Config.DIR_IMAGES));
        });

        nameLabel = new Label();
        nameLabel.textProperty().bind(item.valueProperty(Field.NAME));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px");

        descriptionArea = new Text();
        descriptionArea.textProperty().bind(item.valueProperty(Field.DESC));

        layoutPane();
    }

    public LaunchButton getLaunchButton() {
        return launchButton;
    }

    public void setSelected(boolean b) {
        configurableItem.selectedProperty().set(b);
    }

    public void setEnabled(boolean b) {
        configurableItem.enabledProperty().set(b);
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
}
