package com.vincenthuto.hemomancy.client.event;

import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.client.render.outpouring.CastVeinsLayer;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringClientState;
import com.vincenthuto.hemomancy.client.render.world.CastingAnatomyRenderer;
import com.vincenthuto.hemomancy.client.morphling.MorphlingPlayerPartVisibility;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastLane;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import com.vincenthuto.hemomancy.mixin.core.CastingHandRendererInvoker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class CastingAnimationClientEvents {
    private CastingAnimationClientEvents() {}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) { CastingAnimationClientState.tick(); OutpouringClientState.tick(); }
    @SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) {
        if(event.getLevel().isClientSide()) { CastingAnimationClientState.untrack(event.getEntity().getUUID()); OutpouringClientState.end(event.getEntity().getUUID()); }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void hand(RenderHandEvent event) {
        var mc=Minecraft.getInstance();
        var player=mc.player;
        var animation=CastingAnimationClientState.animation(player);
        if(animation==null || event.isCanceled())return;
        var arm=event.getHand()==InteractionHand.MAIN_HAND?player.getMainArm():player.getMainArm().getOpposite();
        if(arm!=player.getMainArm() && !FirstPersonHandRules.affectsOffHand(animation.packet().rank(),animation.packet().stage(),animation.packet().style()))return;
        if(!CastingPlayerPose.armAvailable(player,arm,event.getPartialTick()) || MorphlingPlayerPartVisibility.shouldHideHeldItem(arm))return;
        var hand=FirstPersonHandRules.clamp(FirstPersonHandRules.handFor(animation.handPose(event.getPartialTick()),
                arm==HumanoidArm.RIGHT,player.getMainArm()==HumanoidArm.LEFT));
        float side=arm==HumanoidArm.RIGHT?1:-1;
        var poses=event.getPoseStack();
        event.setCanceled(true);
        poses.pushPose();
        try {
        poses.translate(hand.tx(),hand.ty(),hand.tz());
        poses.mulPose(Axis.XP.rotation(hand.rx()));
        poses.mulPose(Axis.YP.rotation(hand.ry()));
        poses.mulPose(Axis.ZP.rotation(hand.rz()));
        if(event.getItemStack().isEmpty()) {
            poses.pushPose();
            poses.translate(side*.64000005,-.6-event.getEquipProgress()*.6,-.71999997);
            poses.mulPose(Axis.YP.rotationDegrees(side*45));
            poses.translate(side*-1,3.6,3.5);
            poses.mulPose(Axis.ZP.rotationDegrees(side*120));
            poses.mulPose(Axis.XP.rotationDegrees(200));
            poses.mulPose(Axis.YP.rotationDegrees(side*-135));
            poses.translate(side*5.6,0,0);
            var renderer=(PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer(player);
            if(!player.isInvisible()) {
                CastingPlayerPose.renderFirstPerson(() -> {
                    if(arm==HumanoidArm.RIGHT)renderer.renderRightHand(poses,event.getMultiBufferSource(),event.getPackedLight(),player);
                    else renderer.renderLeftHand(poses,event.getMultiBufferSource(),event.getPackedLight(),player);
                    // The arm part keeps the pose renderHand just gave it, so the veins land on the drawn arm.
                    CastVeinsLayer.firstPersonArm(renderer.getModel(),arm,poses,event.getMultiBufferSource(),player,event.getPartialTick());
                });
            }
            poses.popPose();
        } else {
            CastingPlayerPose.renderFirstPerson(() -> ((CastingHandRendererInvoker)mc.gameRenderer.itemInHandRenderer)
                    .hemomancy$renderCastingHand(player,event.getPartialTick(),event.getInterpolatedPitch(),
                            event.getHand(),event.getSwingProgress(),event.getItemStack(),event.getEquipProgress(),
                            poses,event.getMultiBufferSource(),event.getPackedLight()));
        }
        if(!player.isInvisible()) {
            poses.pushPose();
            poses.translate(side*.48,-.34,-.75);
            float time=animation.playback.elapsed(animation.now(),event.getPartialTick()),weight=animation.weight(event.getPartialTick());
            // First person shows only the Still Art hand ring; the ground ring is never in view.
            if(animation.packet().lane()==CastLane.UNSTAINED)CastingAnatomyRenderer.stillHand(poses,event.getMultiBufferSource(),animation.packet(),time,weight,true);
            poses.popPose();
        }
        } finally { poses.popPose(); }
    }
}
