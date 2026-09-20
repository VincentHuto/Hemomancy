package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.HLPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class SuccessionEffects {
    private SuccessionEffects() {}
    public static void ritual(ServerLevel level, BlockPos body, int phase, int ticks) {
        var heart = Vec3.atCenterOf(body).add(0, -.15, 0);
        var state = level.getBlockState(body);
        var facing = state.hasProperty(VacantEffigyBlock.FACING) ? state.getValue(VacantEffigyBlock.FACING) : net.minecraft.core.Direction.NORTH;
        Vec3 head = heart.add(-facing.getStepX()*.32, .03, -facing.getStepZ()*.32);
        if (ticks % 5 == 0) level.sendParticles(HemoParticleData.bloodCell(phase == 3
                ? new ParticleColor(180, 70, 18) : ParticleColor.BLOOD), heart.x, heart.y, heart.z,
                3, .35, phase >= 3 ? .6 : .1, .35, .015);
        if (ticks % 20 == 0 && phase >= 1) {
            Vec3 target = phase == 1 && ticks % 40 == 0 || phase == 3 ? head : heart;
            Vec3 source = target.add(Math.sin(ticks * .04) * .8, phase == 3 ? 1.6 : .3, Math.cos(ticks * .04) * .8);
            HLPacketHandler.sendLightningSpawn(source, target, 48, level.dimension(), phase == 3 ? new ParticleColor(196, 98, 30) : ParticleColor.BLOOD, 2, 10, 3, .25f);
            if (phase == 1) {
                for (int side : new int[]{-1,1}) {
                    Vec3 hand = heart.add(facing.getStepZ()*.34*side, -.06, -facing.getStepX()*.34*side);
                    HLPacketHandler.sendLightningSpawn(head, hand, 48, level.dimension(), ParticleColor.BLOOD, 1, 8, 2, .15f);
                }
            }
        }
        if (phase >= 2 && ticks % 40 == 0) level.playSound(null, body, net.minecraft.sounds.SoundEvents.WARDEN_HEARTBEAT,
                net.minecraft.sounds.SoundSource.BLOCKS, .55f, 1.1f);
    }
}
