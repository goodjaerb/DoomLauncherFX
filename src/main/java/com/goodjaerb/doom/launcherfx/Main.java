/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx;

/**
 * @author goodjaerb
 */
public class Main {

    public static void main(String[] args) {
        // Workaround because JDK11+ javafx applications must be modular but gradle
        // puts the javafx jars in the classpath, and java doesn't like that.
        // This makes the Main class not extend javafx, and legally calls the javafx main method.
        LauncherFX.main(args);
    }
}
