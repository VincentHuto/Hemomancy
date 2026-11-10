package com.vincenthuto.hemomancy.client.event;

import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.LivingSyringeDrawClientState;
import com.vincenthuto.hemomancy.client.player.LivingSyringeDrawTimeline;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LivingSyringeDrawClientEvents {
    private LivingSyringeDrawClientEvents() { }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) { LivingSyringeDrawClientState.tick(); }

    /** The syringe's first-person shift from its ordinary hold; the blood's path follows it. */
    public static Vec3 firstPersonOffset(float tick, boolean right) {
        float side = right ? 1 : -1;
        float weight = LivingSyringeDrawTimeline.weight(tick);
        float thrust = LivingSyringeDrawTimeline.thrust(tick);
        float lift = LivingSyringeDrawTimeline.lift(tick);
        return new Vec3(-side * (.2F * thrust + .3F * lift) * weight, (.06F * thrust + .16F * lift) * weight,
                (-.28F * thrust + .08F * lift + .012F * LivingSyringeDrawTimeline.pull(tick)) * weight);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void renderHand(RenderHandEvent event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var animation = LivingSyringeDrawClientState.animation(player);
        if (animation == null || event.getHand() != animation.packet.hand()) return;
        event.setCanceled(true);
        boolean right = animation.packet.right();
        float side = right ? 1 : -1;
        float tick = animation.elapsed(event.getPartialTick());
        Vec3 offset = firstPersonOffset(tick, right);
        var poses = event.getPoseStack();
        poses.pushPose();
        poses.translate(side * .56F + offset.x, -.52F - .6F * event.getEquipProgress() + offset.y, -.72F + offset.z);
        // Only once the blood has crossed: turn the drum's face to the camera.
        poses.mulPose(Axis.YP.rotationDegrees(side * 32 * LivingSyringeDrawTimeline.lift(tick) * LivingSyringeDrawTimeline.weight(tick)));
        var context = right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        mc.gameRenderer.itemInHandRenderer.renderItem(player, event.getItemStack(), context, !right, poses,
                event.getMultiBufferSource(), event.getPackedLight());
        poses.popPose();
    }
}
