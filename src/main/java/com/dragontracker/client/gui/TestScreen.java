package com.dragontracker.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TestScreen extends Screen {
    
    public TestScreen() {
        super(Component.literal("Dragon Tracker Test"));
    }
    
    @Override
    protected void init() {
        // Add a simple close button
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            button -> this.onClose())
            .bounds(this.width / 2 - 50, this.height / 2 + 50, 100, 20)
            .build());
            
        System.out.println("Test GUI initialized successfully");
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw background
        this.renderBackground(graphics);
        
        // Draw title
        graphics.drawCenteredString(this.font, "Dragon Tracker Test Screen", 
            this.width / 2, this.height / 2 - 20, 0xFFFFFF);
            
        graphics.drawCenteredString(this.font, "If you see this, GUI system works!", 
            this.width / 2, this.height / 2, 0x00FF00);
        
        // Draw widgets
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}