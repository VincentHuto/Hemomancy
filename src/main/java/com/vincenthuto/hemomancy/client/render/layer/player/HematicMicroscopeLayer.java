package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.morphling.MorphlingPlayerPartVisibility;
import com.vincenthuto.hemomancy.client.player.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class HematicMicroscopeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ModelResourceLocation LEFT_MODEL = ModelResourceLocation.standalone(Hemomancy.rloc("item/hematic_microscope_viewing_left"));
    public HematicMicroscopeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) { super(parent); }
    @Override public void render(PoseStack poses, MultiBufferSource buffer, int light, AbstractClientPlayer player,
            float swing, float amount, float partialTick, float age, float yaw, float pitch) {
        var animation = HematicMicroscopeClientState.animation(player);
        if (animation == null || player.isInvisible()) return;
        boolean right = animation.packet.right();
        float tick = animation.poseTick(partialTick);
        var instrument = new PoseStack();
        HematicMicroscopePose.thirdPersonInstrument(instrument, getParentModel(), tick, right);
        var vial = new PoseStack();
        HematicMicroscopePose.vial(vial, instrument, tick, right);
        renderHeld(poses, buffer, light, player, animation.packet.instrument(), instrument, tick, right, !right);
        renderHeld(poses, buffer, light, player, animation.packet.sample(), vial, tick, !right, false);
    }
    private void renderHeld(PoseStack poses, MultiBufferSource buffer, int light, AbstractClientPlayer player,
            ItemStack stack, PoseStack target, float tick, boolean right, boolean mirroredInstrument) {
        var arm = right ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        if (MorphlingPlayerPartVisibility.shouldHideHeldItem(arm)) return;
        var ordinary = new PoseStack();
        getParentModel().translateToHand(arm, ordinary);
        ordinary.mulPose(Axis.XP.rotationDegrees(-90));
        ordinary.mulPose(Axis.YP.rotationDegrees(180));
        ordinary.translate((right ? 1F : -1F) / 16, .125F, -.625F);
        var mc = Minecraft.getInstance();
        mc.getItemRenderer().getModel(stack, player.level(), player, player.getId()).getTransforms()
                .getTransform(right ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .apply(!right, ordinary);
        poses.pushPose();
        BloodVialInjectionPose.blendTransform(poses, ordinary, target, HematicMicroscopePose.weight(tick));
        renderItem(stack, mirroredInstrument, poses, buffer, light, player);
        poses.popPose();
    }
    public static void renderItem(ItemStack stack, boolean mirroredInstrument, PoseStack poses,
            MultiBufferSource buffer, int light, AbstractClientPlayer player) {
        var mc = Minecraft.getInstance();
        var model = mirroredInstrument ? mc.getModelManager().getModel(LEFT_MODEL)
                : mc.getItemRenderer().getModel(stack, player.level(), player, player.getId());
        mc.getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poses, buffer, light, OverlayTexture.NO_OVERLAY, model);
    }
}
