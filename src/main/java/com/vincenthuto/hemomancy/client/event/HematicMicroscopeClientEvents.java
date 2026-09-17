package com.vincenthuto.hemomancy.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.client.render.layer.player.HematicMicroscopeLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class HematicMicroscopeClientEvents {
    private HematicMicroscopeClientEvents() { }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) { HematicMicroscopeClientState.tick(); }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void renderHand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        var animation = HematicMicroscopeClientState.animation(player);
        if (animation == null) return;
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND || animation.overlayAlpha(event.getPartialTick()) >= 1) return;
        boolean right = animation.packet.right();
        float tick = animation.poseTick(event.getPartialTick());
        var instrument = new PoseStack();
        HematicMicroscopePose.firstPersonInstrument(instrument, tick, right);
        var vial = new PoseStack();
        HematicMicroscopePose.vial(vial, instrument, tick, right);
        var heldInstrument = renderHeld(event, animation.packet.instrument(), instrument, tick, right, !right);
        var heldVial = renderHeld(event, animation.packet.sample(), vial, tick, !right, false);
        renderArm(event, heldInstrument.last().pose().transformPosition(new Vector3f(0, -.21875F, -.0625F)), tick, right);
        renderArm(event, heldVial.last().pose().transformPosition(new Vector3f(-.1875F, -.1875F, 0)), tick, !right);
    }
    private static PoseStack renderHeld(RenderHandEvent event, ItemStack stack, PoseStack target, float tick,
            boolean right, boolean mirroredInstrument) {
        var mc = Minecraft.getInstance();
        var ordinary = new PoseStack();
        ordinary.translate((right ? 1 : -1) * .56F, -.52F - .6F * event.getEquipProgress(), -.72F);
        mc.getItemRenderer().getModel(stack, mc.level, mc.player, mc.player.getId()).getTransforms()
                .getTransform(right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .apply(!right, ordinary);
        var held = new PoseStack();
        BloodVialInjectionPose.blendTransform(held, ordinary, target, HematicMicroscopePose.weight(tick));
        var poses = event.getPoseStack();
        poses.pushPose();
        poses.mulPose(held.last().pose());
        HematicMicroscopeLayer.renderItem(stack, mirroredInstrument, poses, event.getMultiBufferSource(), event.getPackedLight(), mc.player);
        poses.popPose();
        return held;
    }
    private static void renderArm(RenderHandEvent event, Vector3f grip, float tick, boolean right) {
        var mc = Minecraft.getInstance();
        if (mc.player.isInvisible()) return;
        var model = ((PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player)).getModel();
        var arm = right ? model.rightArm : model.leftArm;
        var sleeve = right ? model.rightSleeve : model.leftSleeve;
        var savedArm = arm.storePose(); var savedSleeve = sleeve.storePose();
        boolean armVisible = arm.visible, sleeveVisible = sleeve.visible;
        var poses = event.getPoseStack();
        poses.pushPose();
        try {
            var direction = new Vector3f(grip).sub(new Vector3f(right ? .45F : -.45F, -.65F, .05F)).normalize();
            poses.translate(grip.x, grip.y, grip.z);
            poses.mulPose(new Quaternionf().rotationTo(new Vector3f(0, 1, 0), direction));
            // Turn the skin's inner fist faces inward without changing the wrist anchor.
            poses.mulPose(Axis.YP.rotationDegrees(180));
            poses.translate(0, -.625F, 0);
            arm.setPos(0, 0, 0); arm.setRotation(0, 0, 0); arm.visible = true;
            sleeve.copyFrom(arm);
            sleeve.visible = mc.player.isModelPartShown(right ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE);
            int color = Math.round(255 * HematicMicroscopePose.weight(tick)) << 24 | 0xFFFFFF;
            var vertices = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(mc.player.getSkin().texture()));
            arm.render(poses, vertices, event.getPackedLight(), OverlayTexture.NO_OVERLAY, color);
            sleeve.render(poses, vertices, event.getPackedLight(), OverlayTexture.NO_OVERLAY, color);
        } finally {
            arm.loadPose(savedArm); sleeve.loadPose(savedSleeve);
            arm.visible = armVisible; sleeve.visible = sleeveVisible;
            poses.popPose();
        }
    }
}
