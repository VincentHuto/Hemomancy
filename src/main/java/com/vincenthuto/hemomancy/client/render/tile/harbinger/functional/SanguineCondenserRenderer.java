package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.BloodPoolSurface;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.SanguineSphereRenderer;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * The blood a Condenser holds, the formation setting at its centre, and the finished formations resting on the
 * surface. The vessel itself is the baked block model (the vanilla cauldron at stage 0).
 */
public class SanguineCondenserRenderer implements BlockEntityRenderer<SanguineCondenserBlockEntity> {
    /** The cauldron's open mouth runs from 2/16 to 14/16 at every stage; the square pool fills it wall to wall. */
    private static final float POOL_HALF_WIDTH = 6.0F / 16.0F;

    private final ItemRenderer items;

    public SanguineCondenserRenderer(BlockEntityRendererProvider.Context context) {
        this.items = context.getItemRenderer();
    }

    @Override
    public void render(SanguineCondenserBlockEntity condenser, float partialTick, PoseStack poses,
            MultiBufferSource buffer, int light, int overlay) {
        if (condenser.getLevel() == null) return;
        double ticks = condenser.getLevel().getGameTime() + partialTick;
        double max = condenser.getMaxBloodVolume();
        float fill = max <= 0.0D ? 0.0F : (float) Mth.clamp(condenser.getBloodVolume() / max, 0.0D, 1.0D);
        float surface = (float) SanguineCondenserRules.surfaceHeight(fill);
        int seed = (int) Math.floorMod(condenser.getBlockPos().asLong(), 1000L);
        float progress = condenser.progress();
        Matrix4f pose = poses.last().pose();

        if (fill > 0.002F) {
            // A setting formation stirs the blood around it; a full vessel stands darker and more opaque.
            BloodPoolSurface.draw(pose, buffer.getBuffer(HemoRenderTypes.squareBloodPool((float) (ticks % 24000.0D), seed)),
                    0.5F, surface, 0.5F, POOL_HALF_WIDTH, Math.round(30 + 110 * progress), 0, 0, Math.round(190 + 60 * fill));
        }
        if (progress > 0.0F) renderFormingBead(poses, buffer, surface, progress, (float) ticks, seed);
        renderHeldFormations(condenser, poses, buffer, surface, seed, light, overlay);
    }

    /** The bead coalesces from a loose glow into a dark, tight core as the formation sets. */
    private static void renderFormingBead(PoseStack poses, MultiBufferSource buffer, float surface, float progress,
            float time, int seed) {
        float coalesced = progress * progress;
        float radius = Mth.lerp(coalesced, 0.04F, 0.11F) * (1.0F + 0.05F * Mth.sin(time * 0.35F + (seed & 15)));
        poses.pushPose();
        poses.translate(0.5F, surface + radius * 0.55F, 0.5F);
        Matrix4f matrix = poses.last().pose();
        SanguineSphereRenderer.renderSphere(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), matrix, radius, time,
                seed, Mth.lerp(progress, 0.95F, 0.36F), Mth.lerp(progress, 0.05F, 0.0F), Mth.lerp(progress, 0.05F, 0.0F),
                0.4F + 0.5F * progress);
        SanguineSphereRenderer.renderSphere(buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), matrix,
                radius + 0.08F * (1.0F - coalesced), time, seed + 11L, 0.55F, 0.02F, 0.02F, 0.3F * (1.0F - progress) + 0.04F);
        poses.popPose();
    }

    /** Finished formations lie on the surface in a ring (or alone at the centre), each turned its own way. */
    private void renderHeldFormations(SanguineCondenserBlockEntity condenser, PoseStack poses, MultiBufferSource buffer,
            float surface, int seed, int light, int overlay) {
        int held = condenser.heldFormations();
        if (held <= 0) return;
        ItemStack formation = new ItemStack(ItemInit.sanguine_formation.get());
        // Smaller and wider-set as more wait, so a full Druse still reads as separate stones.
        float scale = held == 1 ? 0.3F : held <= 4 ? 0.24F : 0.19F;
        float ring = held == 1 ? 0.0F : held <= 4 ? 0.17F : 0.25F;
        for (int i = 0; i < held; i++) {
            float angle = (float) (Math.PI * 2.0D * i / held) + (seed & 7) * 0.4F;
            poses.pushPose();
            poses.translate(0.5F + Mth.cos(angle) * ring, surface + 0.03F, 0.5F + Mth.sin(angle) * ring);
            poses.mulPose(Axis.YP.rotationDegrees((seed * 37 + i * 71) % 360));
            poses.mulPose(Axis.XP.rotationDegrees(90.0F));
            poses.scale(scale, scale, scale);
            items.renderStatic(null, formation, ItemDisplayContext.FIXED, false, poses, buffer, condenser.getLevel(),
                    light, overlay, i);
            poses.popPose();
        }
    }
}
