/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control.list;

import com.goodjaerb.doom.launcherfx.LauncherFX;
import com.goodjaerb.doom.launcherfx.scene.dialog.TextViewer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class PWadListCell extends ListCell<PWadListItem> {
    
    public PWadListCell() {
        addEventHandler(MouseEvent.MOUSE_CLICKED, (javafx.scene.input.MouseEvent event) -> {
            PWadListItem item = getItem();
            if (item != null && (item.type == PWadListItem.Type.TXT || item.txt != null) && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                try {
                    if(item.type == PWadListItem.Type.TXT) {
                        new TextViewer(item.path).showAndWait();
                    }
                    else if(item.txt != null) {
                        new TextViewer(item.path.getParent().resolve(item.txt)).showAndWait();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(LauncherFX.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    protected void updateItem(PWadListItem item, boolean empty) {
        super.updateItem(item, empty);
        setText(item == null ? null : item.display);
    }
    
}
