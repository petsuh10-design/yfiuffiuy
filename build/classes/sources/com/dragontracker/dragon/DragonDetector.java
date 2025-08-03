package com.dragontracker.dragon;

import com.dragontracker.config.DragonTrackerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DragonDetector {
    
    private static final Map<Integer, DragonInfo> detectedDragons = new ConcurrentHashMap<>();
    private static final Set<Integer> highlightedDragons = new HashSet<>();
    private static int lastUpdateTick = 0;
    
    public static void tick() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) {
                return;
            }
            
            int currentTick = (int) (mc.level.getGameTime() % Integer.MAX_VALUE);
            if (currentTick - lastUpdateTick < DragonTrackerConfig.UPDATE_FREQUENCY.get()) {
                return;
            }
            lastUpdateTick = currentTick;
            
            scanForDragons(mc.level, mc.player);
        } catch (Exception e) {
            // Silently handle errors to prevent crashes
        }
    }
    
    private static void scanForDragons(Level level, Player player) {
        try {
            Vec3 playerPos = player.position();
            double range = DragonTrackerConfig.DETECTION_RANGE.get();
            
            AABB searchArea = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range
            );
            
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, searchArea);
            Set<Integer> currentDragons = new HashSet<>();
            
            for (Entity entity : entities) {
                if (isDragon(entity)) {
                    DragonInfo dragonInfo = createDragonInfo(entity, player);
                    if (dragonInfo != null) {
                        currentDragons.add(entity.getId());
                        
                        boolean isNew = !detectedDragons.containsKey(entity.getId());
                        detectedDragons.put(entity.getId(), dragonInfo);
                        
                        if (isNew && DragonTrackerConfig.ENABLE_SOUND_NOTIFICATIONS.get()) {
                            level.playLocalSound(playerPos.x, playerPos.y, playerPos.z, 
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 
                                0.5f, 1.0f, false);
                        }
                    }
                }
            }
            
            // Remove dragons that are no longer in range
            detectedDragons.keySet().retainAll(currentDragons);
            highlightedDragons.retainAll(currentDragons);
        } catch (Exception e) {
            // Silently handle errors
        }
    }
    
    private static boolean isDragon(Entity entity) {
        if (entity == null) return false;
        
        String entityClassName = entity.getClass().getSimpleName().toLowerCase();
        String packageName = entity.getClass().getName().toLowerCase();
        
        // Check for Ice and Fire dragons
        return (entityClassName.contains("dragon") && packageName.contains("iceandfire")) ||
               entityClassName.equals("entitydragonfire") ||
               entityClassName.equals("entitydragonice") ||
               entityClassName.equals("entitydragonlightning") ||
               // Also detect vanilla Ender Dragon for testing
               entityClassName.contains("enderdragon");
    }
    
    private static DragonInfo createDragonInfo(Entity entity, Player player) {
        try {
            String dragonType = getDragonType(entity);
            String gender = getDragonGender(entity);
            String stage = getDragonStage(entity);
            boolean isAsleep = isDragonAsleep(entity);
            boolean isTamed = isDragonTamed(entity);
            
            BlockPos pos = entity.blockPosition();
            double distance = player.position().distanceTo(entity.position());
            
            return new DragonInfo(entity, dragonType, gender, pos, distance, stage, isAsleep, isTamed);
        } catch (Exception e) {
            // Fallback if anything fails
            BlockPos pos = entity.blockPosition();
            double distance = player.position().distanceTo(entity.position());
            return new DragonInfo(entity, "Unknown", "Unknown", pos, distance, "Unknown", false, false);
        }
    }
    
    private static String getDragonType(Entity entity) {
        try {
            String className = entity.getClass().getSimpleName().toLowerCase();
            
            if (className.contains("fire") || className.contains("red")) return "Fire";
            if (className.contains("ice") || className.contains("blue")) return "Ice";
            if (className.contains("lightning") || className.contains("yellow")) return "Lightning";
            if (className.contains("ender")) return "Ender";
            
            // Try reflection as fallback
            try {
                Object variant = entity.getClass().getMethod("getVariant").invoke(entity);
                if (variant != null) return variant.toString();
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return "Unknown";
    }
    
    private static String getDragonGender(Entity entity) {
        try {
            // Try reflection for Ice and Fire specific methods
            try {
                boolean male = (Boolean) entity.getClass().getMethod("isMale").invoke(entity);
                return male ? "Male" : "Female";
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return "Unknown";
    }
    
    private static String getDragonStage(Entity entity) {
        try {
            // Try reflection for Ice and Fire specific methods
            try {
                Object stage = entity.getClass().getMethod("getDragonStage").invoke(entity);
                return stage != null ? stage.toString() : "Adult";
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return "Adult";
    }
    
    private static boolean isDragonAsleep(Entity entity) {
        try {
            // Try reflection for Ice and Fire specific methods
            try {
                return (Boolean) entity.getClass().getMethod("isSleeping").invoke(entity);
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return false;
    }
    
    private static boolean isDragonTamed(Entity entity) {
        try {
            // Try reflection for Ice and Fire specific methods
            try {
                return (Boolean) entity.getClass().getMethod("isTame").invoke(entity);
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return false;
    }
    
    public static Map<Integer, DragonInfo> getDetectedDragons() {
        return new HashMap<>(detectedDragons);
    }
    
    public static void toggleHighlight(int dragonId) {
        if (highlightedDragons.contains(dragonId)) {
            highlightedDragons.remove(dragonId);
        } else {
            highlightedDragons.add(dragonId);
        }
    }
    
    public static boolean isHighlighted(int dragonId) {
        return highlightedDragons.contains(dragonId);
    }
    
    public static Set<Integer> getHighlightedDragons() {
        return new HashSet<>(highlightedDragons);
    }
    
    public static void clearAll() {
        detectedDragons.clear();
        highlightedDragons.clear();
    }
}