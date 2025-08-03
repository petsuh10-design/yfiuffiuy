package com.dragontracker.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyBindings {
    
    public static final KeyMapping OPEN_DRAGON_TRACKER = new KeyMapping(
        "key.dragontracker.open_gui",
        KeyConflictContext.IN_GAME,
        KeyModifier.NONE,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_H,
        "key.categories.dragontracker"
    );
    
    public static final KeyMapping TOGGLE_HIGHLIGHTING = new KeyMapping(
        "key.dragontracker.toggle_highlighting", 
        KeyConflictContext.IN_GAME,
        KeyModifier.NONE,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_J,
        "key.categories.dragontracker"
    );
    
    public static final KeyMapping QUICK_ADD_WAYPOINT = new KeyMapping(
        "key.dragontracker.quick_waypoint",
        KeyConflictContext.IN_GAME, 
        KeyModifier.NONE,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_K,
        "key.categories.dragontracker"
    );
    
    public static void register() {
        // Key bindings are automatically registered in 1.20.1+ when they're created with KeyMapping
        // No need for explicit registration
    }
}