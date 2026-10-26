package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.LatentAnnettaInfectionModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.BloodPoolSurface;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaEncounterRules;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentAnnettaInfectionEntity;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentCombatRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class LatentAnnettaInfectionRenderer extends MobRenderer<LatentAnnettaInfectionEntity, LatentAnnettaInfectionModel> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/boss/annetta/latent_annetta_infection.png");
    private static final RenderType GLOW =
            RenderType.eyes(Hemomancy.rloc("textures/entity/boss/annetta/latent_annetta_infection_glow.png"));
    /** The rig is built at half size. */
    private static final float SCALE = 2.0F;
    /** Blocks: the infected blood that seeps out round it wherever it goes. */
    private static final float STAIN_RADIUS = 2.6F;

    public LatentAnnettaInfectionRenderer(EntityRendererProvider.Context context) {
        super(context, new LatentAnnettaInfectionModel(context.bakeLayer(LatentAnnettaInfectionModel.LAYER_LOCATION)), 0.9F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
        this.addLayer(new LatentInfectionLayer(this));
    }

    @Override
    public void render(LatentAnnettaInfectionEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        renderStain(entity, partialTick, poseStack, buffer);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    /** It bleeds where it stands: the infected pool's surface, spreading as it tears free and drying as it dies. */
    private static void renderStain(LatentAnnettaInfectionEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        float spread = HemoEasing.smoothstep((entity.emergeTime(partialTick) - LatentCombatRules.EMERGE_HIDDEN) / 14.0F);
        float dying = entity.deathTime > 0 ? 1.0F - Math.min(1.0F, (entity.deathTime + partialTick) / 20.0F) : 1.0F;
        if (spread <= 0.0F || dying <= 0.0F) return;
        float shaderTime = (float) (AnimationTime.gameTime(entity.level(), partialTick) % 24000.0D);
        int seed = Math.floorMod(entity.getId() * 53, 1000);
        BloodPoolSurface.draw(poseStack.last().pose(), buffer.getBuffer(HemoRenderTypes.infectedPool(shaderTime, seed)),
                0.0F, 0.004F, 0.0F, STAIN_RADIUS * (0.4F + 0.6F * spread), 0, 40, 0, Math.round(225 * dying));
    }

    @Override
    public ResourceLocation getTextureLocation(LatentAnnettaInfectionEntity entity) {
        return TEXTURE;
    }

    /** It does not topple like a body: its coils give way under it (see the model's dying slump). */
    @Override
    protected float getFlipDegrees(LatentAnnettaInfectionEntity entity) {
        return 0.0F;
    }

    @Override
    protected void scale(LatentAnnettaInfectionEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
        if (entity.isEmerging()) {
            // It rises up out of the floor behind her; below the boards until it tears free.
            float rise = LatentCombatRules.rise(entity.emergeTime(partialTick));
            poseStack.translate(0.0F, (1.0F - rise) * 2.4F, 0.0F);
        }
    }
}
