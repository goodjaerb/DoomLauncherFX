/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control.list;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author goodjaerb<goodjaerb @ gmail.com>
 */
public class PWadListItem {
    public static final PWadListItem NO_PWAD = new PWadListItem(PWadListItem.Type.WAD, "No PWAD.", null, null);

    public enum Type {
        WAD, TXT, DEH
    }

    public final Type   type;
    public       String display;
    public final Path   path;
    public       String txt;
    public       String warp;
    public       String args;

    public PWadListItem(Type type, String display, Path path, String txt) {
        this.type = type;
        this.display = display;
        this.path = path;
        this.warp = "";
        this.txt = txt;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final PWadListItem other = (PWadListItem) obj;
        return Objects.equals(this.path, other.path);
    }

    @Override
    public String toString() {
        return display;
    }
}
