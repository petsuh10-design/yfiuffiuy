package com.dragontracker.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class DragonTrackerConfig {
    
    public static final ForgeConfigSpec CLIENT_SPEC;
    
    public static final ForgeConfigSpec.IntValue DETECTION_RANGE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_HIGHLIGHTING;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DISTANCE_DISPLAY;
    public static final ForgeConfigSpec.BooleanValue AUTO_ADD_TO_JOURNEYMAP;
    public static final ForgeConfigSpec.IntValue UPDATE_FREQUENCY;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SOUND_NOTIFICATIONS;
    
    static {
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        
        clientBuilder.comment("Dragon Tracker Configuration").push("dragontracker");
        
        DETECTION_RANGE = clientBuilder
            .comment("Maximum range for dragon detection (in blocks)")
            .defineInRange("detectionRange", 500, 50, 2000);
            
        ENABLE_HIGHLIGHTING = clientBuilder
            .comment("Enable visual highlighting of detected dragons")
            .define("enableHighlighting", true);
            
        ENABLE_DISTANCE_DISPLAY = clientBuilder
            .comment("Show distance to dragons in the world")
            .define("enableDistanceDisplay", true);
            
        AUTO_ADD_TO_JOURNEYMAP = clientBuilder
            .comment("Automatically add dragon waypoints to JourneyMap")
            .define("autoAddToJourneyMap", false);
            
        UPDATE_FREQUENCY = clientBuilder
            .comment("How often to scan for dragons (in ticks, 20 = 1 second)")
            .defineInRange("updateFrequency", 20, 5, 100);
            
        ENABLE_SOUND_NOTIFICATIONS = clientBuilder
            .comment("Play sound when new dragon is detected")
            .define("enableSoundNotifications", true);
        
        clientBuilder.pop();
        CLIENT_SPEC = clientBuilder.build();
    }
}