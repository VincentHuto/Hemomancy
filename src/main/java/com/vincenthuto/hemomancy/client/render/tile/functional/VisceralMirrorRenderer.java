package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.block.VisceralMirrorModel;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.VisceralMirrorBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Block entity renderer for the Visceral Mirror — renders a stencil-clipped
 * player reflection on a 1.6-block-tall × 1-block-wide mirror surface, plus
 * a translucent tinted glass overlay.
 *
 * <h3>Rendering approach</h3>
 * <ol>
 *   <li>Write the mirror quad shape into the <b>stencil buffer</b>
 *       (Tesselator immediate draw — no color/depth output).</li>
 *   <li>With stencil in read mode, render the player entity reflected
 *       across the mirror plane. The stencil clips the model cleanly
 *       to the mirror bounds.</li>
 *   <li>Disable stencil and draw a translucent glass overlay through
 *       the normal batched buffer.</li>
 * </ol>
 */
public class VisceralMirrorRenderer implements BlockEntityRenderer<VisceralMirrorBlockEntity> {

    // =====================================================================
    //  Mirror layout — adjust these values to align with your block model
    // =====================================================================

    /**
     * Beyond this distance² the reflection is not rendered.
     */
    private static final double MAX_REFLECTION_DIST_SQ = 64.0; // 8 blocks
    // ── Glass overlay colours ────────────────────────────────────────────
    private static final float GLASS_R = 0.35f;
    /**
     * Primary wave frequency — controls how many ripples fit across the surface.
     */
    private static final float RIPPLE_FREQ_X = 6.0f;
    private static final float RIPPLE_FREQ_Y = 4.5f;
    /**
     * Secondary (detail) wave frequency for organic layering.
     */
    private static final float RIPPLE_FREQ2_X = 11.0f;
    private static final float RIPPLE_FREQ2_Y = 8.0f;
    /**
     * Speed at which the ripples scroll (radians per tick).
     */
    private static final float RIPPLE_SPEED = 0.07f;

    // ── Ripple / swimming effect parameters ──────────────────────────────
    private static final float RIPPLE_SPEED2 = -0.045f;
    /**
     * Extra alpha variation on ripple crests for a shimmer highlight.
     */
    private static final float RIPPLE_ALPHA_BOOST = 0.06f;
    // ── Stand model ──────────────────────────────────────────────────────
    private static final ResourceLocation MODEL_TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/entity/model_visceral_mirror.png");
    // ── Distance limits ──────────────────────────────────────────────────
    private static final float GLASS_G = 0.08f;
    private static final float GLASS_B = 0.12f;
    private static final float GLASS_A = 0.18f;
    /**
     * Number of horizontal subdivisions for the ripple mesh.
     */
    private static final int RIPPLE_COLS = 12;
    /**
     * Number of vertical subdivisions for the ripple mesh.
     */
    private static final int RIPPLE_ROWS = 20;
    /**
     * Maximum Z displacement of a ripple crest (in blocks).
     */
    private static final float RIPPLE_AMPLITUDE = 0.012f;
    // ┌──────────────────────────────────────────────────────────────────┐
    // │  ★  TWEAK THESE VALUES to align the render with your model  ★   │
    // └──────────────────────────────────────────────────────────────────┘
    private static final MirrorLayout LAYOUT = new MirrorLayout(
            0.25f,      // halfWidth         — half the mirror width (blocks)
            0.365f,    // yStart            — bottom edge Y (blocks above block floor)
            1.4f,      // height            — mirror height (blocks)
            0.15f,   // zOffset           — depth from block centre to mirror face
            0.0f,      // reflectionYOffset — extra Y nudge for the reflected player
            1.0f       // reflectionScale   — scale of the reflected player model
    );
    private final VisceralMirrorModel standModel;

    public VisceralMirrorRenderer(BlockEntityRendererProvider.Context ctx) {
        standModel = new VisceralMirrorModel(ctx.bakeLayer(VisceralMirrorModel.LAYER_LOCATION));
    }

    /**
     * The mirror surface extends above the block boundary.
     */
    @Override
    public boolean shouldRenderOffScreen(VisceralMirrorBlockEntity te) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public void render(VisceralMirrorBlockEntity te, float partialTicks, PoseStack stack,
                       MultiBufferSource buffer, int light, int overlay) {
        if (te.getLevel() == null) return;

        Direction facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        Player player = Minecraft.getInstance().player;

        // Render the stand / frame model
        renderStandModel(stack, buffer, facing, light, overlay);

        // Always draw the tinted glass overlay
        renderMirrorGlass(stack, buffer, facing, te);

        // Render the reflection only when a player is close and in front
        if (player != null && isPlayerInFront(te, player, facing)) {
            renderReflection(te, partialTicks, stack, facing, player, light);
        }
    }

