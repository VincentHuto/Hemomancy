package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.client.event.AxonalTransductionClient;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ShaderInit;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** The route has its own identity mask; world colors cannot accidentally become highlighted nodes. */
public final class AxonalTravelView {
    private static TextureTarget scene;
    private static TextureTarget mask;
    private static long scannedAt = Long.MIN_VALUE;
    private static BlockPos scannedNear;
    private static List<BlockPos> routes = List.of();
    private static final float[][] CORNERS = {{-1,-1,-1},{1,-1,-1},{1,1,-1},{-1,1,-1},{-1,-1,1},{1,-1,1},{1,1,1},{-1,1,1}};
    private static final int[][] FACES = {{0,1,2,3},{4,7,6,5},{0,4,5,1},{3,2,6,7},{0,3,7,4},{1,5,6,2}};
    private AxonalTravelView() { }
    public static void clear() {
        routes = List.of(); scannedNear = null; scannedAt = Long.MIN_VALUE;
        if (scene != null) scene.destroyBuffers();
        if (mask != null) mask.destroyBuffers();
        scene = mask = null;
    }
    private static void targets(RenderTarget main) {
        if (scene != null && scene.width == main.width && scene.height == main.height) return;
        clear();
        scene = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
        mask = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
        mask.setClearColor(0, 0, 0, 0);
        scene.setFilterMode(GL11.GL_LINEAR);
        mask.setFilterMode(GL11.GL_LINEAR);
    }
    public static void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var buffer = mc.renderBuffers().bufferSource();
        Vec3 camera = event.getCamera().getPosition();
        Matrix4f matrix = event.getModelViewMatrix();
        for (var player : mc.level.players()) {
            if (!AxonalTransductionManager.isTraveling(player) || player == mc.player && AxonalTransductionClient.active()) continue;
            Vec3 center = player.getEyePosition(event.getPartialTick().getGameTimeDeltaPartialTick(true)).subtract(camera);
            box(buffer.getBuffer(RenderType.lightning()), matrix, center, .10, .10, .10, 1, .9F, .35F, .9F);
        }
        buffer.endBatch(RenderType.lightning());
        if (!AxonalTransductionClient.active()) return;
        RenderTarget main = mc.getMainRenderTarget();
        if (main.width <= 0 || main.height <= 0) return;
        targets(main);
        BlockPos near = BlockPos.containing(camera);
        if (scannedNear == null || scannedNear.distSqr(near) >= 16 || mc.level.getGameTime() - scannedAt >= 8) {
            List<BlockPos> found = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(near.offset(-16,-16,-16), near.offset(16,16,16))) {
                if (pos.distSqr(near) > 256 || !mc.level.hasChunkAt(pos)) continue;
                var state = mc.level.getBlockState(pos);
                if (state.is(BlockInit.nerve_fiber.get()) || state.is(BlockInit.nerve_bundle.get()) || state.is(BlockInit.synaptic_node.get()))
                    found.add(pos.immutable());
            }
            routes = found.stream().sorted(Comparator.comparingDouble(near::distSqr)).limit(2048).toList();
            scannedNear = near; scannedAt = mc.level.getGameTime();
        }
        buffer.endBatch();
        mask.clear(Minecraft.ON_OSX);
        mask.bindWrite(false);
        try {
            // Nodes remain legible even when several fibers overlap in the same view direction.
            for (int pass = 0; pass < 2; pass++) {
                VertexConsumer vertices = buffer.getBuffer(RenderType.debugQuads());
                for (BlockPos pos : routes) {
                    var state = mc.level.getBlockState(pos);
                    Vec3 center = Vec3.atCenterOf(pos).subtract(camera);
                    double distance = center.length();
                    if (distance < .75 || distance >= 16) continue;
                    boolean node = state.is(BlockInit.synaptic_node.get());
                    if (node != (pass == 1) || !node && distance < 1.5) continue;
                    if (!node && !state.is(BlockInit.nerve_fiber.get()) && !state.is(BlockInit.nerve_bundle.get())) continue;
                    float alpha = (float) Math.clamp((16 - distance) / (node ? 2 : 8), 0, 1);
                    double x = node ? .34 : .09, y = x, z = x;
                    if (!node) {
                        Direction.Axis axis = state.getValue(RotatedPillarBlock.AXIS);
                        if (axis == Direction.Axis.X) x = .5;
                        if (axis == Direction.Axis.Y) y = .5;
                        if (axis == Direction.Axis.Z) z = .5;
                    }
                    box(vertices, matrix, center, x, y, z, node ? alpha : 0, node ? 0 : alpha, 0, 1);
                }
                buffer.endBatch(RenderType.debugQuads());
            }
        } finally { main.bindWrite(false); }
    }
    public static boolean renderGrade(GuiGraphics graphics, int width, int height, float partialTick) {
        if (!AxonalTransductionClient.active() || scene == null || mask == null) return false;
        var shader = ShaderInit.AXONAL_TRAVEL_VIEW.getInstance().get();
        if (shader == null) return false;
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, scene.frameBufferId);
        GL30.glBlitFramebuffer(0,0,main.width,main.height,0,0,scene.width,scene.height,GL11.GL_COLOR_BUFFER_BIT,GL11.GL_NEAREST);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
        main.bindWrite(false);
        shader.safeGetUniform("HemoTime").set(mc.level.getGameTime() + partialTick);
        RenderSystem.setShaderTexture(0, scene.getColorTextureId());
        RenderSystem.setShaderTexture(1, mask.getColorTextureId());
        RenderSystem.disableBlend(); RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderInit.AXONAL_TRAVEL_VIEW.getInstance());
        RenderSystem.setShaderColor(1,1,1,1);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        b.addVertex(matrix,0,height,-90).setUv(0,1).setColor(1F,1F,1F,1F);
        b.addVertex(matrix,width,height,-90).setUv(1,1).setColor(1F,1F,1F,1F);
        b.addVertex(matrix,width,0,-90).setUv(1,0).setColor(1F,1F,1F,1F);
        b.addVertex(matrix,0,0,-90).setUv(0,0).setColor(1F,1F,1F,1F);
        BufferUploader.drawWithShader(b.buildOrThrow());
        RenderSystem.enableDepthTest(); RenderSystem.enableBlend();
        return true;
    }
    private static void box(VertexConsumer v, Matrix4f m, Vec3 c, double x, double y, double z,
                            float r, float g, float b, float a) {
        for (int[] face : FACES) for (int index : face) {
            float[] p = CORNERS[index];
            v.addVertex(m,(float)(c.x+p[0]*x),(float)(c.y+p[1]*y),(float)(c.z+p[2]*z)).setColor(r,g,b,a);
        }
    }
}
