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
    SORT,
    IWAD,
    WARP,
    SKIPWADS,
    WADFOLDER,
    PORT,
    WORKINGDIR,
    CMD,
    ARGS,
    IMG;

    public String iniKey() {
        return name().toLowerCase();
    }
}
