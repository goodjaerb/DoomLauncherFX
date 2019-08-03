/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.goodjaerb.doom.launcherfx.data;

import com.goodjaerb.doom.launcherfx.LauncherFX;
import com.goodjaerb.doom.launcherfx.scene.control.list.WarpListItem;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author goodjaerb<goodjaerb @ gmail.com>
 */
public enum Game {
    UNKNOWN_GAME("UNKNOWN", "unknown", Collections.emptyList(), Collections.emptyList()),
    DOOM("Doom", "doom", GameData.DOOM_SKILL_LIST, GameData.DOOM_WARP_LIST),
    ULTIMATE("Ultimate Doom", "doom", GameData.DOOM_SKILL_LIST, GameData.ULTIMATE_DOOM_WARP_LIST),
    DOOM2("Doom II", "doom2", GameData.DOOM_SKILL_LIST, GameData.DOOM2_WARP_LIST),
    HERETIC("Heretic", "heretic", GameData.HERETIC_SKILL_LIST, GameData.HERETIC_WARP_LIST),
    HERETIC_EXP("Heretic: Shadow of the Serpent Riders", "heretic", GameData.HERETIC_SKILL_LIST, GameData.HERETIC_EXP_WARP_LIST);
    //HEXEN, 
    //HEXEN_EXP;

    public final String name;
    public final String wadfolder;
    public final List<String> skillList;
    public final List<WarpListItem> warpList;

    private Game(String name, String wadfolder, List<String> skillList, List<WarpListItem> warpList) {
        this.name = name;
        this.wadfolder = wadfolder;
        this.skillList = skillList;
        this.warpList = warpList;
    }

    public static Game getGameData(String iwadPath) throws IOException, NoSuchAlgorithmException {
        String sha = calcSHA1(new File(iwadPath));
        for(Map.Entry<List<String>, Game> entry : SHA_GAME_MAPPINGS.entrySet()) {
            if(entry.getKey().contains(sha)) {
                LauncherFX.info("'" + iwadPath + "' matched to " + entry.getValue().name);
                return entry.getValue();
            }
        }
        return UNKNOWN_GAME;
    }

    /**
     * Read the file and calculate the SHA-1 checksum
     * https://stackoverflow.com/questions/6293713/java-how-to-create-sha-1-for-a-file
     *
     * @param file the file to read
     * @return the hex representation of the SHA-1 using *lowercase* chars
     * @throws FileNotFoundException    if the file does not exist, is a directory rather than a
     *                                  regular file, or for some other reason cannot be opened for
     *                                  reading
     * @throws IOException              if an I/O error occurs
     * @throws NoSuchAlgorithmException should never happen
     */
    private static String calcSHA1(File file) throws FileNotFoundException,
            IOException, NoSuchAlgorithmException {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try(InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while(len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return new HexBinaryAdapter().marshal(sha1.digest()).toLowerCase();
        }
    }

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
            "a4ce5128d57cb129fdd1441c12b58245be55c8ce", // Doom 2 Version 1.666g
            "9fbc66aedef7fe3bae0986cdb9323d2b8db4c9d3", // TNT: Evilution Version 1.9
            "4a65c8b960225505187c36040b41a40b152f8f3e", // TNT: Evilution, also Version 1.9 (rare). https://doomwiki.org/wiki/TNT.WAD
            "90361e2a538d2388506657252ae41aceeb1ba360", // The Plutonia Experiment Version 1.9
            "f131cbe1946d7fddb3caec4aa258c83399c21e60")); // The Plutonia Experiment, also Version 1.9 (rare). https://doomwiki.org/wiki/PLUTONIA.WAD

    private static final List<String> HERETIC_EXP_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "f489d479371df32f6d280a0cb23b59a35ba2b833")); // Heretic Version 1.3 (Shadow of the Serpent Riders)

    private static final List<String> HERETIC_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
            "a54c5d30629976a649119c5ce8babae2ddfb1a60", // Heretic Version 1.2
            "b5a6cc79cde48d97905b44282e82c4c966a23a87")); // Heretic Version 1.0

//    private static final List<String> HEXEN_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
//            "4b53832f0733c1e29e5f1de2428e5475e891af29")); // Hexen Version 1.1
//            
//    private static final List<String> HEXEN_EXP_SHA_VALUES = Collections.unmodifiableList(Arrays.asList(
//            "081f6a2024643b54ef4a436a85508539b6d20a1e", // Hexen: Deathkings of the Dark Citadel Version 1.1
//            "c3065527d62b05a930fe75fe8181a64fb1982976")); // Hexen: Deathkings of the Dark Citadel Version 1.0

    private static final Map<List<String>, Game> SHA_GAME_MAPPINGS = new HashMap<>();

    static {
        SHA_GAME_MAPPINGS.put(DOOM_SHA_VALUES, DOOM);
        SHA_GAME_MAPPINGS.put(ULTIMATE_DOOM_SHA_VALUES, ULTIMATE);
        SHA_GAME_MAPPINGS.put(DOOM2_SHA_VALUES, DOOM2);
        SHA_GAME_MAPPINGS.put(HERETIC_SHA_VALUES, HERETIC);
        SHA_GAME_MAPPINGS.put(HERETIC_EXP_SHA_VALUES, HERETIC_EXP);
//        SHA_GAME_MAPPINGS.put(HEXEN_SHA_VALUES, HEXEN);
//        SHA_GAME_MAPPINGS.put(HEXEN_EXP_SHA_VALUES, HEXEN_EXP);
    }
}
