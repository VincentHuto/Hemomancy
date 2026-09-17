package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.client.morphling.MorphlingPlayerPartVisibility;
import com.vincenthuto.hemomancy.client.render.world.CastingAnatomyRenderer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;

public final class CastingAnimationLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    public CastingAnimationLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent) { super(parent); }
    @Override public void render(PoseStack poses,MultiBufferSource buffer,int light,AbstractClientPlayer player,
            float limbSwing,float limbSwingAmount,float partial,float age,float yaw,float pitch) {
        var animation=CastingAnimationClientState.animation(player);
        if(animation==null || player.isInvisible() || CastingPlayerPose.exclusive(player))return;
        float time=animation.playback.elapsed(animation.now(),partial),weight=animation.weight(partial);
        for(var arm:HumanoidArm.values()) {
            if(!CastingPlayerPose.armAvailable(player,arm,partial) || MorphlingPlayerPartVisibility.shouldHideHeldItem(arm))continue;
            if(animation.packet().rank()==com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.HUMILIS
                    && arm!=player.getMainArm())continue;
            poses.pushPose();
            getParentModel().translateToHand(arm,poses);
            poses.translate(arm==HumanoidArm.RIGHT?-.06:.06,.55,0);
            CastingAnatomyRenderer.hand(poses,buffer,animation.packet(),time,weight,false);
            poses.popPose();
        }
        double distance=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(player.position());
        if(distance>48*48)return;
        // Player layers use downward Y; anatomy uses an upright origin at the player's feet.
        poses.pushPose();
        // Lux wings attach to the back, including the torso's crouch pivot and casting twist.
        if(animation.packet().school()==EnumBloodTendency.LUX) getParentModel().body.translateAndRotate(poses);
        poses.translate(0,1.5,0);
        poses.scale(1,-1,1);
        CastingAnatomyRenderer.body(poses,buffer,animation.packet(),time,weight,distance);
        poses.popPose();
    }
}
