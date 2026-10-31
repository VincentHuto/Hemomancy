package com.vincenthuto.hemomancy.common.tile.harbinger.decoration;

import com.vincenthuto.hemomancy.common.entity.npc.court.CourtPresences;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Marimo's Heart's block entity. It saves nothing: once a second on the server it looks for the living within
 * {@link CourtPresences#MARIMO_RANGE} of the heart, gives a newcomer the first-sight scene (once per player, recorded in
 * {@code CourtProgress}) and sends one pulse through the moss for every arrival. On the client it keeps the game time
 * of the last pulse for the renderer.
 */
public class MarimoHeartBlockEntity extends BlockEntity {
	public static final int PULSE_EVENT = 1;
	public static final int PULSE_TICKS = 60;

	private static final ParticleColor PULSE = new ParticleColor(246, 204, 112);

	private final Set<UUID> present = new HashSet<>();
	private long pulseStart = Long.MIN_VALUE / 2;

	public MarimoHeartBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.marimo_heart.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, MarimoHeartBlockEntity heart) {
		if (!(level instanceof ServerLevel server) || server.getGameTime() % 20L != 0L) return;
		Set<UUID> now = new HashSet<>();
		boolean arrival = false;
		for (ServerPlayer player : CourtPresences.livingNear(server, pos, CourtPresences.MARIMO_RANGE)) {
			now.add(player.getUUID());
			if (!heart.present.contains(player.getUUID())) arrival = true;
			CourtPresences.marimoFirstSight(player, pos);
		}
		heart.present.clear();
		heart.present.addAll(now);
		if (arrival) heart.pulse();
	}

	/** One pulse through the moss, for everyone who can see it. */
	public void pulse() {
		if (level != null) level.blockEvent(worldPosition, getBlockState().getBlock(), PULSE_EVENT, 0);
	}

	@Override
	public boolean triggerEvent(int id, int param) {
		if (id != PULSE_EVENT) return super.triggerEvent(id, param);
		if (level == null) return true;
		pulseStart = level.getGameTime();
		if (level.isClientSide) {
			// The pulse leaves the heart as a ring of gold motes running out over the moss.
			for (int i = 0; i < 18; i++) {
				double angle = i * Math.PI * 2.0D / 18.0D;
				level.addParticle(HemoParticleData.glow(PULSE), worldPosition.getX() + 0.5D + Math.cos(angle) * 0.7D,
						worldPosition.getY() + 1.05D, worldPosition.getZ() + 0.5D + Math.sin(angle) * 0.7D,
						Math.cos(angle) * 0.06D, 0.004D, Math.sin(angle) * 0.06D);
			}
		}
		return true;
	}

	/** 0..1 for {@link #PULSE_TICKS} after a pulse, swelling then fading; 0 otherwise. */
	public float pulse(double gameTime) {
		double t = (gameTime - pulseStart) / PULSE_TICKS;
		if (t < 0.0D || t >= 1.0D) return 0.0F;
		return (float) Math.sin(t * Math.PI);
	}
}
