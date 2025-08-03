package com.dragontracker.client.gui;

import com.dragontracker.dragon.DragonDetector;
import com.dragontracker.dragon.DragonInfo;
import com.dragontracker.integration.JourneyMapIntegration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DragonTrackerScreen extends Screen {
    
    private final int PANEL_WIDTH = 400;
    private final int PANEL_HEIGHT = 300;
    private int leftPos;
    private int topPos;
    
    private List<DragonInfo> dragonList;
    private int selectedDragon = -1;
    private int scrollOffset = 0;
    private final int MAX_VISIBLE_DRAGONS = 10;
    
    public DragonTrackerScreen() {
        super(Component.translatable("gui.dragontracker.title"));
        refreshDragonList();
        System.out.println("DragonTrackerScreen constructor called");
    }
    
    @Override
    protected void init() {
        this.leftPos = (this.width - PANEL_WIDTH) / 2;
        this.topPos = (this.height - PANEL_HEIGHT) / 2;
        
        System.out.println("DragonTrackerScreen init() called - creating buttons");
        
        // Add waypoint button
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.dragontracker.add_waypoint"),
            button -> addWaypoint())
            .bounds(leftPos + 10, topPos + PANEL_HEIGHT - 30, 100, 20)
            .build());
            
        // Toggle highlight button
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.dragontracker.toggle_highlight"),
            button -> toggleHighlight())
            .bounds(leftPos + 120, topPos + PANEL_HEIGHT - 30, 100, 20)
            .build());
            
        // Refresh button
        this.addRenderableWidget(Button.builder(
            Component.translatable("gui.dragontracker.refresh"),
            button -> refreshDragonList())
            .bounds(leftPos + 230, topPos + PANEL_HEIGHT - 30, 70, 20)
            .build());
            
        // Close button
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            button -> this.onClose())
            .bounds(leftPos + 310, topPos + PANEL_HEIGHT - 30, 80, 20)
            .build());
            
        System.out.println("DragonTrackerScreen buttons created successfully");
    }
    
    private void refreshDragonList() {
        try {
            Map<Integer, DragonInfo> dragons = DragonDetector.getDetectedDragons();
            this.dragonList = new ArrayList<>(dragons.values());
            this.dragonList.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
            System.out.println("Dragon list refreshed: " + dragonList.size() + " dragons found");
        } catch (Exception e) {
            this.dragonList = new ArrayList<>();
            System.err.println("Error refreshing dragon list: " + e.getMessage());
        }
    }
    
    private void addWaypoint() {
        if (selectedDragon >= 0 && selectedDragon < dragonList.size()) {
            DragonInfo dragon = dragonList.get(selectedDragon);
            if (dragon.getEntity() != null) {
                BlockPos pos = dragon.getPosition();
                String name = "Dragon " + dragon.getDragonType() + " (" + 
                             dragon.getGender() + ", Stage " + dragon.getDragonStage() + ")";
                
                boolean success = JourneyMapIntegration.addWaypoint(name, pos);
                String message = success ? "Waypoint added!" : "JourneyMap not available";
                
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.displayClientMessage(
                        Component.literal(message), true);
                }
                System.out.println(message + " for dragon at " + pos);
            }
        } else {
            System.out.println("No dragon selected for waypoint");
        }
    }
    
    private void toggleHighlight() {
        if (selectedDragon >= 0 && selectedDragon < dragonList.size()) {
            DragonInfo dragon = dragonList.get(selectedDragon);
            DragonDetector.toggleHighlight(dragon.getEntityId());
            
            boolean highlighted = DragonDetector.isHighlighted(dragon.getEntityId());
            String message = highlighted ? "Dragon highlighted" : "Dragon unhighlighted";
            
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(
                    Component.literal(message), true);
            }
            System.out.println(message + " for " + dragon.getDragonType() + " dragon");
        } else {
            System.out.println("No dragon selected for highlighting");
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw background
        this.renderBackground(graphics);
        
        // Draw main panel background
        graphics.fill(leftPos, topPos, leftPos + PANEL_WIDTH, topPos + PANEL_HEIGHT, 0xCC000000);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + PANEL_WIDTH - 1, topPos + PANEL_HEIGHT - 1, 0xCC333333);
        
        // Draw title
        graphics.drawCenteredString(this.font, this.title, 
            leftPos + PANEL_WIDTH / 2, topPos + 10, 0xFFFFFF);
        
        // Draw dragon list
        if (dragonList.isEmpty()) {
            graphics.drawCenteredString(this.font, 
                Component.translatable("gui.dragontracker.no_dragons").getString(),
                leftPos + PANEL_WIDTH / 2, topPos + 50, 0xAAAAAA);
        } else {
            drawDragonList(graphics, mouseX, mouseY);
        }
        
        // Draw selected dragon details
        if (selectedDragon >= 0 && selectedDragon < dragonList.size()) {
            drawDragonDetails(graphics);
        }
        
        // Draw widgets (buttons)
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    private void drawDragonList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = leftPos + 10;
        int listY = topPos + 30;
        int listWidth = 180;
        int itemHeight = 20;
        
        // Draw list background
        graphics.fill(listX, listY, listX + listWidth, listY + MAX_VISIBLE_DRAGONS * itemHeight, 0x88000000);
        
        for (int i = 0; i < Math.min(dragonList.size(), MAX_VISIBLE_DRAGONS); i++) {
            int index = i + scrollOffset;
            if (index >= dragonList.size()) break;
            
            DragonInfo dragon = dragonList.get(index);
            int itemY = listY + i * itemHeight;
            
            // Highlight selected item
            if (index == selectedDragon) {
                graphics.fill(listX, itemY, listX + listWidth, itemY + itemHeight, 0x80FFFFFF);
            }
            
            // Check if mouse is over this item
            boolean mouseOver = mouseX >= listX && mouseX < listX + listWidth && 
                              mouseY >= itemY && mouseY < itemY + itemHeight;
            if (mouseOver && index != selectedDragon) {
                graphics.fill(listX, itemY, listX + listWidth, itemY + itemHeight, 0x40FFFFFF);
            }
            
            // Draw dragon name and distance
            String name = dragon.getDragonType() + " " + dragon.getGender().substring(0, 1);
            String distance = String.format("%.0fm", dragon.getDistance());
            
            graphics.drawString(this.font, name, listX + 5, itemY + 6, 0xFFFFFF);
            graphics.drawString(this.font, distance, listX + listWidth - 50, itemY + 6, 0xAAAAAAA);
        }
    }
    
    private void drawDragonDetails(GuiGraphics graphics) {
        DragonInfo dragon = dragonList.get(selectedDragon);
        int detailsX = leftPos + 200;
        int detailsY = topPos + 30;
        int detailsWidth = 190;
        int detailsHeight = 200;
        
        // Draw details background
        graphics.fill(detailsX, detailsY, detailsX + detailsWidth, detailsY + detailsHeight, 0x88000000);
        
        // Draw details text
        int textY = detailsY + 10;
        int lineHeight = 12;
        
        graphics.drawString(this.font, "Dragon Details:", detailsX + 10, textY, 0xFFFF00);
        textY += lineHeight + 5;
        
        graphics.drawString(this.font, "Type: " + dragon.getDragonType(), detailsX + 10, textY, 0xFFFFFF);
        textY += lineHeight;
        
        graphics.drawString(this.font, "Gender: " + dragon.getGender(), detailsX + 10, textY, 0xFFFFFF);
        textY += lineHeight;
        
        graphics.drawString(this.font, "Stage: " + dragon.getDragonStage(), detailsX + 10, textY, 0xFFFFFF);
        textY += lineHeight;
        
        graphics.drawString(this.font, String.format("Distance: %.1f blocks", dragon.getDistance()), 
            detailsX + 10, textY, 0xFFFFFF);
        textY += lineHeight;
        
        BlockPos pos = dragon.getPosition();
        graphics.drawString(this.font, String.format("Position: %d, %d, %d", pos.getX(), pos.getY(), pos.getZ()), 
            detailsX + 10, textY, 0xFFFFFF);
        textY += lineHeight;
        
        String status = dragon.isTamed() ? "Tamed" : "Wild";
        if (dragon.isAsleep()) status += ", Sleeping";
        graphics.drawString(this.font, "Status: " + status, detailsX + 10, textY, 0xFFFFFF);
        textY += lineHeight;
        
        boolean highlighted = DragonDetector.isHighlighted(dragon.getEntityId());
        graphics.drawString(this.font, "Highlighted: " + (highlighted ? "Yes" : "No"), 
            detailsX + 10, textY, highlighted ? 0x00FF00 : 0xFF0000);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle dragon list clicks
        int listX = leftPos + 10;
        int listY = topPos + 30;
        int listWidth = 180;
        int itemHeight = 20;
        
        if (mouseX >= listX && mouseX < listX + listWidth) {
            for (int i = 0; i < Math.min(dragonList.size(), MAX_VISIBLE_DRAGONS); i++) {
                int itemY = listY + i * itemHeight;
                if (mouseY >= itemY && mouseY < itemY + itemHeight) {
                    int index = i + scrollOffset;
                    if (index < dragonList.size()) {
                        selectedDragon = index;
                        System.out.println("Selected dragon: " + dragonList.get(index).getDragonType());
                        return true;
                    }
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (dragonList.size() > MAX_VISIBLE_DRAGONS) {
            scrollOffset = Math.max(0, Math.min(dragonList.size() - MAX_VISIBLE_DRAGONS, 
                scrollOffset - (int) delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void onClose() {
        System.out.println("DragonTrackerScreen closing");
        super.onClose();
    }
}