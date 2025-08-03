package com.dragontracker.client.renderer;

import com.dragontracker.config.DragonTrackerConfig;
import com.dragontracker.dragon.DragonDetector;
import com.dragontracker.dragon.DragonInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.Map;

@Mod.EventBusSubscriber(modid = "dragontracker", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DragonHighlightRenderer {
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        
        if (!DragonTrackerConfig.ENABLE_HIGHLIGHTING.get()) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        
        Map<Integer, DragonInfo> dragons = DragonDetector.getDetectedDragons();
        if (dragons.isEmpty()) {
            return;
        }
        
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        for (DragonInfo dragon : dragons.values()) {
            Entity entity = dragon.getEntity();
            if (entity == null || !entity.isAlive()) {
                continue;
            }
            
            boolean isHighlighted = DragonDetector.isHighlighted(dragon.getEntityId());
            if (!isHighlighted) {
                continue;
            }
            
            renderDragonHighlight(poseStack, buffer, entity, dragon, cameraPos);
            
            if (DragonTrackerConfig.ENABLE_DISTANCE_DISPLAY.get()) {
                renderDistanceText(poseStack, entity, dragon, cameraPos);
            }
        }
        
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
    
    private static void renderDragonHighlight(PoseStack poseStack, BufferBuilder buffer, 
                                            Entity entity, DragonInfo dragon, Vec3 cameraPos) {
        
        AABB boundingBox = entity.getBoundingBox();
        double x = boundingBox.minX - cameraPos.x;
        double y = boundingBox.minY - cameraPos.y;
        double z = boundingBox.minZ - cameraPos.z;
        double width = boundingBox.getXsize();
        double height = boundingBox.getYsize();
        double depth = boundingBox.getZsize();
        
        // Get color based on dragon type
        float[] color = getDragonHighlightColor(dragon.getDragonType());
        float red = color[0];
        float green = color[1];
        float blue = color[2];
        float alpha = 0.3f;
        
        // Pulsing effect
        long time = System.currentTimeMillis();
        float pulse = (float) (Math.sin(time * 0.003) * 0.1 + 0.9);
        alpha *= pulse;
        
        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();
        
        // Render outline box
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        
        // Bottom face
        addLine(buffer, matrix, x, y, z, x + width, y, z, red, green, blue, alpha);
        addLine(buffer, matrix, x + width, y, z, x + width, y, z + depth, red, green, blue, alpha);
        addLine(buffer, matrix, x + width, y, z + depth, x, y, z + depth, red, green, blue, alpha);
        addLine(buffer, matrix, x, y, z + depth, x, y, z, red, green, blue, alpha);
        
        // Top face
        addLine(buffer, matrix, x, y + height, z, x + width, y + height, z, red, green, blue, alpha);
        addLine(buffer, matrix, x + width, y + height, z, x + width, y + height, z + depth, red, green, blue, alpha);
        addLine(buffer, matrix, x + width, y + height, z + depth, x, y + height, z + depth, red, green, blue, alpha);
        addLine(buffer, matrix, x, y + height, z + depth, x, y + height, z, red, green, blue, alpha);
        
        // Vertical lines
        addLine(buffer, matrix, x, y, z, x, y + height, z, red, green, blue, alpha);
        addLine(buffer, matrix, x + width, y, z, x + width, y + height, z, red, green, blue, alpha);
        addLine(buffer, matrix, x + width, y, z + depth, x + width, y + height, z + depth, red, green, blue, alpha);
        addLine(buffer, matrix, x, y, z + depth, x, y + height, z + depth, red, green, blue, alpha);
        
        BufferUploader.drawWithShader(buffer.end());
        
        poseStack.popPose();
    }
    
    private static void addLine(BufferBuilder buffer, Matrix4f matrix, 
                              double x1, double y1, double z1, 
                              double x2, double y2, double z2,
                              float red, float green, float blue, float alpha) {
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(red, green, blue, alpha).endVertex();
    }
    
    private static void renderDistanceText(PoseStack poseStack, Entity entity, 
                                         DragonInfo dragon, Vec3 cameraPos) {
        
        Minecraft mc = Minecraft.getInstance();
        Vec3 entityPos = entity.position().add(0, entity.getBbHeight() + 0.5, 0);
        Vec3 relativePos = entityPos.subtract(cameraPos);
        
        String distanceText = String.format("%.1fm", dragon.getDistance());
        
        poseStack.pushPose();
        poseStack.translate(relativePos.x, relativePos.y, relativePos.z);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);
        
        Matrix4f matrix = poseStack.last().pose();
        
        int textWidth = mc.font.width(distanceText);
        int backgroundColor = 0x80000000;
        int textColor = 0xFFFFFF;
        
        // Draw background
        VertexConsumer vertex = mc.renderBuffers().bufferSource().getBuffer(
            net.minecraft.client.renderer.RenderType.textBackground());
        vertex.vertex(matrix, -textWidth / 2f - 1, -1, 0).color(backgroundColor).endVertex();
        vertex.vertex(matrix, -textWidth / 2f - 1, 8, 0).color(backgroundColor).endVertex();
        vertex.vertex(matrix, textWidth / 2f + 1, 8, 0).color(backgroundColor).endVertex();
        vertex.vertex(matrix, textWidth / 2f + 1, -1, 0).color(backgroundColor).endVertex();
        
        // Draw text
        mc.font.drawInBatch(distanceText, -textWidth / 2f, 0, textColor, false, 
                           matrix, mc.renderBuffers().bufferSource(), 
                           net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
        
        mc.renderBuffers().bufferSource().endBatch();
        
        poseStack.popPose();
    }
    
    private static float[] getDragonHighlightColor(String dragonType) {
        switch (dragonType.toLowerCase()) {
            case "fire":
                return new float[]{1.0f, 0.2f, 0.2f}; // Red
            case "ice":
                return new float[]{0.2f, 0.6f, 1.0f}; // Light blue
            case "lightning":
                return new float[]{1.0f, 1.0f, 0.2f}; // Yellow
            default:
                return new float[]{0.2f, 1.0f, 0.2f}; // Green
        }
    }
}