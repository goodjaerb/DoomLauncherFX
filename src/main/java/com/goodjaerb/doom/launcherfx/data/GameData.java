/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.data;

import com.goodjaerb.doom.launcherfx.scene.control.list.WarpListItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
final class GameData {
    
    static final List<String> DOOM_SKILL_LIST = 
            Collections.unmodifiableList(Arrays.asList(
                    "I'm Too Young To Die",
                    "Hey, Not Too Rough",
                    "Hurt Me Plenty",
                    "Ultra-Violence",
                    "Nightmare!"));
    
    static final List<String> HERETIC_SKILL_LIST = 
            Collections.unmodifiableList(Arrays.asList(
                    "Thou Needeth a Wet-Nurse",
                    "Yellowbellies-R-Us",
                    "Bringest Them Oneth",
                    "Thou Art a Smite-Meister",
                    "Black Plague Possesses Thee"));
    
    static final List<WarpListItem> DOOM_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(WarpListItem.DO_NOT_WARP,
                    new WarpListItem("E1M1", "1 1"), 
                    new WarpListItem("E1M2", "1 2"), 
                    new WarpListItem("E1M3", "1 3"), 
                    new WarpListItem("E1M4", "1 4"), 
                    new WarpListItem("E1M5", "1 5"), 
                    new WarpListItem("E1M6", "1 6"), 
                    new WarpListItem("E1M7", "1 7"), 
                    new WarpListItem("E1M8", "1 8"), 
                    new WarpListItem("E1M9", "1 9"), 
                    
                    new WarpListItem("E2M1", "2 1"), 
                    new WarpListItem("E2M2", "2 2"), 
                    new WarpListItem("E2M3", "2 3"), 
                    new WarpListItem("E2M4", "2 4"), 
                    new WarpListItem("E2M5", "2 5"), 
                    new WarpListItem("E2M6", "2 6"), 
                    new WarpListItem("E2M7", "2 7"), 
                    new WarpListItem("E2M8", "2 8"), 
                    new WarpListItem("E2M9", "2 9"), 
                    
                    new WarpListItem("E3M1", "3 1"), 
                    new WarpListItem("E3M2", "3 2"), 
                    new WarpListItem("E3M3", "3 3"), 
                    new WarpListItem("E3M4", "3 4"), 
                    new WarpListItem("E3M5", "3 5"), 
                    new WarpListItem("E3M6", "3 6"), 
                    new WarpListItem("E3M7", "3 7"), 
                    new WarpListItem("E3M8", "3 8"), 
                    new WarpListItem("E3M9", "3 9")));
    
    static final List<WarpListItem> ULTIMATE_DOOM_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(WarpListItem.DO_NOT_WARP,
                    new WarpListItem("E1M1", "1 1"), 
                    new WarpListItem("E1M2", "1 2"), 
                    new WarpListItem("E1M3", "1 3"), 
                    new WarpListItem("E1M4", "1 4"), 
                    new WarpListItem("E1M5", "1 5"), 
                    new WarpListItem("E1M6", "1 6"), 
                    new WarpListItem("E1M7", "1 7"), 
                    new WarpListItem("E1M8", "1 8"), 
                    new WarpListItem("E1M9", "1 9"), 
                    
                    new WarpListItem("E2M1", "2 1"), 
                    new WarpListItem("E2M2", "2 2"), 
                    new WarpListItem("E2M3", "2 3"), 
                    new WarpListItem("E2M4", "2 4"), 
                    new WarpListItem("E2M5", "2 5"), 
                    new WarpListItem("E2M6", "2 6"), 
                    new WarpListItem("E2M7", "2 7"), 
                    new WarpListItem("E2M8", "2 8"), 
                    new WarpListItem("E2M9", "2 9"), 
                    
                    new WarpListItem("E3M1", "3 1"), 
                    new WarpListItem("E3M2", "3 2"), 
                    new WarpListItem("E3M3", "3 3"), 
                    new WarpListItem("E3M4", "3 4"), 
                    new WarpListItem("E3M5", "3 5"), 
                    new WarpListItem("E3M6", "3 6"), 
                    new WarpListItem("E3M7", "3 7"), 
                    new WarpListItem("E3M8", "3 8"), 
                    new WarpListItem("E3M9", "3 9"), 
                    
                    new WarpListItem("E4M1", "4 1"), 
                    new WarpListItem("E4M2", "4 2"), 
                    new WarpListItem("E4M3", "4 3"), 
                    new WarpListItem("E4M4", "4 4"), 
                    new WarpListItem("E4M5", "4 5"), 
                    new WarpListItem("E4M6", "4 6"), 
                    new WarpListItem("E4M7", "4 7"), 
                    new WarpListItem("E4M8", "4 8"), 
                    new WarpListItem("E4M9", "4 9")));
    
    static final List<WarpListItem> DOOM2_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(WarpListItem.DO_NOT_WARP,
                    new WarpListItem("MAP01", "1"),
                    new WarpListItem("MAP02", "2"),
                    new WarpListItem("MAP03", "3"),
                    new WarpListItem("MAP04", "4"),
                    new WarpListItem("MAP05", "5"),
                    new WarpListItem("MAP06", "6"),
                    new WarpListItem("MAP07", "7"),
                    new WarpListItem("MAP08", "8"),
                    new WarpListItem("MAP09", "9"),
                    new WarpListItem("MAP10", "10"),
                    new WarpListItem("MAP11", "11"),
                    new WarpListItem("MAP12", "12"),
                    new WarpListItem("MAP13", "13"),
                    new WarpListItem("MAP14", "14"),
                    new WarpListItem("MAP15", "15"),
                    new WarpListItem("MAP16", "16"),
                    new WarpListItem("MAP17", "17"),
                    new WarpListItem("MAP18", "18"),
                    new WarpListItem("MAP19", "19"),
                    new WarpListItem("MAP20", "20"),
                    new WarpListItem("MAP21", "21"),
                    new WarpListItem("MAP22", "22"),
                    new WarpListItem("MAP23", "23"),
                    new WarpListItem("MAP24", "24"),
                    new WarpListItem("MAP25", "25"),
                    new WarpListItem("MAP26", "26"),
                    new WarpListItem("MAP27", "27"),
                    new WarpListItem("MAP28", "28"),
                    new WarpListItem("MAP29", "29"),
                    new WarpListItem("MAP30", "30"),
                    new WarpListItem("MAP31", "31"),
                    new WarpListItem("MAP32", "32")));
    
    static final List<WarpListItem> HERETIC_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(WarpListItem.DO_NOT_WARP,
                    new WarpListItem("E1M1", "1 1"), 
                    new WarpListItem("E1M2", "1 2"), 
                    new WarpListItem("E1M3", "1 3"), 
                    new WarpListItem("E1M4", "1 4"), 
                    new WarpListItem("E1M5", "1 5"), 
                    new WarpListItem("E1M6", "1 6"), 
                    new WarpListItem("E1M7", "1 7"), 
                    new WarpListItem("E1M8", "1 8"), 
                    new WarpListItem("E1M9", "1 9"), 
                    
                    new WarpListItem("E2M1", "2 1"), 
                    new WarpListItem("E2M2", "2 2"), 
                    new WarpListItem("E2M3", "2 3"), 
                    new WarpListItem("E2M4", "2 4"), 
                    new WarpListItem("E2M5", "2 5"), 
                    new WarpListItem("E2M6", "2 6"), 
                    new WarpListItem("E2M7", "2 7"), 
                    new WarpListItem("E2M8", "2 8"), 
                    new WarpListItem("E2M9", "2 9"), 
                    
                    new WarpListItem("E3M1", "3 1"), 
                    new WarpListItem("E3M2", "3 2"), 
                    new WarpListItem("E3M3", "3 3"), 
                    new WarpListItem("E3M4", "3 4"), 
                    new WarpListItem("E3M5", "3 5"), 
                    new WarpListItem("E3M6", "3 6"), 
                    new WarpListItem("E3M7", "3 7"), 
                    new WarpListItem("E3M8", "3 8"), 
                    new WarpListItem("E3M9", "3 9")));
    
    static final List<WarpListItem> HERETIC_EXP_WARP_LIST = 
            Collections.unmodifiableList(Arrays.asList(WarpListItem.DO_NOT_WARP,
                    new WarpListItem("E1M1", "1 1"), 
                    new WarpListItem("E1M2", "1 2"), 
                    new WarpListItem("E1M3", "1 3"), 
                    new WarpListItem("E1M4", "1 4"), 
                    new WarpListItem("E1M5", "1 5"), 
                    new WarpListItem("E1M6", "1 6"), 
                    new WarpListItem("E1M7", "1 7"), 
                    new WarpListItem("E1M8", "1 8"), 
                    new WarpListItem("E1M9", "1 9"), 
                    
                    new WarpListItem("E2M1", "2 1"), 
                    new WarpListItem("E2M2", "2 2"), 
                    new WarpListItem("E2M3", "2 3"), 
                    new WarpListItem("E2M4", "2 4"), 
                    new WarpListItem("E2M5", "2 5"), 
                    new WarpListItem("E2M6", "2 6"), 
                    new WarpListItem("E2M7", "2 7"), 
                    new WarpListItem("E2M8", "2 8"), 
                    new WarpListItem("E2M9", "2 9"), 
                    
                    new WarpListItem("E3M1", "3 1"), 
                    new WarpListItem("E3M2", "3 2"), 
                    new WarpListItem("E3M3", "3 3"), 
                    new WarpListItem("E3M4", "3 4"), 
                    new WarpListItem("E3M5", "3 5"), 
                    new WarpListItem("E3M6", "3 6"), 
                    new WarpListItem("E3M7", "3 7"), 
                    new WarpListItem("E3M8", "3 8"), 
                    new WarpListItem("E3M9", "3 9"),
                    
                    new WarpListItem("E4M1", "4 1"), 
                    new WarpListItem("E4M2", "4 2"), 
                    new WarpListItem("E4M3", "4 3"), 
                    new WarpListItem("E4M4", "4 4"), 
                    new WarpListItem("E4M5", "4 5"), 
                    new WarpListItem("E4M6", "4 6"), 
                    new WarpListItem("E4M7", "4 7"), 
                    new WarpListItem("E4M8", "4 8"), 
                    new WarpListItem("E4M9", "4 9"),
                    
                    new WarpListItem("E5M1", "5 1"), 
                    new WarpListItem("E5M2", "5 2"), 
                    new WarpListItem("E5M3", "5 3"), 
                    new WarpListItem("E5M4", "5 4"), 
                    new WarpListItem("E5M5", "5 5"), 
                    new WarpListItem("E5M6", "5 6"), 
                    new WarpListItem("E5M7", "5 7"), 
                    new WarpListItem("E5M8", "5 8"), 
                    new WarpListItem("E5M9", "5 9"),
                    
                    new WarpListItem("E6M1", "6 1"), 
                    new WarpListItem("E6M2", "6 2"), 
                    new WarpListItem("E6M3", "6 3")));
}
