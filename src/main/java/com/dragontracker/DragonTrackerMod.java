package com.dragontracker;

import com.dragontracker.client.KeyBindings;
import com.dragontracker.client.gui.DragonTrackerScreen;
import com.dragontracker.client.renderer.DragonHighlightRenderer;
import com.dragontracker.config.DragonTrackerConfig;
import com.dragontracker.dragon.DragonDetector;
import com.dragontracker.integration.JourneyMapIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(DragonTrackerMod.MODID)
public class DragonTrackerMod {
    
    public static final String MODID = "dragontracker";
    
    public DragonTrackerMod() {
        // Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DragonTrackerConfig.CLIENT_SPEC);
        
        // Register mod setup
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        
        // Register client setup only on client side
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyBindings);
            
            // Initialize keybindings early
            KeyBindings.init();
        }
        
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);
        
        System.out.println("Dragon Tracker Mod initializing...");
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        System.out.println("Dragon Tracker Mod setup complete!");
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        // Initialize client-side components
        MinecraftForge.EVENT_BUS.register(new DragonHighlightRenderer());
        
        System.out.println("Dragon Tracker Client setup complete!");
    }
    
    private void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.OPEN_DRAGON_TRACKER);
        event.register(KeyBindings.TOGGLE_HIGHLIGHTING);
        event.register(KeyBindings.QUICK_ADD_WAYPOINT);
        
        System.out.println("Dragon Tracker keybindings registered: H, J, K");
    }
    
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && FMLEnvironment.dist == Dist.CLIENT) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                // Update dragon detection system
                DragonDetector.tick();
                
                // Handle key presses
                handleKeyPresses(mc);
            }
        }
    }
    
    private void handleKeyPresses(Minecraft mc) {
        try {
            // Open GUI with H key
            while (KeyBindings.OPEN_DRAGON_TRACKER.consumeClick()) {
                System.out.println("H key pressed - opening Dragon Tracker GUI");
                if (mc.screen == null) {
                    mc.setScreen(new DragonTrackerScreen());
                    System.out.println("Dragon Tracker GUI opened successfully");
                } else {
                    System.out.println("Cannot open GUI - another screen is open: " + mc.screen.getClass().getSimpleName());
                }
            }
            
            // Toggle highlighting with J key
            while (KeyBindings.TOGGLE_HIGHLIGHTING.consumeClick()) {
                System.out.println("J key pressed - toggling highlighting");
                toggleAllHighlighting();
            }
            
            // Quick add waypoint with K key
            while (KeyBindings.QUICK_ADD_WAYPOINT.consumeClick()) {
                System.out.println("K key pressed - adding waypoint");
                addNearestDragonWaypoint();
            }
        } catch (Exception e) {
            System.err.println("Error handling key presses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void toggleAllHighlighting() {
        var dragons = DragonDetector.getDetectedDragons();
        boolean anyHighlighted = dragons.keySet().stream()
            .anyMatch(DragonDetector::isHighlighted);
        
        // If any are highlighted, turn off all. Otherwise, turn on all.
        for (Integer dragonId : dragons.keySet()) {
            if (anyHighlighted && DragonDetector.isHighlighted(dragonId)) {
                DragonDetector.toggleHighlight(dragonId);
            } else if (!anyHighlighted) {
                DragonDetector.toggleHighlight(dragonId);
            }
        }
        
        String message = anyHighlighted ? "Dragon highlighting disabled" : "Dragon highlighting enabled";
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(message), true);
        }
        System.out.println(message);
    }
    
    private void addNearestDragonWaypoint() {
        var dragons = DragonDetector.getDetectedDragons();
        if (dragons.isEmpty()) {
            String message = "No dragons detected nearby";
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(message), true);
            }
            System.out.println(message);
            return;
        }
        
        // Find nearest dragon
        var nearestDragon = dragons.values().stream()
            .min((d1, d2) -> Double.compare(d1.getDistance(), d2.getDistance()))
            .orElse(null);
        
        if (nearestDragon != null) {
            Entity entity = nearestDragon.getEntity();
            if (entity != null) {
                BlockPos pos = entity.blockPosition();
                String name = "Dragon " + nearestDragon.getDragonType() + " (" + 
                             nearestDragon.getGender() + ", Stage " + nearestDragon.getDragonStage() + ")";
                
                // Try JourneyMap integration
                boolean success = JourneyMapIntegration.addWaypoint(name, pos);
                
                String message = success ? 
                    "Waypoint added for " + nearestDragon.getDragonType() + " dragon" :
                    "JourneyMap not available - waypoint not created";
                
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(message), true);
                }
                System.out.println(message);
            }
        }
    }
}