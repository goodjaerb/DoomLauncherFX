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
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author goodjaerb
 */
public class LaunchItemPane extends BorderPane {

    public final IniConfigurableItem configurableItem;

    private final LaunchButton launchButton;
    private final Text nameLabel;
    private final Text versionLabel;
    private final Text descriptionArea;
    private final Hyperlink hyperLink;

    public LaunchItemPane(IniConfigurableItem item, EventHandler<ActionEvent> handler, EventHandler<ActionEvent> editHandler) {
        launchButton = new LaunchButton(LauncherFX.resolvePathRelativeToConfig(item.get(Field.IMG), Config.DIR_IMAGES));
        launchButton.textProperty().bind(item.valueProperty(Field.NAME));
        launchButton.addEventHandler(ActionEvent.ACTION, handler);
        
        MenuItem editItem = new MenuItem("Edit");
        editItem.addEventHandler(ActionEvent.ACTION, editHandler);
        launchButton.setContextMenu(new ContextMenu(editItem));

        configurableItem = item;
        configurableItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            launchButton.setCheckmarkVisible(newValue);
        });
        
        configurableItem.enabledProperty().addListener((observable, oldValue, newValue) -> {
            launchButton.setDisable(!newValue);
        });

        configurableItem.valueProperty(Field.IMG).addListener((observable, oldValue, newValue) -> {
            launchButton.setIcon(LauncherFX.resolvePathRelativeToConfig(newValue, Config.DIR_IMAGES));
        });

        nameLabel = new Text();
        nameLabel.textProperty().bind(item.valueProperty(Field.NAME));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px");

        versionLabel = new Text();
        versionLabel.textProperty().bind(item.valueProperty(Field.VERSION));
        versionLabel.setStyle("-fx-font-weight: bold; -fx-font-style: italic; -fx-font-size: 14px");
        
        descriptionArea = new Text();
        descriptionArea.textProperty().bind(item.valueProperty(Field.DESC));
        
        hyperLink = new Hyperlink();
        hyperLink.textProperty().bind(item.valueProperty(Field.HTTP));
        hyperLink.addEventHandler(ActionEvent.ACTION, (event) -> {
            try {
                Desktop.getDesktop().browse(new URI(hyperLink.getText()));
            } catch (URISyntaxException | IOException ex) {
                Logger.getLogger(LaunchItemPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

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

        TextFlow textFlow = new TextFlow(nameLabel, new Text("  "), versionLabel);
        
        VBox vBox = new VBox();
        vBox.setSpacing(2);
        vBox.getChildren().add(textFlow);
        vBox.getChildren().add(descriptionArea);
        vBox.getChildren().add(hyperLink);

        setLeft(launchButton);
        setCenter(vBox);

        setMargin(launchButton, new Insets(4));
        setMargin(vBox, new Insets(4, 4, 4, 0));
    }
}
