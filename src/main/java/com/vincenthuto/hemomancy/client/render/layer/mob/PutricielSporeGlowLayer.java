package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules;

/** The fitted mask lights spore nodules and censer offerings while leaving the vestments and shelves dark. */
public class PutricielSporeGlowLayer<T extends PutricielEntity> extends RenderLayer<T, PutricielModel<T>> {
    private static final RenderType GLOW = RenderType.eyes(
            Hemomancy.rloc("textures/entity/boss/putriciel/putriciel_glow.png"));

    public PutricielSporeGlowLayer(RenderLayerParent<T, PutricielModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity,
                       float limbSwing, float limbAmount, float partialTick, float age, float yaw, float pitch) {
        if (entity.isInvisible()) return;
        var motion = PutricielAnimState.of(entity);
        motion.update(entity, partialTick);
        var action = PutricielVisualRules.pose(entity.getVisualState(), entity.isAbsolved(), entity.getAbsolutionCount(),
                motion.visual(), motion.window(), motion.nova(), motion.release());
        float pulse = (Mth.sin(age * .11F) + 1) * .5F;
        float heat = Math.max(action.gather(), action.release());
        int alpha = (int) Math.min(255, 85 + pulse * 40 + heat * 130 + action.offering() * 90 + action.flinch() * 60);
        int color = FastColor.ARGB32.color(alpha, 255, (int) (215 - heat * 115), (int) (125 - heat * 65));
        getParentModel().renderToBuffer(poseStack, buffer.getBuffer(GLOW), 0x00F000F0,
                OverlayTexture.NO_OVERLAY, color);
    }
}
