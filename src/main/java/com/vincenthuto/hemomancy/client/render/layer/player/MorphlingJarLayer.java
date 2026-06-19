package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.MorphlingJarModel;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MorphlingJarLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public static ResourceLocation texture = Hemomancy.rloc("textures/entity/model_morphling_jar.png");

    private final MorphlingJarModel<T> modelMorphlingJar;

    public MorphlingJarLayer(LivingEntityRenderer<T, M> owner) {
        super(owner);
        modelMorphlingJar = new MorphlingJarModel<>(
                Minecraft.getInstance().getEntityModels().bakeLayer(MorphlingJarModel.morphling_jar));
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int lightness, T ent, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!HemoClientConfig.RENDER_MORPHLING_JAR_LAYER.get()) {
            return;
        }
        if (ent instanceof Player player) {
            HemoCapabilityAccess.getEquipment(player).ifPresent(inv -> {
                var stack = inv.getStackInSlot(7);
                if (stack.getItem() instanceof ItemMorphlingJar jar) {
                    this.translateToBody(matrixStack);
                    VertexConsumer ivertexbuilder = buffer.getBuffer(RenderType.text(texture));
                    modelMorphlingJar.renderToBuffer(matrixStack, ivertexbuilder, lightness,
                            OverlayTexture.NO_OVERLAY, -1);
                }
            });
        }
    }

    private void translateToBody(PoseStack matrixStack) {
        this.getParentModel().body.translateAndRotate(matrixStack);
    }
}
