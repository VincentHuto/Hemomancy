package com.vincenthuto.hemomancy.client.event;

import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.BloodVialInjectionClientState;
import com.vincenthuto.hemomancy.client.player.BloodVialInjectionPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BloodVialInjectionClientEvents {
    private BloodVialInjectionClientEvents() { }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) { BloodVialInjectionClientState.tick(); }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void renderHand(RenderHandEvent event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var animation = BloodVialInjectionClientState.animation(player);
        if (animation == null || event.getHand() != animation.packet.hand()) return;
        event.setCanceled(true);
        boolean right = animation.packet.right();
        float tick = animation.elapsed(event.getPartialTick());
        var grip = BloodVialInjectionPose.firstPersonGrip(tick, right);
        var poses = event.getPoseStack();
        var renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        float side = right ? 1 : -1;
        poses.pushPose();
        poses.translate(grip.x, grip.y, grip.z);
        // Work backwards from the wrist so the vial remains gripped throughout the plunge.
        poses.mulPose(Axis.ZP.rotationDegrees(side * 20));
        poses.mulPose(Axis.XP.rotationDegrees(-58 + 25 * BloodVialInjectionPose.motion(tick).strike()));
        poses.translate(right ? 1F / 16 : -1F / 16, -10F / 16, 0);
        if (!player.isInvisible()) {
            var model = renderer.getModel();
            var arm = right ? model.rightArm : model.leftArm;
            var sleeve = right ? model.rightSleeve : model.leftSleeve;
            var savedArm = arm.storePose();
            var savedSleeve = sleeve.storePose();
            boolean armVisible = arm.visible;
            boolean sleeveVisible = sleeve.visible;
            try {
                arm.setPos(0, 0, 0);
                arm.setRotation(0, 0, 0);
                arm.visible = true;
                sleeve.copyFrom(arm);
                sleeve.visible = player.isModelPartShown(right
                        ? net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE
                        : net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE);
                int alpha = Math.round(255 * BloodVialInjectionPose.motion(tick).weight());
                var vertices = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(player.getSkin().texture()));
                arm.render(poses, vertices, event.getPackedLight(), OverlayTexture.NO_OVERLAY, alpha << 24 | 0xFFFFFF);
                sleeve.render(poses, vertices, event.getPackedLight(), OverlayTexture.NO_OVERLAY, alpha << 24 | 0xFFFFFF);
            } finally {
                arm.loadPose(savedArm);
                sleeve.loadPose(savedSleeve);
                arm.visible = armVisible;
                sleeve.visible = sleeveVisible;
            }
        }
        poses.popPose();
        poses.pushPose();
        var injection = new PoseStack();
        BloodVialInjectionPose.orientVial(injection, grip, new Vector3f(-side * .12F, -.92F, .35F).normalize());
        var ordinary = new PoseStack();
        ordinary.translate(side * .56F, -.52F - .6F * event.getEquipProgress(), -.72F);
        var context = right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        mc.getItemRenderer().getModel(animation.vial, player.level(), player, player.getId())
                .getTransforms().getTransform(context).apply(!right, ordinary);
        BloodVialInjectionPose.blendTransform(poses, ordinary, injection, BloodVialInjectionPose.motion(tick).weight());
        mc.getItemRenderer().renderStatic(animation.vial, ItemDisplayContext.NONE, event.getPackedLight(),
                OverlayTexture.NO_OVERLAY, poses, event.getMultiBufferSource(), player.level(), player.getId());
        poses.popPose();
    }
}
