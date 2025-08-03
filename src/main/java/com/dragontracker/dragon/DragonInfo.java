package com.dragontracker.dragon;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public class DragonInfo {
    private final int entityId;
    private final String dragonType;
    private final String gender;
    private final BlockPos position;
    private final double distance;
    private final String dragonStage;
    private final boolean isAsleep;
    private final boolean isTamed;
    private final Entity entity;
    
    public DragonInfo(Entity entity, String dragonType, String gender, BlockPos position, 
                     double distance, String dragonStage, boolean isAsleep, boolean isTamed) {
        this.entity = entity;
        this.entityId = entity.getId();
        this.dragonType = dragonType;
        this.gender = gender;
        this.position = position;
        this.distance = distance;
        this.dragonStage = dragonStage;
        this.isAsleep = isAsleep;
        this.isTamed = isTamed;
    }
    
    public int getEntityId() {
        return entityId;
    }
    
    public String getDragonType() {
        return dragonType;
    }
    
    public String getGender() {
        return gender;
    }
    
    public BlockPos getPosition() {
        return position;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public String getDragonStage() {
        return dragonStage;
    }
    
    public boolean isAsleep() {
        return isAsleep;
    }
    
    public boolean isTamed() {
        return isTamed;
    }
    
    public Entity getEntity() {
        return entity;
    }
    
    public String getDisplayName() {
        return String.format("%s %s Dragon (Stage %s)", 
            gender.substring(0, 1).toUpperCase() + gender.substring(1),
            dragonType, 
            dragonStage);
    }
    
    public String getStatusText() {
        StringBuilder status = new StringBuilder();
        if (isTamed) {
            status.append("Tamed");
        } else {
            status.append("Wild");
        }
        if (isAsleep) {
            status.append(", Sleeping");
        } else {
            status.append(", Awake");
        }
        return status.toString();
    }
}