    // =====================================================================
    //  Main render entry
    // =====================================================================

    /**
     * Renders the custom stand / frame model around the mirror.
     * <p>
     * The model is authored facing SOUTH (frame flat face perpendicular to X,
     * spanning Z). We rotate it around Y so the frame aligns with the block's
     * FACING direction. The model is flipped 180° on X because it is authored
     * Y-down (Blockbench convention).
     */
    private void renderStandModel(PoseStack stack, MultiBufferSource buffer,
                                  Direction facing, int light, int overlay) {
        stack.pushPose();

        // Centre on the block
        stack.translate(0.5D, 1.5D, 0.5D);

        // Flip model upside-down (Blockbench Y-down → world Y-up)
        stack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());

        // Rotate the model based on the block's facing direction.
        // The model's frame face points along -X; we rotate so that face
        // aligns with the block's FACING direction (the side the player sees).
        float yRot = switch (facing) {
            case SOUTH -> 270f;
            case WEST -> 0f;
            case NORTH -> 90f;
            case EAST -> 180f;
            default -> 180f;
        };
        stack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(MODEL_TEXTURE));
        standModel.renderToBuffer(stack, vc, light, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        stack.popPose();
    }

    // =====================================================================
    //  Stand / frame model rendering
    // =====================================================================

    private void renderReflection(VisceralMirrorBlockEntity te, float partialTicks,
                                  PoseStack stack, Direction facing, Player player, int light) {

        Minecraft mc = Minecraft.getInstance();
        mc.getMainRenderTarget().enableStencil();

        // Animated time for ripple synchronisation
        float time = te.getLevel() != null
                ? te.getLevel().getGameTime() + mc.getFrameTime()
                : 0f;

        // ── Phase 1: write mirror quad into stencil ──────────────────────
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);

        drawStencilQuad(stack, facing, time);

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);

        // ── Phase 2: render reflected player within stencil ──────────────
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilMask(0x00);

        renderReflectedPlayer(te, partialTicks, stack, facing, player, light);

        // ── Phase 3: disable stencil ─────────────────────────────────────
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    // =====================================================================
    //  Stencil-based reflection pipeline
    // =====================================================================

    private void drawStencilQuad(PoseStack stack, Direction facing, float time) {
        stack.pushPose();
        applyFacingTransform(stack, facing);

        Matrix4f mat = stack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        // Subdivide to match ripple mesh so reflection clips to the swimming surface
        float cellW = (LAYOUT.halfWidth() * 2f) / RIPPLE_COLS;
        float cellH = LAYOUT.height() / RIPPLE_ROWS;

        for (int row = 0; row < RIPPLE_ROWS; row++) {
            for (int col = 0; col < RIPPLE_COLS; col++) {
                float x0 = -LAYOUT.halfWidth() + col * cellW;
                float x1 = x0 + cellW;
                float y0 = LAYOUT.yStart() + row * cellH;
                float y1 = y0 + cellH;

                float z00 = rippleZ(x0, y0, time, 1f);
                float z10 = rippleZ(x1, y0, time, 1f);
                float z11 = rippleZ(x1, y1, time, 1f);
                float z01 = rippleZ(x0, y1, time, 1f);

                // Front face
                builder.vertex(mat, x0, y0, z00).endVertex();
                builder.vertex(mat, x0, y1, z01).endVertex();
                builder.vertex(mat, x1, y1, z11).endVertex();
                builder.vertex(mat, x1, y0, z10).endVertex();

                // Back face
                builder.vertex(mat, x1, y0, z10).endVertex();
                builder.vertex(mat, x1, y1, z11).endVertex();
                builder.vertex(mat, x0, y1, z01).endVertex();
                builder.vertex(mat, x0, y0, z00).endVertex();
            }
        }

        Tesselator.getInstance().end();

        stack.popPose();
    }

    // =====================================================================
    //  Stencil mask quad (Tesselator — drawn immediately)
    // =====================================================================

    /**
     * Renders the client player reflected across the mirror surface.
     * <p>
     * The reflection is achieved by:
     * <ol>
     *   <li>Calculating the player's position mirrored across the plane.</li>
     *   <li>Applying a negative scale on the mirror's normal axis, which
     *       geometrically mirrors the entire model (including rotations,
     *       limb positions and held items).</li>
     * </ol>
     * The stencil buffer (set up by the caller) clips the result to the
     * mirror bounds.
     */
    private void renderReflectedPlayer(VisceralMirrorBlockEntity te, float partialTicks,
                                       PoseStack stack, Direction facing, Player player, int light) {

        BlockPos pos = te.getBlockPos();
        Vec3 playerPos = player.getPosition(partialTicks);

        // Player position in block-local coordinates
        double localX = playerPos.x - pos.getX();
        double localY = playerPos.y - pos.getY();
        double localZ = playerPos.z - pos.getZ();

        // Mirror surface coordinate along the normal axis (block-local)
        double mirrorCoord = getMirrorSurfaceCoord(facing);
        boolean flipZ = (facing == Direction.SOUTH || facing == Direction.NORTH);

        // Reflect position across mirror plane
        double reflX = localX;
        double reflY = localY;
        double reflZ = localZ;
        if (flipZ) {
            reflZ = 2.0 * mirrorCoord - localZ;
        } else {
            reflX = 2.0 * mirrorCoord - localX;
        }

        stack.pushPose();

        // Translate to the reflected position
        stack.translate(reflX, reflY, reflZ);

        // Apply mirror flip — negative scale on the normal axis
        if (flipZ) {
            stack.scale(1.0f, 1.0f, -1.0f);
        } else {
            stack.scale(-1.0f, 1.0f, 1.0f);
        }

        // Force every fragment to pass the depth test so that blocks placed
        // behind the mirror cannot occlude the reflection.
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        GL11.glDepthMask(false);

        // The negative scale reverses face winding — flip front face so
        // the base skin model renders correctly.
        GL11.glFrontFace(GL11.GL_CW);

        // ── Render ONLY the base player model (skin) — no armor/layers ──
        // This sidesteps the inside-out layer issue entirely.
        if (player instanceof net.minecraft.client.player.AbstractClientPlayer clientPlayer) {
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            var renderer = dispatcher.getRenderer(clientPlayer);
            if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
                var model = playerRenderer.getModel();

                // Pose the model to match the player's current animation state
                float bodyYaw = net.minecraft.util.Mth.rotLerp(partialTicks, clientPlayer.yBodyRotO, clientPlayer.yBodyRot);
                float headYaw = net.minecraft.util.Mth.rotLerp(partialTicks, clientPlayer.yHeadRotO, clientPlayer.yHeadRot);
                float headRelYaw = headYaw - bodyYaw;
                float headPitch = net.minecraft.util.Mth.lerp(partialTicks, clientPlayer.xRotO, clientPlayer.getXRot());
                float limbSwing = clientPlayer.walkAnimation.position(partialTicks);
                float limbSwingAmount = clientPlayer.walkAnimation.speed(partialTicks);
                float ageInTicks = clientPlayer.tickCount + partialTicks;

                model.attackTime = clientPlayer.getAttackAnim(partialTicks);
                model.riding = clientPlayer.isPassenger();
                model.young = clientPlayer.isBaby();
                model.crouching = clientPlayer.isCrouching();

                model.setupAnim(clientPlayer, limbSwing, limbSwingAmount, ageInTicks, headRelYaw, headPitch);
                model.prepareMobModel(clientPlayer, limbSwing, limbSwingAmount, partialTicks);

                // Hide all optional layers — only render the base skin parts
                model.hat.visible = false;
                model.jacket.visible = false;
                model.leftSleeve.visible = false;
                model.rightSleeve.visible = false;
                model.leftPants.visible = false;
                model.rightPants.visible = false;

                stack.pushPose();
                // Apply the same transform the vanilla player renderer uses:
                // flip upside-down and shift up by 1.501 blocks (entity height offset)
                stack.mulPose(Vector3.YP.rotationDegrees(180f - bodyYaw).toMoj());

                // Apply reflection scale from layout (uniform scale before the flip)
                float rs = LAYOUT.reflectionScale();
                stack.scale(-rs, -rs, rs);

                // Vanilla shifts the model down when crouching
                float yOffset = clientPlayer.isCrouching() ? -1.501f + 0.125f : -1.501f;
                // Apply the layout's extra Y nudge for alignment tweaking
                yOffset += LAYOUT.reflectionYOffset();
                stack.translate(0.0, yOffset, 0.0);

                ResourceLocation skin = playerRenderer.getTextureLocation(clientPlayer);
                MultiBufferSource.BufferSource bs = Minecraft.getInstance().renderBuffers().bufferSource();
                bs.endBatch(); // flush prior batches so our GL state doesn't corrupt them

                VertexConsumer vc = bs.getBuffer(RenderType.entityTranslucent(skin));
                model.renderToBuffer(stack, vc, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                        1.0f, 1.0f, 1.0f, 1.0f);
                bs.endBatch();

                // Restore layer visibility so the player's normal render isn't affected
                model.hat.visible = true;
                model.jacket.visible = true;
                model.leftSleeve.visible = true;
                model.rightSleeve.visible = true;
                model.leftPants.visible = true;
                model.rightPants.visible = true;

                stack.popPose();
            }
        }

        // Restore GL state
        GL11.glFrontFace(GL11.GL_CCW);
        GL11.glDepthMask(true);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        stack.popPose();
    }

    // =====================================================================
    //  Reflected player entity
    // =====================================================================

    private void renderMirrorGlass(PoseStack stack, MultiBufferSource buffer,
                                   Direction facing, VisceralMirrorBlockEntity te) {
        stack.pushPose();
        applyFacingTransform(stack, facing);

        Matrix4f mat = stack.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderTypeInit.LOOM_EFFECT);

        float r = GLASS_R;
        float g = GLASS_G;
        float b = GLASS_B;
        float baseA = GLASS_A;

        // Subtle pulse during an active ritual
        boolean ritualActive = te.getPhase() != VisceralMirrorBlockEntity.RitualPhase.IDLE
                && te.getLevel() != null;
        if (ritualActive) {
            float pulse = (float) (Math.sin(te.getLevel().getGameTime() * 0.1) * 0.08);
            baseA += pulse;
            r += 0.12f;
        }

        // Animated time value (ticks + partial for smooth motion)
        float time = te.getLevel() != null
                ? te.getLevel().getGameTime() + Minecraft.getInstance().getFrameTime()
                : 0f;

        // Increase ripple during ritual
        float ampScale = ritualActive ? 2.0f : 1.0f;

        // Subdivide the mirror surface into a grid of quads and displace
        // each vertex in Z with layered sine waves for a swimming/rippling look.
        float cellW = (LAYOUT.halfWidth() * 2f) / RIPPLE_COLS;
        float cellH = LAYOUT.height() / RIPPLE_ROWS;

        for (int row = 0; row < RIPPLE_ROWS; row++) {
            for (int col = 0; col < RIPPLE_COLS; col++) {
                float x0 = -LAYOUT.halfWidth() + col * cellW;
                float x1 = x0 + cellW;
                float y0 = LAYOUT.yStart() + row * cellH;
                float y1 = y0 + cellH;

                float z00 = rippleZ(x0, y0, time, ampScale);
                float z10 = rippleZ(x1, y0, time, ampScale);
                float z11 = rippleZ(x1, y1, time, ampScale);
                float z01 = rippleZ(x0, y1, time, ampScale);

                float a00 = baseA + rippleAlpha(x0, y0, time) * ampScale;
                float a10 = baseA + rippleAlpha(x1, y0, time) * ampScale;
                float a11 = baseA + rippleAlpha(x1, y1, time) * ampScale;
                float a01 = baseA + rippleAlpha(x0, y1, time) * ampScale;

                // Front face
                vc.vertex(mat, x0, y0, z00).color(r, g, b, a00).endVertex();
                vc.vertex(mat, x0, y1, z01).color(r, g, b, a01).endVertex();
                vc.vertex(mat, x1, y1, z11).color(r, g, b, a11).endVertex();
                vc.vertex(mat, x1, y0, z10).color(r, g, b, a10).endVertex();

                // Back face
                vc.vertex(mat, x1, y0, z10).color(r, g, b, a10).endVertex();
                vc.vertex(mat, x1, y1, z11).color(r, g, b, a11).endVertex();
                vc.vertex(mat, x0, y1, z01).color(r, g, b, a01).endVertex();
                vc.vertex(mat, x0, y0, z00).color(r, g, b, a00).endVertex();
            }
        }

        stack.popPose();
    }

    // =====================================================================
    //  Mirror glass overlay (batched — translucent tinted quad)
    // =====================================================================

    /**
     * Returns the Z displacement for a point (x, y) on the mirror surface
     * at the given animation time. Two sine layers are combined for an
     * organic, swimming look.
     */
    private float rippleZ(float x, float y, float time, float ampScale) {
        float wave1 = (float) Math.sin(x * RIPPLE_FREQ_X + y * RIPPLE_FREQ_Y + time * RIPPLE_SPEED);
        float wave2 = (float) Math.sin(x * RIPPLE_FREQ2_X - y * RIPPLE_FREQ2_Y + time * RIPPLE_SPEED2);
        return (wave1 * 0.65f + wave2 * 0.35f) * RIPPLE_AMPLITUDE * ampScale;
    }

    // =====================================================================
    //  Ripple displacement helpers
    // =====================================================================

    /**
     * Returns an alpha offset for the ripple shimmer highlight.
     */
    private float rippleAlpha(float x, float y, float time) {
        float wave = (float) Math.sin(x * RIPPLE_FREQ_X + y * RIPPLE_FREQ_Y + time * RIPPLE_SPEED);
        return wave * RIPPLE_ALPHA_BOOST;
    }

    /**
     * Applies a Y-axis rotation to the PoseStack so that the mirror surface
     * is always on the XY plane at z = +{@code LAYOUT.zOffset()} from the
     * block centre, facing outward.
     */
    private void applyFacingTransform(PoseStack stack, Direction facing) {
        stack.translate(0.5, 0.0, 0.5);
        // A Y-rotation of θ maps local +Z → world direction:
        //   0° → +Z (south),  90° → +X (east),  180° → -Z (north),  270° → -X (west)
        // We want the mirror surface pushed along the facing direction,
        // so the angle must map the FACING direction to local +Z.
        float angle = switch (facing) {
            case SOUTH -> 0f;
            case EAST -> 90f;
            case NORTH -> 180f;
            case WEST -> 270f;
            default -> 0f;
        };
        stack.mulPose(Vector3.YP.rotationDegrees(angle).toMoj());
        stack.translate(0.0, 0.0, LAYOUT.zOffset());
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    /**
     * Returns the mirror surface coordinate along its normal axis
     * (block-local, 0–1 range).
     */
    private double getMirrorSurfaceCoord(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0.5 + LAYOUT.zOffset();
            case NORTH -> 0.5 - LAYOUT.zOffset();
            case EAST -> 0.5 + LAYOUT.zOffset();
            case WEST -> 0.5 - LAYOUT.zOffset();
            default -> 0.5;
        };
    }

    /**
     * Returns {@code true} when the player is in front of the mirror
     * surface and within reflection range.
     */
    private boolean isPlayerInFront(VisceralMirrorBlockEntity te, Player player, Direction facing) {
        BlockPos pos = te.getBlockPos();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        if (player.distanceToSqr(cx, cy, cz) > MAX_REFLECTION_DIST_SQ) {
            return false;
        }

        Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 toPlayer = player.position().subtract(cx, cy, cz);
        return toPlayer.dot(normal) > 0;
    }

    /**
     * Centralised description of the mirror surface geometry.
     * <p>Change any of these values and every part of the renderer
     * (stencil quad, glass overlay, reflection position, facing
     * transform) will update automatically.</p>
     *
     * @param halfWidth         Half the mirror width.  0.5 = full block width (±0.5).
     * @param yStart            Bottom edge of the mirror surface in block-local Y.
     *                          e.g. 0.125 = 2 pixels above the block floor.
     * @param height            Height of the mirror surface in blocks.
     *                          e.g. 1.6 = extends 1.6 blocks above yStart.
     * @param zOffset           Distance from block centre (0.5) to the mirror face
     *                          along the facing axis.
     *                          e.g. 0.3125 = face sits at 13/16 of the block.
     * @param reflectionYOffset Additional Y shift applied to the reflected
     *                          player model (positive = up). Use this to nudge
     *                          the reflection vertically without moving the glass.
     * @param reflectionScale   Uniform scale of the reflected player model.
     *                          1.0 = normal size.
     */
    private record MirrorLayout(
            float halfWidth,
            float yStart,
            float height,
            float zOffset,
            float reflectionYOffset,
            float reflectionScale
    ) {
        float yEnd() {
            return yStart + height;
        }
    }
}
