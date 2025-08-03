package com.dragontracker;

import com.dragontracker.client.KeyBindings;
import com.dragontracker.client.gui.DragonTrackerScreen;
import com.dragontracker.config.DragonTrackerConfig;
import com.dragontracker.dragon.DragonDetector;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(DragonTrackerMod.MODID)
public class DragonTrackerMod {
    
    public static final String MODID = "dragontracker";
    
    public DragonTrackerMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DragonTrackerConfig.CLIENT_SPEC);
        
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            KeyBindings.register();
        });
    }
    
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onKeyInput(InputEvent.Key event) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null && mc.player != null) {
            if (KeyBindings.OPEN_DRAGON_TRACKER.consumeClick()) {
                mc.setScreen(new DragonTrackerScreen());
            }
        }
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                DragonDetector.tick();
            }
        }
    }
}