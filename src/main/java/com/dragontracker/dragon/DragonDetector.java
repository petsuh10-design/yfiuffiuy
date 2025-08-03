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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DragonDetector {
    
    private static final Map<Integer, DragonInfo> detectedDragons = new ConcurrentHashMap<>();
    private static final Set<Integer> highlightedDragons = new HashSet<>();
    private static int lastUpdateTick = 0;
    
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        
        int currentTick = mc.level.getGameTime() % Integer.MAX_VALUE;
        if (currentTick - lastUpdateTick < DragonTrackerConfig.UPDATE_FREQUENCY.get()) {
            return;
        }
        lastUpdateTick = currentTick;
        
        scanForDragons(mc.level, mc.player);
    }
    
    private static void scanForDragons(Level level, Player player) {
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
    }
    
    private static boolean isDragon(Entity entity) {
        String entityClassName = entity.getClass().getSimpleName().toLowerCase();
        return entityClassName.contains("dragon") && 
               entity.getClass().getName().contains("iceandfire");
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
            // Fallback if reflection fails
            BlockPos pos = entity.blockPosition();
            double distance = player.position().distanceTo(entity.position());
            return new DragonInfo(entity, "Unknown", "Unknown", pos, distance, "Unknown", false, false);
        }
    }
    
    private static String getDragonType(Entity entity) {
        try {
            // Try to get dragon variant/type using reflection
            Class<?> entityClass = entity.getClass();
            
            // Look for common Ice and Fire dragon type methods/fields
            try {
                Method getVariant = entityClass.getMethod("getVariant");
                Object variant = getVariant.invoke(entity);
                if (variant != null) {
                    return variant.toString();
                }
            } catch (Exception ignored) {}
            
            try {
                Field variantField = entityClass.getField("dragonType");
                Object variant = variantField.get(entity);
                if (variant != null) {
                    return variant.toString();
                }
            } catch (Exception ignored) {}
            
            // Fallback: parse from class name
            String className = entity.getClass().getSimpleName();
            if (className.contains("Fire")) return "Fire";
            if (className.contains("Ice")) return "Ice";
            if (className.contains("Lightning")) return "Lightning";
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return "Unknown";
    }
    
    private static String getDragonGender(Entity entity) {
        try {
            Class<?> entityClass = entity.getClass();
            
            try {
                Method isMale = entityClass.getMethod("isMale");
                boolean male = (Boolean) isMale.invoke(entity);
                return male ? "Male" : "Female";
            } catch (Exception ignored) {}
            
            try {
                Field genderField = entityClass.getField("gender");
                Object gender = genderField.get(entity);
                if (gender != null) {
                    return gender.toString();
                }
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return "Unknown";
    }
    
    private static String getDragonStage(Entity entity) {
        try {
            Class<?> entityClass = entity.getClass();
            
            try {
                Method getDragonStage = entityClass.getMethod("getDragonStage");
                Object stage = getDragonStage.invoke(entity);
                return stage != null ? stage.toString() : "Unknown";
            } catch (Exception ignored) {}
            
            try {
                Field stageField = entityClass.getField("dragonStage");
                Object stage = stageField.get(entity);
                return stage != null ? stage.toString() : "Unknown";
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return "Unknown";
    }
    
    private static boolean isDragonAsleep(Entity entity) {
        try {
            Class<?> entityClass = entity.getClass();
            
            try {
                Method isSleeping = entityClass.getMethod("isSleeping");
                return (Boolean) isSleeping.invoke(entity);
            } catch (Exception ignored) {}
            
            try {
                Field sleepingField = entityClass.getField("isSleeping");
                return sleepingField.getBoolean(entity);
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            // Ignore reflection errors
        }
        return false;
    }
    
    private static boolean isDragonTamed(Entity entity) {
        try {
            Class<?> entityClass = entity.getClass();
            
            try {
                Method isTame = entityClass.getMethod("isTame");
                return (Boolean) isTame.invoke(entity);
            } catch (Exception ignored) {}
            
            try {
                Field tamedField = entityClass.getField("isTamed");
                return tamedField.getBoolean(entity);
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