package com.dragontracker;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("dragontracker")
public class SimpleDragonTrackerMod {
    
    public static final String MODID = "dragontracker";
    
    public SimpleDragonTrackerMod() {
        // Реєструємо setup event
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        
        System.out.println("Dragon Tracker Mod loading...");
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        System.out.println("Dragon Tracker Mod setup complete!");
    }
}