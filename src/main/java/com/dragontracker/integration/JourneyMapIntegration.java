package com.dragontracker.integration;

import com.dragontracker.dragon.DragonInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class JourneyMapIntegration {
    
    private static boolean journeyMapLoaded = false;
    
    public static void init() {
        try {
            Class.forName("journeymap.client.api.ClientPlugin");
            journeyMapLoaded = true;
            System.out.println("Dragon Tracker: JourneyMap integration enabled");
        } catch (ClassNotFoundException e) {
            journeyMapLoaded = false;
            System.out.println("Dragon Tracker: JourneyMap not found, waypoint integration disabled");
        }
    }
    
    public static void addWaypoint(DragonInfo dragon) {
        if (!journeyMapLoaded) {
            Minecraft.getInstance().player.sendSystemMessage(
                Component.literal("JourneyMap not installed - cannot add waypoint"));
            return;
        }
        
        try {
            addJourneyMapWaypoint(dragon);
            Minecraft.getInstance().player.sendSystemMessage(
                Component.literal("Added dragon waypoint to JourneyMap: " + dragon.getDisplayName()));
        } catch (Exception e) {
            System.err.println("Failed to add JourneyMap waypoint: " + e.getMessage());
            Minecraft.getInstance().player.sendSystemMessage(
                Component.literal("Failed to add waypoint to JourneyMap"));
        }
    }
    
    private static void addJourneyMapWaypoint(DragonInfo dragon) {
        // This method uses reflection to call JourneyMap API
        // Since we can't guarantee JourneyMap classes are available at compile time
        try {
            Class<?> clientApiClass = Class.forName("journeymap.client.api.ClientPlugin");
            Object clientApi = clientApiClass.getMethod("getInstance").invoke(null);
            
            if (clientApi == null) {
                throw new RuntimeException("JourneyMap ClientPlugin not initialized");
            }
            
            // Create waypoint
            Class<?> waypointClass = Class.forName("journeymap.client.api.model.Waypoint");
            Class<?> mapImageClass = Class.forName("journeymap.client.api.model.MapImage");
            
            Object waypoint = waypointClass.getConstructor(
                String.class, // modId
                String.class, // name  
                int.class,    // dimension
                int.class,    // x
                int.class,    // y
                int.class,    // z
                int.class,    // color
                mapImageClass, // icon
                boolean.class  // persistent
            ).newInstance(
                "dragontracker",
                dragon.getDisplayName(),
                Minecraft.getInstance().level.dimension().location().hashCode(),
                dragon.getPosition().getX(),
                dragon.getPosition().getY(), 
                dragon.getPosition().getZ(),
                getDragonColor(dragon.getDragonType()),
                getMapImage(dragon.getDragonType()),
                true
            );
            
            // Add waypoint using API
            clientApiClass.getMethod("createWaypoint", waypointClass).invoke(clientApi, waypoint);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JourneyMap waypoint", e);
        }
    }
    
    private static int getDragonColor(String dragonType) {
        switch (dragonType.toLowerCase()) {
            case "fire": return 0xFF4444; // Red
            case "ice": return 0x4444FF;  // Blue  
            case "lightning": return 0xFFFF44; // Yellow
            default: return 0x44FF44; // Green
        }
    }
    
    private static Object getMapImage(String dragonType) {
        try {
            Class<?> mapImageClass = Class.forName("journeymap.client.api.model.MapImage");
            
            // Try to use a dragon-themed icon, fallback to default
            try {
                return mapImageClass.getField("DRAGON").get(null);
            } catch (Exception e) {
                // Fallback to diamond icon if dragon icon doesn't exist
                return mapImageClass.getField("DIAMOND").get(null);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean isJourneyMapLoaded() {
        return journeyMapLoaded;
    }
}