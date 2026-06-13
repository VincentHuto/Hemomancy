package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.HematicMemoryToolEvents;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class HuskEffigyControlGlowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE =
            Hemomancy.rloc("textures/entity/puppeteer_summon/husk_effigy_control_glow.png");
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final int CONTROL_RED = 0xAAFF2028;

    public HuskEffigyControlGlowLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!entity.getPersistentData().getBoolean(HematicMemoryToolEvents.TAG_EFFIGY) || entity.isInvisible()) {
            return;
        }
        float offset = (entity.tickCount + partialTicks) * 0.01F;
        VertexConsumer consumer = buffer.getBuffer(RenderType.energySwirl(TEXTURE, offset, offset * 0.45F));
        poseStack.pushPose();
        poseStack.scale(1.035F, 1.035F, 1.035F);
        this.getParentModel().renderToBuffer(poseStack, consumer, FULL_BRIGHT,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F), CONTROL_RED);
        poseStack.popPose();
    }
}
