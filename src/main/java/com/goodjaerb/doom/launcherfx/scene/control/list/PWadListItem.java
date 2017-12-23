/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control.list;

import java.nio.file.Path;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class PWadListItem implements Comparable<PWadListItem> {
    public static final PWadListItem NO_PWAD = new PWadListItem(PWadListItem.Type.WAD, "No PWAD.", null, null);
    
    public enum Type {
        WAD, TXT, DEH;
    }
    public final Type type;
    public String display;
    public final Path path;
    public String txt;
    public String warp;
    public String args;

    public PWadListItem(Type type, String display, Path path, String txt) {
        this.type = type;
        this.display = display;
        this.path = path;
        this.warp = "";
        this.txt = txt;
    }

    @Override
    public int compareTo(PWadListItem other) {
        if (this == NO_PWAD) {
            return -1;
        }
        if (other == NO_PWAD) {
            return 1;
        }
        return this.display.compareToIgnoreCase(other.display);
    }
    
    @Override
    public String toString() {
        return display;
    }
}
