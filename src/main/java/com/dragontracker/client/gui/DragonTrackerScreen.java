package com.dragontracker.client.gui;

import com.dragontracker.dragon.DragonDetector;
import com.dragontracker.dragon.DragonInfo;
import com.dragontracker.integration.JourneyMapIntegration;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DragonTrackerScreen extends Screen {
    
    private static final ResourceLocation BACKGROUND_TEXTURE = 
        new ResourceLocation("dragontracker", "textures/gui/dragon_tracker_bg.png");
    
    private final int imageWidth = 256;
    private final int imageHeight = 192;
    private int leftPos;
    private int topPos;
    
    private List<DragonInfo> dragonList;
    private int selectedDragon = -1;
    private int scrollOffset = 0;
    
    public DragonTrackerScreen() {
        super(Component.translatable("gui.dragontracker.title"));
        refreshDragonList();
    }
    
    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        // Add waypoint button
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.dragontracker.add_waypoint"),
            button -> addSelectedToJourneyMap())
            .bounds(leftPos + 10, topPos + imageHeight - 30, 100, 20)
            .build());
            
        // Toggle highlight button
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.dragontracker.toggle_highlight"),
            button -> toggleHighlight())
            .bounds(leftPos + 120, topPos + imageHeight - 30, 100, 20)
            .build());
            
        // Refresh button
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.dragontracker.refresh"),
            button -> refreshDragonList())
            .bounds(leftPos + imageWidth - 60, topPos + 10, 50, 20)
            .build());
    }
    
    private void refreshDragonList() {
        Map<Integer, DragonInfo> dragons = DragonDetector.getDetectedDragons();
        this.dragonList = new ArrayList<>(dragons.values());
        this.dragonList.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
    }
    
    private void addSelectedToJourneyMap() {
        if (selectedDragon >= 0 && selectedDragon < dragonList.size()) {
            DragonInfo dragon = dragonList.get(selectedDragon);
            JourneyMapIntegration.addWaypoint(dragon);
        }
    }
    
    private void toggleHighlight() {
        if (selectedDragon >= 0 && selectedDragon < dragonList.size()) {
            DragonInfo dragon = dragonList.get(selectedDragon);
            DragonDetector.toggleHighlight(dragon.getEntityId());
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        
        // Draw background texture
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        graphics.blit(BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        
        // Draw title
        graphics.drawCenteredString(this.font, this.title, 
            leftPos + imageWidth / 2, topPos + 10, 0xFFFFFF);
        
        // Draw dragon list
        drawDragonList(graphics, mouseX, mouseY);
        
        // Draw selected dragon details
        if (selectedDragon >= 0 && selectedDragon < dragonList.size()) {
            drawDragonDetails(graphics, dragonList.get(selectedDragon));
        }
        
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    private void drawDragonList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = leftPos + 10;
        int listY = topPos + 30;
        int listWidth = 120;
        int listHeight = 120;
        
        // Draw list background
        graphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0x80000000);
        
        int entryHeight = 15;
        int maxVisible = listHeight / entryHeight;
        
        for (int i = 0; i < Math.min(dragonList.size(), maxVisible); i++) {
            int index = i + scrollOffset;
            if (index >= dragonList.size()) break;
            
            DragonInfo dragon = dragonList.get(index);
            int entryY = listY + i * entryHeight;
            
            // Highlight selected entry
            if (index == selectedDragon) {
                graphics.fill(listX, entryY, listX + listWidth, entryY + entryHeight, 0x80FFFFFF);
            }
            
            // Check if mouse is over this entry
            boolean isHovered = mouseX >= listX && mouseX < listX + listWidth && 
                              mouseY >= entryY && mouseY < entryY + entryHeight;
            if (isHovered) {
                graphics.fill(listX, entryY, listX + listWidth, entryY + entryHeight, 0x40FFFFFF);
            }
            
            // Draw dragon name and distance
            String dragonText = String.format("%s %s", 
                dragon.getDragonType(), dragon.getGender());
            graphics.drawString(font, dragonText, listX + 2, entryY + 2, 0xFFFFFF);
            
            String distanceText = String.format("%.0fm", dragon.getDistance());
            graphics.drawString(font, distanceText, listX + 2, entryY + 8, 0xCCCCCC);
        }
    }
    
    private void drawDragonDetails(GuiGraphics graphics, DragonInfo dragon) {
        int detailsX = leftPos + 140;
        int detailsY = topPos + 30;
        int detailsWidth = 106;
        
        // Draw details background
        graphics.fill(detailsX, detailsY, detailsX + detailsWidth, detailsY + 120, 0x80000000);
        
        int y = detailsY + 5;
        int lineHeight = 12;
        
        // Dragon name
        graphics.drawString(font, dragon.getDisplayName(), detailsX + 5, y, 0xFFFFFF);
        y += lineHeight + 3;
        
        // Type and gender
        graphics.drawString(font, "Type: " + dragon.getDragonType(), detailsX + 5, y, 0xCCCCCC);
        y += lineHeight;
        graphics.drawString(font, "Gender: " + dragon.getGender(), detailsX + 5, y, 0xCCCCCC);
        y += lineHeight;
        
        // Stage
        graphics.drawString(font, "Stage: " + dragon.getDragonStage(), detailsX + 5, y, 0xCCCCCC);
        y += lineHeight;
        
        // Status
        graphics.drawString(font, "Status: " + dragon.getStatusText(), detailsX + 5, y, 0xCCCCCC);
        y += lineHeight + 3;
        
        // Distance
        graphics.drawString(font, String.format("Distance: %.1fm", dragon.getDistance()), 
            detailsX + 5, y, 0xFFFFFF);
        y += lineHeight;
        
        // Coordinates
        graphics.drawString(font, String.format("X: %d", dragon.getPosition().getX()), 
            detailsX + 5, y, 0xCCCCCC);
        y += lineHeight;
        graphics.drawString(font, String.format("Y: %d", dragon.getPosition().getY()), 
            detailsX + 5, y, 0xCCCCCC);
        y += lineHeight;
        graphics.drawString(font, String.format("Z: %d", dragon.getPosition().getZ()), 
            detailsX + 5, y, 0xCCCCCC);
        
        // Highlight status
        boolean isHighlighted = DragonDetector.isHighlighted(dragon.getEntityId());
        String highlightText = isHighlighted ? "Highlighted: Yes" : "Highlighted: No";
        int color = isHighlighted ? 0x00FF00 : 0xFF0000;
        graphics.drawString(font, highlightText, detailsX + 5, y + lineHeight + 3, color);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle dragon list clicking
        int listX = leftPos + 10;
        int listY = topPos + 30;
        int listWidth = 120;
        int listHeight = 120;
        
        if (mouseX >= listX && mouseX < listX + listWidth && 
            mouseY >= listY && mouseY < listY + listHeight) {
            
            int entryHeight = 15;
            int clickedIndex = (int) ((mouseY - listY) / entryHeight) + scrollOffset;
            
            if (clickedIndex >= 0 && clickedIndex < dragonList.size()) {
                selectedDragon = clickedIndex;
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // Handle scrolling in dragon list
        int listX = leftPos + 10;
        int listY = topPos + 30;
        int listWidth = 120;
        int listHeight = 120;
        
        if (mouseX >= listX && mouseX < listX + listWidth && 
            mouseY >= listY && mouseY < listY + listHeight) {
            
            int maxVisible = listHeight / 15;
            int maxScroll = Math.max(0, dragonList.size() - maxVisible);
            
            scrollOffset -= (int) delta;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            
            return true;
        }
        
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}