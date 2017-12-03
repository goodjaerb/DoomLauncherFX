/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control.list;

import javafx.scene.control.ListCell;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class WarpListCell extends ListCell<WarpListItem> {

    @Override
    protected void updateItem(WarpListItem item, boolean empty) {
        super.updateItem(item, empty);

        if(!empty) {
            setStyle(null);
            if(item != null && item.highlight) {
                setStyle("-fx-font-weight: bold;");
            }
        }
        else {
            setStyle("-fx-font-weight: normal;");
        }
        setText(item == null ? null : item.display);
    }
}
