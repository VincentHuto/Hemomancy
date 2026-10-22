package com.vincenthuto.hemomancy.client.particle;

import com.vincenthuto.hemomancy.client.render.world.SanguineMonolithShatterRenderer;
import com.vincenthuto.hemomancy.common.network.particle.ParticleBurstPacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Client side of {@link ParticleBurstPacket}: spawns the burst each kind names. */
public final class ParticleBurstEffects {
	private ParticleBurstEffects() {
	}

	public static void spawn(ParticleBurstPacket packet, Player player) {
		if (player == null) return;
		Level level = player.level();
		Vec3 pos = packet.pos();
		switch (packet.kind()) {
			case AVATAR_HIT -> level.addParticle(HemoParticleData.bloodAvatarHit(packet.color()),
					pos.x, pos.y, pos.z, 0, 0, 0);
			case BLOOD_FLASK -> level.addParticle(HemoParticleData.glow(packet.color()),
					pos.x, pos.y + 1, pos.z, scatter(), scatter(), scatter());
			case BLOOD_CLAW -> level.addParticle(HemoParticleData.bloodClaw(packet.color()),
					pos.x, pos.y, pos.z, 0f, 0, 0);
			case LIVING_TOOL_BREAK -> {
				for (int i = 0; i < 20; i++) {
					level.addParticle(HemoParticleData.bloodCell(packet.color()),
							pos.x, pos.y + 1, pos.z, scatter(), scatter(), scatter());
				}
			}
			case MONOLITH_SHATTER -> SanguineMonolithShatterRenderer.spawnBurst(pos, level.random);
			case MONOLITH_SHARD_PULSE -> SanguineMonolithShatterRenderer.spawnShardPulse(pos, level.random);
			case POME_PULSE -> SanguineMonolithShatterRenderer.spawnPomePulse(pos);
		}
	}

	private static double scatter() {
		return HLParticleUtils.inRange(-3, 3) * 0.015f;
	}
}
