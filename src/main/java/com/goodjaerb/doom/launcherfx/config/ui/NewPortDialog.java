/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config.ui;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class NewPortDialog extends Dialog<ButtonType> {
    
    public NewPortDialog() {
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    }
}
