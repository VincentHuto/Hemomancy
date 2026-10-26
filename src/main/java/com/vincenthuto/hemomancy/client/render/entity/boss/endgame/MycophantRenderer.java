package com.vincenthuto.hemomancy.client.render.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.MycophantGlowLayer;
import com.vincenthuto.hemomancy.client.render.layer.mob.endgame.MycophantSacLayer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantState;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantYieldRules;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** The Spawn Mother at 2x: the generated model, its emissive glow layer and the translucent gestation sac. */
public class MycophantRenderer extends MobRenderer<MycophantEntity, MycophantModel> {
    private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/endgame/mycophant.png");
    /** How far she sinks into the nectar over the yielding, in blocks. */
    private static final float YIELD_SINK_BLOCKS = 2.5F;

    public MycophantRenderer(Context context) {
        super(context, new MycophantModel(context.bakeLayer(MycophantModel.LAYER_LOCATION)), 3.0F);
        this.addLayer(new MycophantGlowLayer(this));
        this.addLayer(new MycophantSacLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MycophantEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(MycophantEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
        if (entity.getState() == MycophantState.YIELD) {
            MycophantAnimState motion = MycophantAnimState.of(entity);
            motion.update(entity, partialTick);
            float sink = MycophantYieldRules.sink(motion.yieldTime()) * YIELD_SINK_BLOCKS;
            poseStack.translate(0.0F, sink / 2.0F, 0.0F);   // the stack is flipped here: +Y is down
        }
    }

    /** She never topples: the yielding replaces the vanilla death flip. */
    @Override
    protected float getFlipDegrees(MycophantEntity entity) {
        return 0.0F;
    }
}
