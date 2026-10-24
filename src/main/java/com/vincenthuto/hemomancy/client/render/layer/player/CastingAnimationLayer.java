package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.client.model.morphling.MorphlingPlayerPartVisibility;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringAnchors;
import com.vincenthuto.hemomancy.client.render.world.CastingAnatomyRenderer;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastLane;
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
        // Hand-anchored visuals (conjured weapons, cell hands, mending) grow from the posed hand even outside a cast.
        if(!player.isInvisible()) {
            OutpouringAnchors.recordHand(player,getParentModel(),poses);
            // Conjured weapons harden on the weapon as it is actually held.
            for(var arm:HumanoidArm.values())
                com.vincenthuto.hemomancy.client.render.item.harbinger.HeldWeaponPose.recordThirdPerson(player,getParentModel(),poses,arm);
        }
        var animation=CastingAnimationClientState.animation(player);
        if(animation==null || player.isInvisible() || CastingPlayerPose.exclusive(player))return;
        // Harbinger casts are drawn by the outpouring (veins layer and world renderer); this layer only records where the
        // posed body opens, then keeps the Still Art rings.
        if(animation.packet().lane()!=CastLane.UNSTAINED) { OutpouringAnchors.capture(player,getParentModel(),poses,partial); return; }
        float time=animation.playback.elapsed(animation.now(),partial),weight=animation.weight(partial);
        for(var arm:HumanoidArm.values()) {
            if(!CastingPlayerPose.armAvailable(player,arm,partial) || MorphlingPlayerPartVisibility.shouldHideHeldItem(arm))continue;
            if(arm!=player.getMainArm() && !FirstPersonHandRules.affectsOffHand(animation.packet().rank(),animation.packet().stage()))continue;
            poses.pushPose();
            getParentModel().translateToHand(arm,poses);
            poses.translate(arm==HumanoidArm.RIGHT?-.06:.06,.55,0);
            CastingAnatomyRenderer.stillHand(poses,buffer,animation.packet(),time,weight,false);
            poses.popPose();
        }
        double distance=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(player.position());
        if(distance>48*48)return;
        // Player layers use downward Y; anatomy uses an upright origin at the player's feet.
        poses.pushPose();
        poses.translate(0,1.5,0);
        poses.scale(1,-1,1);
        // Still Arts have no vascular anatomy: only the pale still-water ring at the feet.
        CastingAnatomyRenderer.stillGround(poses,buffer,animation.packet(),time,weight,distance);
        poses.popPose();
    }
}
