/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public enum Field {
    NAME,
    DESC,
    TYPE,
    IWAD,
    FILE,
    WARP,
    GAME,
    SKIPWADS,
    WADFOLDER,
    PORT,
    CMD,
    ARGS,
    IMG,
    AUTHOR,
    TXT,
    IGNORE;

    String iniKey() {
        return name().toLowerCase();
    }
}
