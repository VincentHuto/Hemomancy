package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.phlegethosoma.PhlegethosomaModel;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class PhlegethosomaGlowLayer extends RenderLayer<PhlegethosomaEntity,PhlegethosomaModel> {
    private static final RenderType GLOW=RenderType.eyes(Hemomancy.rloc("textures/entity/phlegethosoma/phlegethosoma_glow.png"));
    public PhlegethosomaGlowLayer(RenderLayerParent<PhlegethosomaEntity,PhlegethosomaModel> parent) {super(parent);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,PhlegethosomaEntity entity,float limb,float amount,float partial,float age,float yaw,float pitch) {
        if(!entity.isInvisible())getParentModel().renderToBuffer(pose,buffers.getBuffer(GLOW),0xF000F0,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
    }
}
