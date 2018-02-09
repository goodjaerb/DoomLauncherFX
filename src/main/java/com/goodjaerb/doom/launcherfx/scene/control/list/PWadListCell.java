/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control.list;

import com.goodjaerb.doom.launcherfx.LauncherFX;
import com.goodjaerb.doom.launcherfx.scene.dialog.TextViewer;
import java.io.IOException;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class PWadListCell extends ListCell<PWadListItem> {
    
    public PWadListCell(ContextMenu contextMenu) {
        addEventHandler(MouseEvent.MOUSE_CLICKED, (javafx.scene.input.MouseEvent event) -> {
            PWadListItem item = getItem();
            if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                if (item != null && (item.type == PWadListItem.Type.TXT || item.txt != null)) {
                    try {
                        if(item.type == PWadListItem.Type.TXT) {
                            new TextViewer(item.path).show();
                        }
                        else if(item.txt != null) {
                            new TextViewer(item.path.getParent().resolve(item.txt)).show();
                        }
                    } catch (IOException ex) {
                        LauncherFX.error(ex);
                    }
                }
            }
        });
        
        setContextMenu(contextMenu);
    }

    @Override
    protected void updateItem(PWadListItem item, boolean empty) {
        super.updateItem(item, empty);
        setText(item == null ? null : item.display);
    }
}
