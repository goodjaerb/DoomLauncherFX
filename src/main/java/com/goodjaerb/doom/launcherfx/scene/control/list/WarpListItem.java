/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.scene.control.list;

import java.util.Objects;

/**
 * @author goodjaerb<goodjaerb @ gmail.com>
 */
public class WarpListItem implements Comparable<WarpListItem> {
    public static final WarpListItem DO_NOT_WARP = new WarpListItem("Do not warp.", null);

    public final String display;
    public final String arg;
    public boolean highlight;

    public WarpListItem(String display, String arg) {
        this.display = display;
        this.arg = arg;
    }

    @Override
    public int compareTo(WarpListItem other) {
        if(this == DO_NOT_WARP) {
            return -1;
        }
        if(other == DO_NOT_WARP) {
            return 1;
        }
        return this.display.compareToIgnoreCase(other.display);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.display);
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
        final WarpListItem other = (WarpListItem) obj;
        return Objects.equals(this.display, other.display);
    }

    @Override
    public String toString() {
        return display;
    }
}
