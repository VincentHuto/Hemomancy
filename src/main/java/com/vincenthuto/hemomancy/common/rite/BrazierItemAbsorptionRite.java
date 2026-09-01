package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.particle.SpawnBrazierItemAbsorptionParticlesPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BrazierItemAbsorptionRite {
	public static final int REQUIRED_CHANNEL_TICKS = 60;
	private static final int PARTICLE_PULSE_INTERVAL_TICKS = 10;

	private BrazierItemAbsorptionRite() {
	}

	public static int advance(ServerLevel level, BlockPos pos, ServerPlayer player,
			IronBrazierBlockEntity brazier, String riteId, ItemStack offering) {
		int progress = brazier.advanceItemAbsorption(player, riteId, REQUIRED_CHANNEL_TICKS);
		if (shouldEmitItemStream(progress)) {
			spawnItemDrawParticles(pos, player, offering);
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

	public static void complete(ServerLevel level, BlockPos pos, IronBrazierBlockEntity brazier) {
		brazier.consumeOffering();
		brazier.resetItemAbsorptionProgress();
		BlockState state = level.getBlockState(pos);
		if (state.hasProperty(BrazierBlock.RITUAL_PHASE)
				&& state.getValue(BrazierBlock.RITUAL_PHASE) != 0) {
			level.setBlock(pos, state.setValue(BrazierBlock.RITUAL_PHASE, 0), Block.UPDATE_ALL);
		}
	}

	private static void spawnChannelParticles(ServerLevel level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos).add(0.0D, 0.55D, 0.0D);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z,
				4, 0.25D, 0.12D, 0.25D, 0.01D);
	}

	private static void spawnItemDrawParticles(BlockPos pos, ServerPlayer player, ItemStack offering) {
		ItemStack particleStack = offering.copy();
		particleStack.setCount(1);
		PacketHandler.sendToPlayer(player,
				new SpawnBrazierItemAbsorptionParticlesPacket(
						Vec3.atCenterOf(pos).add(0.0D, 0.65D, 0.0D), particleStack));
	}
}
