package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Server-sent bursts over a Condenser's blood surface: a formation set, a formation lost, a cauldron taking blood. */
public final class SanguineCondenserEffects {
    private static final ParticleColor CRYSTAL = new ParticleColor(214, 22, 34);
    private static final ParticleColor CORPUS = new ParticleColor(150, 16, 28);
    private static final ParticleColor CLOT = new ParticleColor(58, 4, 8);

    private SanguineCondenserEffects() {
    }

    public static void condensed(ServerLevel level, SanguineCondenserBlockEntity condenser) {
        Vec3 at = surface(condenser);
        level.sendParticles(HemoParticleData.glow(CRYSTAL), at.x, at.y + 0.08D, at.z, 6, 0.10D, 0.04D, 0.10D, 0.0D);
        level.sendParticles(HemoParticleData.bloodCell(CORPUS), at.x, at.y, at.z, 10, 0.18D, 0.02D, 0.18D, 0.02D);
    }

    public static void collapse(ServerLevel level, SanguineCondenserBlockEntity condenser) {
        Vec3 at = surface(condenser);
        level.sendParticles(HemoParticleData.darkGlow(CLOT), at.x, at.y + 0.05D, at.z, 8, 0.14D, 0.04D, 0.14D, 0.01D);
        level.sendParticles(HemoParticleData.bloodCell(CLOT), at.x, at.y, at.z, 6, 0.12D, 0.02D, 0.12D, 0.01D);
    }

    public static void converted(ServerLevel level, BlockPos pos) {
        Vec3 at = Vec3.atBottomCenterOf(pos).add(0.0D, 0.32D, 0.0D);
        level.sendParticles(HemoParticleData.bloodCell(CORPUS), at.x, at.y, at.z, 12, 0.22D, 0.03D, 0.22D, 0.04D);
    }

    private static Vec3 surface(SanguineCondenserBlockEntity condenser) {
        double fill = condenser.getMaxBloodVolume() <= 0.0D ? 0.0D
                : condenser.getBloodVolume() / condenser.getMaxBloodVolume();
        return Vec3.atBottomCenterOf(condenser.getBlockPos())
                .add(0.0D, SanguineCondenserRules.surfaceHeight(fill), 0.0D);
    }
}
