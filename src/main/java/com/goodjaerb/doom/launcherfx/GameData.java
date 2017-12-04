/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx;

import com.goodjaerb.doom.launcherfx.scene.control.list.WarpListItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 *
 * @author goodjaerb<goodjaerb@gmail.com>
 */
public class GameData {
    public final String wadfolder;
    public final List<String> skillList;
    public final List<WarpListItem> warpList;
    
    private GameData(String wadfolder, List<String> skillList, List<WarpListItem> warpList) {
        this.wadfolder = wadfolder;
        this.skillList = skillList;
        this.warpList = warpList;
    }
    
    public static GameData getGameData(String iwadPath) {
        try {
            String sha = calcSHA1(new File(iwadPath));
            for(Map.Entry<List<String>, GameData> entry : SHA_GAME_MAPPINGS.entrySet()) {
                if(entry.getKey().contains(sha)) {
                    return entry.getValue();
                }
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(GameData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return UNKNOWN_GAME_DATA;
    }
    
    /**
    * Read the file and calculate the SHA-1 checksum
    * https://stackoverflow.com/questions/6293713/java-how-to-create-sha-1-for-a-file
    * 
    * @param file
    *            the file to read
    * @return the hex representation of the SHA-1 using *lowercase* chars
    * @throws FileNotFoundException
    *             if the file does not exist, is a directory rather than a
    *             regular file, or for some other reason cannot be opened for
    *             reading
    * @throws IOException
    *             if an I/O error occurs
    * @throws NoSuchAlgorithmException
    *             should never happen
    */
    private static String calcSHA1(File file) throws FileNotFoundException,
            IOException, NoSuchAlgorithmException {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return new HexBinaryAdapter().marshal(sha1.digest()).toLowerCase();
        }
    }
    
    private static final List<String> DOOM_SKILL_LIST = 
            Collections.unmodifiableList(Arrays.asList(
                    "I'm Too Young To Die",
                    "Hey, Not Too Rough",
                    "Hurt Me Plenty",
                    "Ultra-Violence",
                    "Nightmare!"));
    
    private static final List<String> HERETIC_SKILL_LIST = 
            Collections.unmodifiableList(Arrays.asList(
                    "Thou Needeth a Wet-Nurse",
                    "Yellowbellies-R-Us",
                    "Bringest Them Oneth",
                    "Thou Art a Smite-Meister",
                    "Black Plague Possesses Thee"));
    
    private static final List<WarpListItem> DOOM_WARP_LIST = 
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
    
    private static final List<WarpListItem> ULTIMATE_DOOM_WARP_LIST = 
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
    
    private static final List<WarpListItem> DOOM2_WARP_LIST = 
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
    
    private static final List<WarpListItem> HERETIC_WARP_LIST = 
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
    
    private static final List<WarpListItem> HERETIC_EXP_WARP_LIST = 
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
    
    private static final List<String> DOOM_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "7742089b4468a736cadb659a7deca3320fe6dcbd", // Doom Version 1.9
            "2c8212631b37f21ad06d18b5638c733a75e179ff", // Doom Version 1.8
            "2e89b86859acd9fc1e552f587b710751efcffa8e", // Doom Version 1.666
            "b5f86a559642a2b3bdfb8a75e91c8da97f057fe6", // Doom Version 1.2
            "df0040ccb29cc1622e74ceb3b7793a2304cca2c8")); // Doom Version 1.1
    
