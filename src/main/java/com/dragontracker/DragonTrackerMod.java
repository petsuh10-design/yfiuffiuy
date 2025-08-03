package com.dragontracker;

import com.dragontracker.client.KeyBindings;
import com.dragontracker.client.gui.DragonTrackerScreen;
import com.dragontracker.client.gui.SimpleTestScreen;
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
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
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
        System.out.println("=== CLIENT SETUP EVENT ===");
        
        // Initialize client-side components
        MinecraftForge.EVENT_BUS.register(new DragonHighlightRenderer());
        
        // Діагностика keybindings
        System.out.println("Checking keybindings in client setup:");
        try {
            System.out.println("OPEN_DRAGON_TRACKER exists: " + (KeyBindings.OPEN_DRAGON_TRACKER != null));
            System.out.println("OPEN_DRAGON_TRACKER key: " + KeyBindings.OPEN_DRAGON_TRACKER.getKey().getName());
            System.out.println("OPEN_DRAGON_TRACKER category: " + KeyBindings.OPEN_DRAGON_TRACKER.getCategory());
        } catch (Exception e) {
            System.out.println("ERROR accessing keybindings in client setup: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Dragon Tracker Client setup complete!");
        System.out.println("=========================");
    }
    
    private void registerKeyBindings(RegisterKeyMappingsEvent event) {
        System.out.println("=== REGISTERING KEYBINDINGS ===");
        
        try {
            System.out.println("Registering OPEN_DRAGON_TRACKER...");
            event.register(KeyBindings.OPEN_DRAGON_TRACKER);
            System.out.println("OPEN_DRAGON_TRACKER registered successfully");
            
            System.out.println("Registering TOGGLE_HIGHLIGHTING...");
            event.register(KeyBindings.TOGGLE_HIGHLIGHTING);
            System.out.println("TOGGLE_HIGHLIGHTING registered successfully");
            
            System.out.println("Registering QUICK_ADD_WAYPOINT...");
            event.register(KeyBindings.QUICK_ADD_WAYPOINT);
            System.out.println("QUICK_ADD_WAYPOINT registered successfully");
            
            System.out.println("All Dragon Tracker keybindings registered: H, J, K");
            
            // Перевірка після реєстрації
            System.out.println("Post-registration check:");
            System.out.println("H key: " + KeyBindings.OPEN_DRAGON_TRACKER.getKey().getName());
            System.out.println("J key: " + KeyBindings.TOGGLE_HIGHLIGHTING.getKey().getName());
            System.out.println("K key: " + KeyBindings.QUICK_ADD_WAYPOINT.getKey().getName());
            
        } catch (Exception e) {
            System.out.println("ERROR during keybinding registration: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("==============================");
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            
            // Діагностика стану гри
            if (mc.level != null && mc.player != null) {
                // Перевіряємо стан keybindings кожні 60 тиків (3 секунди)
                if (mc.level.getGameTime() % 60 == 0) {
                    System.out.println("=== KEYBINDING STATUS CHECK ===");
                    System.out.println("OPEN_DRAGON_TRACKER key: " + KeyBindings.OPEN_DRAGON_TRACKER.getKey().getName());
                    System.out.println("OPEN_DRAGON_TRACKER mapping: " + KeyBindings.OPEN_DRAGON_TRACKER.getDefaultKey().getName());
                    System.out.println("Current screen: " + (mc.screen != null ? mc.screen.getClass().getSimpleName() : "null"));
                    System.out.println("Player in game: " + (mc.player != null));
                    System.out.println("Level loaded: " + (mc.level != null));
                    System.out.println("===============================");
                }
            }
            
            DragonDetector.tick();
            handleKeyPresses(mc);
        }
    }

    private static void handleKeyPresses(Minecraft mc) {
        System.out.println("=== HANDLE KEY PRESSES START ===");
        System.out.println("Current time: " + System.currentTimeMillis());
        System.out.println("Screen state: " + (mc.screen != null ? mc.screen.getClass().getSimpleName() : "null"));
        
        // Перевіряємо стан всіх наших клавіш
        System.out.println("Checking OPEN_DRAGON_TRACKER...");
        if (KeyBindings.OPEN_DRAGON_TRACKER.isDown()) {
            System.out.println("H key is being held down!");
        }
        
        int clickCount = 0;
        while (KeyBindings.OPEN_DRAGON_TRACKER.consumeClick()) {
            clickCount++;
            System.out.println("H key click consumed #" + clickCount);
            System.out.println("Player state: " + (mc.player != null ? "alive" : "null"));
            System.out.println("Level state: " + (mc.level != null ? "loaded" : "null"));
            System.out.println("Current screen before: " + (mc.screen != null ? mc.screen.getClass().getSimpleName() : "null"));
            
            if (mc.screen == null) {
                                 System.out.println("Attempting to open SIMPLE TEST GUI...");
                 try {
                     SimpleTestScreen screen = new SimpleTestScreen();
                     System.out.println("SimpleTestScreen created successfully");
                     mc.setScreen(screen);
                     System.out.println("mc.setScreen() called successfully");
                     System.out.println("Current screen after: " + (mc.screen != null ? mc.screen.getClass().getSimpleName() : "null"));
                } catch (Exception e) {
                    System.out.println("ERROR creating/setting Dragon Tracker GUI: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Fallback - пробуємо відкрити базовий меню інвентаря як тест
                    System.out.println("Trying fallback - opening inventory as test...");
                    try {
                        mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
                        System.out.println("Fallback inventory opened successfully!");
                    } catch (Exception e2) {
                        System.out.println("ERROR even with fallback inventory: " + e2.getMessage());
                    }
                }
            } else {
                System.out.println("Cannot open GUI - another screen is open: " + mc.screen.getClass().getSimpleName());
            }
        }
        
        if (clickCount > 0) {
            System.out.println("Total H key clicks processed: " + clickCount);
        }

        // Діагностика інших клавіш
        while (KeyBindings.TOGGLE_HIGHLIGHTING.consumeClick()) {
            System.out.println("J key pressed - toggling highlighting");
            toggleAllHighlighting();
        }

        while (KeyBindings.QUICK_ADD_WAYPOINT.consumeClick()) {
            System.out.println("K key pressed - adding waypoint");
            addNearestDragonWaypoint();
        }
        
        System.out.println("=== HANDLE KEY PRESSES END ===");
    }
    
    private static void toggleAllHighlighting() {
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
    
    private static void addNearestDragonWaypoint() {
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