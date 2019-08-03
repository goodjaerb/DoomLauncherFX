/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.dialog;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.Modality;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * @author goodjaerb<goodjaerb @ gmail.com>
 */
public class TextViewer extends Dialog<Void> {

    public TextViewer(Path txtPath) throws IOException {
//        BufferedReader reader = Files.newBufferedReader(txtPath); //this causes malformedinput exceptions
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(txtPath.toFile()), "utf-8"));

        String line;
        String text = "";
        while((line = reader.readLine()) != null) {
            text += line + "\n";
        }

        TextArea textArea = new TextArea(text);
        textArea.setFont(new Font("Lucida Console", 12));
        textArea.setMinSize(650, 600);
        getDialogPane().setContent(textArea);

        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        initModality(Modality.NONE);
    }
}
