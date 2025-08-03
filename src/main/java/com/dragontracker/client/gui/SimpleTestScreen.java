package com.dragontracker.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SimpleTestScreen extends Screen {
    
    public SimpleTestScreen() {
        super(Component.literal("SIMPLE TEST SCREEN"));
        System.out.println("SimpleTestScreen constructor called");
    }
    
    @Override
    protected void init() {
        super.init();
        System.out.println("SimpleTestScreen.init() called");
        System.out.println("Screen width: " + this.width + ", height: " + this.height);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        System.out.println("SimpleTestScreen.render() called");
        
        // Простий фон
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        
        // Великий текст в центрі
        String text = "TEST SCREEN WORKS!";
        int textWidth = this.font.width(text);
        int x = (this.width - textWidth) / 2;
        int y = this.height / 2;
        
        guiGraphics.drawString(this.font, text, x, y, 0xFFFFFF);
        
        // Інструкція
        String instruction = "Press ESC to close";
        int instWidth = this.font.width(instruction);
        int instX = (this.width - instWidth) / 2;
        int instY = y + 20;
        
        guiGraphics.drawString(this.font, instruction, instX, instY, 0xAAAAAA);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        System.out.println("SimpleTestScreen.render() completed");
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        System.out.println("SimpleTestScreen.keyPressed: " + keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void onClose() {
        System.out.println("SimpleTestScreen.onClose() called");
        super.onClose();
    }
    
    @Override
    public boolean isPauseScreen() {
        return false; // Не паузить гру
    }
}