    private static final List<String> ULTIMATE_DOOM_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "9b07b02ab3c275a6a7570c3f73cc20d63a0e3833", // Doom Version 1.9ud
            "e5ec79505530e151ff0e6f517f3ce1fd65969c46")); // Doom BFG Edition
    
    private static final List<String> DOOM2_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "7ec7652fcfce8ddc6e801839291f0e28ef1d5ae7", // Doom 2 Version 1.9
            "a59548125f59f6aa1a41c22f615557d3dd2e85a9", // Doom 2 BFG Edition
            "d510c877031bbd5f3d198581a2c8651e09b9861f", // Doom 2 Version 1.8f
            "79c283b18e61b9a989cfd3e0f19a42ea98fda551", // Doom 2 Version 1.8
            "70192b8d5aba65c7e633a7c7bcfe7e3e90640c97", // Doom 2 Version 1.7a
            "78009057420b792eacff482021db6fe13b370dcc", // Doom 2 Version 1.7
            "6d559b7ceece4f5ad457415049711992370d520a", // Doom 2 Version 1.666
            "a4ce5128d57cb129fdd1441c12b58245be55c8ce")); // Doom 2 Version 1.666g
    
    private static final List<String> HERETIC_EXP_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "f489d479371df32f6d280a0cb23b59a35ba2b833")); // Heretic Version 1.3 (Shadow of the Serpent Riders)
    
    private static final List<String> HERETIC_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "a54c5d30629976a649119c5ce8babae2ddfb1a60", // Heretic Version 1.2
            "b5a6cc79cde48d97905b44282e82c4c966a23a87")); // Heretic Version 1.0
    
    private static final List<String> HEXEN_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "4b53832f0733c1e29e5f1de2428e5475e891af29")); // Hexen Version 1.1
            
    private static final List<String> HEXEN_EXP_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "081f6a2024643b54ef4a436a85508539b6d20a1e", // Hexen: Deathkings of the Dark Citadel Version 1.1
            "c3065527d62b05a930fe75fe8181a64fb1982976")); // Hexen: Deathkings of the Dark Citadel Version 1.0
    
    private static final GameData UNKNOWN_GAME_DATA = new GameData("unknown", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    private static final GameData DOOM_GAME_DATA = new GameData("doom", DOOM_SKILL_LIST, DOOM_WARP_LIST);
    private static final GameData ULTIMATE_DOOM_GAME_DATA = new GameData("doom", DOOM_SKILL_LIST, ULTIMATE_DOOM_WARP_LIST);
    private static final GameData DOOM2_GAME_DATA = new GameData("doom2", DOOM_SKILL_LIST, DOOM2_WARP_LIST);
    private static final GameData HERETIC_GAME_DATA = new GameData("heretic", HERETIC_SKILL_LIST, HERETIC_WARP_LIST);
    private static final GameData HERETIC_EXP_GAME_DATA = new GameData("heretic", HERETIC_SKILL_LIST, HERETIC_EXP_WARP_LIST);
//    private static final GameData HEXEN_GAME_DATA = new GameData("hexen", HEXEN_SKILL_LIST, HEXEN_WARP_LIST);
//    private static final GameData HEXEN_EXP_GAME_DATA = new GameData("hexen", HEXEN_SKILL_LIST, HEXEN_EXP_WARP_LIST);
    
    private static final Map<List<String>, GameData> SHA_GAME_MAPPINGS = new HashMap<>();
    static {
        SHA_GAME_MAPPINGS.put(DOOM_SHA_VALUES, DOOM_GAME_DATA);
        SHA_GAME_MAPPINGS.put(ULTIMATE_DOOM_SHA_VALUES, ULTIMATE_DOOM_GAME_DATA);
        SHA_GAME_MAPPINGS.put(DOOM2_SHA_VALUES, DOOM2_GAME_DATA);
        SHA_GAME_MAPPINGS.put(HERETIC_SHA_VALUES, HERETIC_GAME_DATA);
        SHA_GAME_MAPPINGS.put(HERETIC_EXP_SHA_VALUES, HERETIC_EXP_GAME_DATA);
//        SHA_GAME_MAPPINGS.put(HEXEN_SHA_VALUES, HEXEN_GAME_DATA);
//        SHA_GAME_MAPPINGS.put(HEXEN_EXP_SHA_VALUES, HEXEN_EXP_GAME_DATA);
    }
}
