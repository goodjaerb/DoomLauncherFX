/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.config.input;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public abstract class Input {
    public enum Type {
        TEXT, BROWSE, LIST, MULTI_LIST, HIDDEN, BOOLEAN;
    }
    
    public final Type type;
    
    Input(Type type) {
        this.type = type;
    }
}
