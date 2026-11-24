package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.MycophantBroodLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.MycophantGlowLayer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantState;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantYieldRules;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** The Spawn Mother at the Blockbench scale of sixteen grid units to a block. */
public class MycophantRenderer extends MobRenderer<MycophantEntity, MycophantModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/endgame/mycophant.png");
    /** How far she sinks into the nectar over the yielding, in blocks. */
    private static final float YIELD_SINK_BLOCKS = 2.5F;

    public MycophantRenderer(Context context) {
        super(context, new MycophantModel(), 3.0F);
        this.addLayer(new MycophantGlowLayer(this));
        this.addLayer(new MycophantBroodLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(MycophantEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(MycophantEntity entity, PoseStack poseStack, float partialTick) {
        // Keep the Blockbench model's left and right after the living renderer turns it to face forward.
        poseStack.scale(-1.0F, 1.0F, 1.0F);
        if (entity.getState() == MycophantState.YIELD) {
            MycophantAnimState motion = MycophantAnimState.of(entity);
            motion.update(entity, partialTick);
            float sink = MycophantYieldRules.sink(motion.yieldTime()) * YIELD_SINK_BLOCKS;
            poseStack.translate(0.0F, sink, 0.0F);
        }
    }

    /** She never topples: the yielding replaces the vanilla death flip. */
    @Override
    protected float getFlipDegrees(MycophantEntity entity) {
        return 0.0F;
    }
}
