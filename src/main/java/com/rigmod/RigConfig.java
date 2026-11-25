package com.rigmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class RigConfig {
    /** With the rig on, these numbers LOOK dropped (empty) in the host dropper ("N H" items). */
    public int hostMin = 7;
    public int hostMax = 9;

    /** With the rig on, these numbers LOOK dropped (empty) in the viewer dropper ("N V" items). */
    public int viewerMin = 1;
    public int viewerMax = 6;

    /** Fallback look for a dropped item when no snapshot of the real one exists. */
    public String dropItem = "minecraft:paper";
    public String dropModel = "";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static RigConfig INSTANCE = new RigConfig();

    private static int clamp(int v) {
        return Math.max(1, Math.min(9, v));
    }

    /** {lo, hi} of the numbers that look dropped for this side ('H' or 'V'). */
    public static int[] range(char side) {
        int lo = clamp(side == 'V' ? INSTANCE.viewerMin : INSTANCE.hostMin);
        int hi = clamp(side == 'V' ? INSTANCE.viewerMax : INSTANCE.hostMax);
        if (lo > hi) {
            int t = lo;
            lo = hi;
            hi = t;
        }
        return new int[] {lo, hi};
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("rigmod.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                RigConfig c = GSON.fromJson(Files.readString(p), RigConfig.class);
                if (c != null) INSTANCE = c;
            } else {
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(INSTANCE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
