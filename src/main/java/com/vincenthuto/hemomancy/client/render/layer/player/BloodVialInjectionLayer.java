package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.player.BloodVialInjectionClientState;
import com.vincenthuto.hemomancy.client.player.BloodVialInjectionPose;
import com.vincenthuto.hemomancy.client.morphling.MorphlingPlayerPartVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;

public final class BloodVialInjectionLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public BloodVialInjectionLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override public void render(PoseStack poses, MultiBufferSource buffer, int light, AbstractClientPlayer player,
            float limbSwing, float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
        var animation = BloodVialInjectionClientState.animation(player);
        if (animation == null || player.isInvisible() || MorphlingPlayerPartVisibility.shouldHideHeldItem(
                animation.packet.right() ? HumanoidArm.RIGHT : HumanoidArm.LEFT)) return;
        poses.pushPose();
        boolean right = animation.packet.right();
        float tick = animation.elapsed(partialTick);
        var injection = new PoseStack();
        BloodVialInjectionPose.vialTransform(injection, getParentModel(), tick, right);
        var ordinary = new PoseStack();
        getParentModel().translateToHand(right ? HumanoidArm.RIGHT : HumanoidArm.LEFT, ordinary);
        ordinary.mulPose(Axis.XP.rotationDegrees(-90));
        ordinary.mulPose(Axis.YP.rotationDegrees(180));
        ordinary.translate((right ? 1F : -1F) / 16, .125F, -.625F);
        var context = right ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        Minecraft.getInstance().getItemRenderer().getModel(animation.vial, player.level(), player, player.getId())
                .getTransforms().getTransform(context).apply(!right, ordinary);
        BloodVialInjectionPose.blendTransform(poses, ordinary, injection, BloodVialInjectionPose.motion(tick).weight());
        Minecraft.getInstance().getItemRenderer().renderStatic(animation.vial, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, poses, buffer, player.level(), player.getId());
        poses.popPose();
    }
}
