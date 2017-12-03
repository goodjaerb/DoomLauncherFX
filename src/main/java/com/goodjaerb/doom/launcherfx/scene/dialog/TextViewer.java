/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class TextViewer extends Dialog<Void> {
    
    public TextViewer(Path txtPath) throws IOException {
        BufferedReader reader = Files.newBufferedReader(txtPath);
        
        String line;
        String text = "";
        while((line = reader.readLine()) != null) {
            text += line + "\n";
        }
        
        TextArea textArea = new TextArea(text);
        textArea.setFont(new Font("Lucida Console", 12));
        textArea.setMinSize(650, 600);
        getDialogPane().setContent(textArea);
        
        ButtonType closeButtonType = new ButtonType("Close", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(closeButtonType);
        getDialogPane().lookupButton(closeButtonType).setDisable(false);
    }
}
