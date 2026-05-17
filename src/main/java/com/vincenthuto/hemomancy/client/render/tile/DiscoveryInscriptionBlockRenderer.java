package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.block.harbinger.EngramTextureCache;
import com.vincenthuto.hemomancy.common.block.shared.DiscoveryInscriptionBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.DiscoveryInscriptionBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

/**
 * Renders the blood_echo_inscription block as a constant glowing engram glyph
 * on the wall face, using per-pixel quads derived from {@link EngramTextureCache}.
 *
 * <p>The glyph is chosen deterministically from the inscription's resource path
 * so each unique inscription shows a distinct engram character. Two render passes
 * are used: a bright core pass and a slightly-expanded glow pass, both animated
 * with a slow pulse.</p>
 */
public class DiscoveryInscriptionBlockRenderer implements BlockEntityRenderer<DiscoveryInscriptionBlockEntity> {

    // Deep crimson palette for the Harbinger blood-echo aesthetic
    private static final float CORE_R = 0.80f, CORE_G = 0.05f, CORE_B = 0.05f;
    private static final float GLOW_R = 0.55f, GLOW_G = 0.02f, GLOW_B = 0.02f;

    private static final float BASE_ALPHA = 0.72f;
    private static final float PULSE_AMP = 0.18f;
    private static final float PULSE_SPEED = 0.04f;

    /**
     * Depth of the inscription face in local block space (block spans 0→1 on each
     * axis). The glyph is drawn just inside the model's thin slab, at ≈14/16 for
     * the far side and ≈2/16 for the near side.
     */
    private static final float OUTER_Z = 14.0f / 16.0f;
    private static final float INNER_Z = 2.0f / 16.0f;

    /** Expansion in block units added per pixel for the glow pass. */
    private static final float GLOW_EXPAND = 0.6f / 16.0f;

    public DiscoveryInscriptionBlockRenderer(BlockEntityRendererProvider.Context ctx) {
        EngramTextureCache.loadAll();
    }

    @Override
    public void render(DiscoveryInscriptionBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        BlockState state = be.getBlockState();

        // Only handle wall-mounted blood-echo inscriptions through this renderer
        if (state.getBlock() != BlockInit.blood_echo_inscription.get()) {
            return;
        }

        Direction facing = state.getValue(DiscoveryInscriptionBlock.FACING);

        // Derive a stable engram glyph index (0–25) from the inscription's resource path
        int engramIndex = Math.abs(be.getInscriptionId().getPath().hashCode()) % 26;

        // Lazy-load in case the renderer was created before ServerAboutToStartEvent
        EngramTextureCache.loadAll();
        boolean[][] pixels = EngramTextureCache.getPixels(engramIndex);
        int[][] colors = EngramTextureCache.getColors(engramIndex);
        if (pixels == null || colors == null) {
            return;
        }

        // Pulsing brightness over time
        Minecraft mc = Minecraft.getInstance();
        float gameTime = (mc.level != null ? mc.level.getGameTime() : 0L) + partialTick;
        float pulse = BASE_ALPHA + PULSE_AMP * Mth.sin(gameTime * PULSE_SPEED);

        Matrix4f mat = poseStack.last().pose();
        VertexConsumer vcCore = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);
        VertexConsumer vcGlow = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);

        for (int px = 0; px < 16; px++) {
            for (int py = 0; py < 16; py++) {
                if (!pixels[px][py]) {
                    continue;
                }

                // Use engram texture brightness to modulate color per pixel
                int argb = colors[px][py];
                float brightness = (((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF)) / 765.0f;
                float scale = 0.55f + 0.45f * brightness;

                // Core pass
                emitFacingPixel(vcCore, mat, facing, px, py,
                        CORE_R * scale, CORE_G * scale, CORE_B * scale,
                        pulse * 0.92f, 0f);

                // Glow pass (slightly expanded, lower alpha)
                emitFacingPixel(vcGlow, mat, facing, px, py,
                        GLOW_R * scale, GLOW_G * scale, GLOW_B * scale,
                        pulse * 0.28f, GLOW_EXPAND);
            }
        }
    }

    /**
     * Emits a single pixel quad on the correct wall face for the given
     * {@link Direction}, with optional edge expansion for the glow pass.
     *
     * <p>Coordinate convention in local block space (origin at block corner,
     * each axis 0→1 per block):</p>
     * <ul>
     *   <li><b>SOUTH</b> – face at z≈OUTER_Z, x=left→right, y=bottom→top</li>
     *   <li><b>NORTH</b> – face at z≈INNER_Z, x mirrored (east is "left" to reader)</li>
     *   <li><b>EAST</b>  – face at x≈OUTER_Z, px→z axis, y=bottom→top</li>
     *   <li><b>WEST</b>  – face at x≈INNER_Z, px→z mirrored</li>
     * </ul>
     */
    private static void emitFacingPixel(VertexConsumer vc, Matrix4f mat,
                                        Direction facing, int px, int py,
                                        float r, float g, float b, float a,
                                        float expand) {
        // Vertical mapping: texture top (py=0) → world high y
        float y0 = (15 - py) / 16.0f - expand;
        float y1 = (16 - py) / 16.0f + expand;

        float u0 = px / 16.0f - expand;
        float u1 = (px + 1) / 16.0f + expand;
        // Mirrored u (used for NORTH and WEST so the glyph reads correctly)
        float um0 = (15 - px) / 16.0f - expand;
        float um1 = (16 - px) / 16.0f + expand;

        switch (facing) {
            case SOUTH ->
                emitQuad(vc, mat,
                        u0, y0, OUTER_Z,  u1, y0, OUTER_Z,
                        u1, y1, OUTER_Z,  u0, y1, OUTER_Z,
                        r, g, b, a);
            case NORTH ->
                emitQuad(vc, mat,
                        um0, y0, INNER_Z,  um1, y0, INNER_Z,
                        um1, y1, INNER_Z,  um0, y1, INNER_Z,
                        r, g, b, a);
            case EAST ->
                // px drives the z axis; viewer standing east looks west, left = north = low z
                emitQuad(vc, mat,
                        OUTER_Z, y0, u0,  OUTER_Z, y0, u1,
                        OUTER_Z, y1, u1,  OUTER_Z, y1, u0,
                        r, g, b, a);
            case WEST ->
                // mirror z for correct left-to-right reading when facing west
                emitQuad(vc, mat,
                        INNER_Z, y0, um0,  INNER_Z, y0, um1,
                        INNER_Z, y1, um1,  INNER_Z, y1, um0,
                        r, g, b, a);
            default -> { /* no-op for UP/DOWN */ }
        }
    }

    private static void emitQuad(VertexConsumer vc, Matrix4f mat,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3,
                                 float x4, float y4, float z4,
                                 float r, float g, float b, float a) {
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
        vc.addVertex(mat, x3, y3, z3).setColor(r, g, b, a);
        vc.addVertex(mat, x4, y4, z4).setColor(r, g, b, a);
    }

    @Override
    public int getViewDistance() {
        return 48;
    }
}
