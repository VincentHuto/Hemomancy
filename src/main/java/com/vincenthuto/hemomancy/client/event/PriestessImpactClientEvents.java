package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.boss.VesperRumbleClientState;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessEruptionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** A jolt through the camera when the Stained Priestess's blood thorns burst nearby; the plummet's landing hits hardest. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PriestessImpactClientEvents {
    private static final int JOLT_TICKS = 7;
    private static float amplitude;

    private PriestessImpactClientEvents() {
    }

    @SubscribeEvent
    public static void tickClient(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        amplitude = 0.0F;
        if (minecraft.level == null || player == null || player.isSpectator()) return;
        for (PriestessEruptionEntity eruption : minecraft.level.getEntitiesOfClass(PriestessEruptionEntity.class,
                player.getBoundingBox().inflate(24.0D))) {
            int since = eruption.tickCount - eruption.delay();
            if (since < 0 || since >= JOLT_TICKS) continue;
            double reach = eruption.isMajor() ? 22.0D : 9.0D;
            double falloff = 1.0D - Math.min(1.0D, player.distanceTo(eruption) / reach);
            float peak = eruption.isMajor() ? 2.4F : 0.7F;
            amplitude = Math.max(amplitude, (float) (peak * falloff) * (1.0F - since / (float) JOLT_TICKS));
        }
    }

    @SubscribeEvent
    public static void shakeCamera(ViewportEvent.ComputeCameraAngles event) {
        if (amplitude <= 0.0F || Minecraft.getInstance().level == null) return;
        float time = Minecraft.getInstance().level.getGameTime() + (float) event.getPartialTick();
        event.setPitch(event.getPitch() + VesperRumbleClientState.pitchShake(amplitude, time * 1.6F));
        event.setRoll(event.getRoll() + VesperRumbleClientState.rollShake(amplitude, time * 1.6F));
    }
}
