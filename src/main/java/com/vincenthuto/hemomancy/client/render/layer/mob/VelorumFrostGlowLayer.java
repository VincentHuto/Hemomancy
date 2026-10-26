package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumAnimState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;

/** Ice cores and sparse frost glints share the fitted mask; the shadow vestments stay dark. */
public class VelorumFrostGlowLayer<T extends VelorumEntity> extends RenderLayer<T, VelorumModel<T>> {
    private static final RenderType GLOW = RenderType.eyes(
            Hemomancy.rloc("textures/entity/boss/velorum/velorum_glow.png"));

    public VelorumFrostGlowLayer(RenderLayerParent<T, VelorumModel<T>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity,
                       float limbSwing, float limbAmount, float partialTick, float age, float yaw, float pitch) {
        if (entity.isInvisible()) return;
        var motion=VelorumAnimState.of(entity);motion.update(entity,partialTick);
        var action=VelorumVisualRules.pose(motion.phase());
        float frost=Math.max(action.gather(),action.frost());
        float pulse=(Mth.sin(age*.055F)+1)*.5F;
        int alpha=(int)Math.min(255,135+pulse*35+frost*85+action.silence()*70+action.martyr()*50);
        int color=FastColor.ARGB32.color(alpha,205,225,255);
        getParentModel().renderToBuffer(poseStack, buffer.getBuffer(GLOW), 0x00F000F0,
                OverlayTexture.NO_OVERLAY, color);
    }
}
