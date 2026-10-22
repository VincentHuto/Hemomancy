package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierMount;
import com.vincenthuto.hemomancy.common.network.particle.SpawnBrazierItemAbsorptionParticlesPacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class BrazierItemAbsorptionRite {
	public static final int REQUIRED_CHANNEL_TICKS = 60;
	private static final int PARTICLE_PULSE_INTERVAL_TICKS = 10;
	/** How far away other players still see an offering drawn out of a brazier. */
	private static final double ITEM_STREAM_VIEW_RADIUS = 32.0D;
	private static final ParticleColor CHANNEL_FLARE = new ParticleColor(236, 52, 40);

	private BrazierItemAbsorptionRite() {
	}

	public static int advance(ServerLevel level, BlockPos pos, ServerPlayer player,
			IronBrazierBlockEntity brazier, String riteId, ItemStack offering) {
		int progress = brazier.advanceItemAbsorption(player, riteId, REQUIRED_CHANNEL_TICKS);
		if (shouldEmitItemStream(progress)) {
			spawnItemDrawParticles(level, pos, player, offering);
		}
		if (shouldEmitPulse(progress)) {
			spawnChannelParticles(level, pos);
		}
		return progress;
	}

	public static boolean shouldEmitItemStream(int progress) {
		return progress > 0 && progress <= REQUIRED_CHANNEL_TICKS;
	}

	public static boolean shouldEmitPulse(int progress) {
		return progress > 0 && progress <= REQUIRED_CHANNEL_TICKS
				&& progress % PARTICLE_PULSE_INTERVAL_TICKS == 0;
	}

	public static boolean isComplete(int progress) {
		return progress >= REQUIRED_CHANNEL_TICKS;
	}

	/** The offering is drawn out with its fire: it is consumed and the brazier goes out. */
	public static void complete(ServerLevel level, BlockPos pos, IronBrazierBlockEntity brazier) {
		brazier.consumeForRite(true);
	}

	private static void spawnChannelParticles(ServerLevel level, BlockPos pos) {
		Vec3 center = BrazierMount.bowl(level.getBlockState(pos), pos, 0.55D);
		// The rite flame flares crimson round the offering while it is drawn out.
		level.sendParticles(HemoParticleData.ember(CHANNEL_FLARE, 0.07F, 0.9F, 22), center.x, center.y, center.z,
				4, 0.22D, 0.12D, 0.22D, 0.012D);
	}

	private static void spawnItemDrawParticles(ServerLevel level, BlockPos pos, ServerPlayer player,
			ItemStack offering) {
		ItemStack particleStack = offering.copy();
		particleStack.setCount(1);
		Vec3 source = BrazierMount.bowl(level.getBlockState(pos), pos, 0.65D);
		// Everyone near the brazier sees the offering stream into the channelling hand, not only the channeller.
		PacketHandler.sendNear(level, source.x, source.y, source.z, ITEM_STREAM_VIEW_RADIUS,
				new SpawnBrazierItemAbsorptionParticlesPacket(player.getId(), source, particleStack));
	}
}
