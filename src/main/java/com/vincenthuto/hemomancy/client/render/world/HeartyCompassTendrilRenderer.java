package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.LastDeathMemory;
import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilEffectData;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class HeartyCompassTendrilRenderer {
    private static final int PULSE_INTERVAL = 4;
    private static final double TENDRIL_LENGTH = 2.35D;
    private static final double HAND_SIDE_OFFSET = 0.56D;
    private static final double HAND_FORWARD_OFFSET = 0.38D;
    private static final double HAND_DOWN_OFFSET = 0.98D;
    private static final double THIRD_PERSON_HAND_EXTRA_DROP = 0.24D;
    private static long nextPulseTime = Long.MIN_VALUE;

    private HeartyCompassTendrilRenderer() {
    }

    public static void render(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null) {
            nextPulseTime = Long.MIN_VALUE;
            return;
        }
        HeldCompass heldCompass = findHeldCompass(player);
        if (heldCompass == null) {
            return;
        }
        LastDeathMemory memory = HemoCapabilityAccess.getLastDeathMemory(player);
        if (!memory.hasMemory() || !memory.isSameDimension(mc.level.dimension().location().toString())) {
            return;
        }

        long gameTime = mc.level.getGameTime();
        if (gameTime < nextPulseTime) {
            return;
        }
        nextPulseTime = gameTime + PULSE_INTERVAL;

        Vec3 start = handPosition(mc, player, heldCompass.hand(), partialTick);
        Vec3 target = new Vec3(LastDeathMemory.unpackX(memory.packedPosition()) + 0.5D,
                LastDeathMemory.unpackY(memory.packedPosition()) + 0.5D,
                LastDeathMemory.unpackZ(memory.packedPosition()) + 0.5D);
        Vec3 toTarget = target.subtract(start);
        double length = toTarget.length();
        if (length < 0.001D) {
            return;
        }
        Vec3 end = start.add(toTarget.scale(Math.min(TENDRIL_LENGTH, length) / length));
        long seed = tendrilSeed(player, memory.packedPosition(), gameTime / PULSE_INTERVAL);
        TendrilRenderer.INSTANCE.add(new TendrilEffectData(new TendrilAnchor.Point(start),
                new TendrilAnchor.Point(end), config(seed), seed), partialTick);
    }

    private static HeldCompass findHeldCompass(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(ItemInit.hearty_compass.get())) {
            return new HeldCompass(InteractionHand.MAIN_HAND);
        }
        ItemStack offHand = player.getOffhandItem();
        if (offHand.is(ItemInit.hearty_compass.get())) {
            return new HeldCompass(InteractionHand.OFF_HAND);
        }
        return null;
    }

    private static Vec3 handPosition(Minecraft mc, LocalPlayer player, InteractionHand hand, float partialTick) {
        double x = Mth.lerp(partialTick, player.xOld, player.getX());
        double y = Mth.lerp(partialTick, player.yOld, player.getY()) + player.getEyeHeight();
        double z = Mth.lerp(partialTick, player.zOld, player.getZ());
        float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot()) * Mth.DEG_TO_RAD;
        double side = hand == InteractionHand.MAIN_HAND ? -1.0D : 1.0D;
        if (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT) {
            side *= -1.0D;
        }
        Vec3 forward = player.getViewVector(partialTick).normalize();
        Vec3 right = new Vec3(Mth.cos(yaw), 0.0D, Mth.sin(yaw));
        double drop = HAND_DOWN_OFFSET;
        if (!mc.options.getCameraType().isFirstPerson()) {
            drop += THIRD_PERSON_HAND_EXTRA_DROP;
        }
        return new Vec3(x, y, z)
                .add(right.scale(HAND_SIDE_OFFSET * side))
                .add(forward.scale(HAND_FORWARD_OFFSET))
                .add(0.0D, -drop, 0.0D);
    }

    private static TendrilEffectConfig config(long seed) {
        return TendrilEffectConfig.defaults()
                .withMode(TendrilEffectConfig.Mode.FREEFORM)
                .withColors(0xF4070102, 0xD8D10B1A)
                .withBlendColors(false)
                .withLifecycle(3, 6, 5)
                .withShape(13, 1, 0.026F, 0.055F)
                .withBranching(1, 1, 0.10F, 0.32F)
                .withWrithe(0.035F, 0.08F, 0.07F, 0.0F)
                .withFixedSeed(true, seed);
    }

    private static long tendrilSeed(LocalPlayer player, long packedPosition, long pulse) {
        long seed = 0xA0761D6478BD642FL;
        seed ^= (long) player.getId() * 0xE7037ED1A0B428DBL;
        seed ^= packedPosition * 0x8EBC6AF09C88C6E3L;
        seed ^= pulse * 0x589965CC75374CC3L;
        return seed;
    }

    private record HeldCompass(InteractionHand hand) {
    }
}
