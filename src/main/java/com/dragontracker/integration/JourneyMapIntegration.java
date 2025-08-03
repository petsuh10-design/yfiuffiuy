package com.dragontracker.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;

/**
 * Integration with JourneyMap mod for waypoint creation
 */
public class JourneyMapIntegration {
    
    private static boolean journeyMapAvailable = false;
    private static boolean checkedAvailability = false;
    
    /**
     * Check if JourneyMap is available and can be used
     */
    private static boolean isJourneyMapAvailable() {
        if (!checkedAvailability) {
            try {
                // Try to find JourneyMap classes using reflection
                Class.forName("journeymap.client.api.ClientPlugin");
                Class.forName("journeymap.client.api.display.Waypoint");
                journeyMapAvailable = true;
                System.out.println("JourneyMap integration available!");
            } catch (ClassNotFoundException e) {
                journeyMapAvailable = false;
                System.out.println("JourneyMap not found - waypoint features disabled");
            }
            checkedAvailability = true;
        }
        return journeyMapAvailable;
    }
    
    /**
     * Add a waypoint for a dragon using JourneyMap
     * @param name The name of the waypoint
     * @param pos The position of the dragon
     * @return true if waypoint was successfully added, false otherwise
     */
    public static boolean addWaypoint(String name, BlockPos pos) {
        if (!isJourneyMapAvailable()) {
            return false;
        }
        
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return false;
            }
            
            // Use reflection to create waypoint without compile-time dependency
            Class<?> waypointClass = Class.forName("journeymap.client.api.display.Waypoint");
            Class<?> clientPluginClass = Class.forName("journeymap.client.api.ClientPlugin");
            
            // Get the ClientPlugin instance
            Object clientPlugin = clientPluginClass.getMethod("getInstance").invoke(null);
            
            // Create waypoint constructor parameters
            String modId = "dragontracker";
            String waypointId = "dragon_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
            String displayName = name;
            
            // Get dimension
            Level level = mc.level;
            Object dimension = level.dimension();
            
            // Create waypoint using reflection
            Object waypoint = waypointClass.getConstructor(
                String.class,    // modId
                String.class,    // id
                String.class,    // name
                Object.class,    // dimension (ResourceKey<Level>)
                BlockPos.class   // pos
            ).newInstance(modId, waypointId, displayName, dimension, pos);
            
            // Set waypoint properties using reflection
            try {
                // Set color to red for fire dragons, blue for ice, yellow for lightning
                int color = getWaypointColor(name);
                waypointClass.getMethod("setColor", int.class).invoke(waypoint, color);
                
                // Set icon
                waypointClass.getMethod("setIcon", String.class).invoke(waypoint, "waypoint-normal.png");
                
                // Make it persistent
                waypointClass.getMethod("setPersistent", boolean.class).invoke(waypoint, true);
                
                // Make it visible
                waypointClass.getMethod("setDisplayed", boolean.class).invoke(waypoint, true);
                
            } catch (Exception e) {
                // If setting properties fails, continue anyway
                System.out.println("Could not set waypoint properties: " + e.getMessage());
            }
            
            // Show the waypoint using ClientPlugin
            clientPluginClass.getMethod("show", waypointClass).invoke(clientPlugin, waypoint);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to create JourneyMap waypoint: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get appropriate color for waypoint based on dragon type
     */
    private static int getWaypointColor(String dragonName) {
        String name = dragonName.toLowerCase();
        
        if (name.contains("fire") || name.contains("red")) {
            return 0xFF4444; // Red
        } else if (name.contains("ice") || name.contains("blue")) {
            return 0x4444FF; // Blue
        } else if (name.contains("lightning") || name.contains("yellow")) {
            return 0xFFFF44; // Yellow
        } else if (name.contains("ender")) {
            return 0x8844FF; // Purple
        } else {
            return 0x44FF44; // Green (default)
        }
    }
    
    /**
     * Remove a waypoint by its ID
     * @param pos The position that was used to create the waypoint
     * @return true if waypoint was successfully removed, false otherwise
     */
    public static boolean removeWaypoint(BlockPos pos) {
        if (!isJourneyMapAvailable()) {
            return false;
        }
        
        try {
            Class<?> clientPluginClass = Class.forName("journeymap.client.api.ClientPlugin");
            Object clientPlugin = clientPluginClass.getMethod("getInstance").invoke(null);
            
            String modId = "dragontracker";
            String waypointId = "dragon_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
            
            // Remove waypoint using ClientPlugin
            clientPluginClass.getMethod("remove", String.class, String.class)
                .invoke(clientPlugin, modId, waypointId);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to remove JourneyMap waypoint: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if JourneyMap integration is working
     */
    public static boolean testIntegration() {
        return isJourneyMapAvailable();
    }
}