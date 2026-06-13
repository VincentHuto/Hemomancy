package com.vincenthuto.hemomancy.common.block.shared;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class BlockBloodInteractions {
	private static final double BLOCK_REACH = 5.5D;

	private BlockBloodInteractions() {
	}

	public static double tryAbsorbFromLookedAtBlock(Level level, LivingEntity user, double maxAmount) {
		return tryAbsorbFromLookedAtBlock(level, user, BLOCK_REACH, maxAmount);
	}

	public static double tryAbsorbFromLookedAtBlock(Level level, LivingEntity user, double reach, double maxAmount) {
		if (!(level instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player) || maxAmount <= 0.0D) {
			return 0.0D;
		}
		BlockTarget target = findLookedAtEndpoint(serverLevel, player, reach);
		if (target != null) {
			return target.endpoint().absorbBloodFromBlock(serverLevel, target.pos(), target.state(), player, maxAmount);
		}
		BlockPos pos = findLookedAtBlockPos(player, reach);
		return pos == null ? 0.0D : tryAbsorbFromBloodTile(serverLevel, pos, player, maxAmount);
	}

	public static double tryProjectIntoLookedAtBlock(Level level, LivingEntity user, double maxAmount) {
		if (!(level instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player) || maxAmount <= 0.0D) {
			return 0.0D;
		}
		BlockPos pos = findLookedAtBlockPos(player, BLOCK_REACH);
		if (pos == null) {
			return 0.0D;
		}
		double bloodwoodHandled = BloodwoodGrowthHandler.tryGrowFromProjection(serverLevel, pos, player, maxAmount);
		if (bloodwoodHandled > 0.0D) {
			return bloodwoodHandled;
		}
		BlockTarget target = findEndpoint(serverLevel, pos);
		return target == null ? 0.0D
				: target.endpoint().projectBloodIntoBlock(serverLevel, target.pos(), target.state(), player, maxAmount);
	}

	public static Optional<Vec3> findLookedAtBloodBlockSource(Level level, LivingEntity user) {
		HitResult trace = user.pick(BLOCK_REACH, 0, true);
		if (!(trace instanceof BlockHitResult blockHit)) {
			return Optional.empty();
		}
		return isBloodInteractionBlock(level, blockHit.getBlockPos())
				? Optional.of(blockHit.getLocation())
				: Optional.empty();
	}

	public static boolean isBloodInteractionBlock(Level level, BlockPos pos) {
		if (level.getBlockState(pos).getBlock() instanceof BlockBloodEndpoint) {
			return true;
		}
		return resolveBloodTile(level, pos) != null;
	}

	private static BlockTarget findLookedAtEndpoint(ServerLevel level, ServerPlayer player, double reach) {
		BlockPos pos = findLookedAtBlockPos(player, reach);
		if (pos == null) {
			return null;
		}
		return findEndpoint(level, pos);
	}

	private static BlockTarget findEndpoint(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof BlockBloodEndpoint endpoint) {
			return new BlockTarget(pos, state, endpoint);
		}
		return null;
	}

	private static BlockPos findLookedAtBlockPos(ServerPlayer player) {
		return findLookedAtBlockPos(player, BLOCK_REACH);
	}

	private static BlockPos findLookedAtBlockPos(ServerPlayer player, double reach) {
		HitResult trace = player.pick(reach, 0, true);
		return trace.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) trace).getBlockPos() : null;
	}

	private static double tryAbsorbFromBloodTile(ServerLevel level, BlockPos pos, ServerPlayer player,
			double maxAmount) {
		BlockEntity be = resolveBloodTile(level, pos);
		if (be == null) {
			return 0.0D;
		}
		IBloodVolume tileVolume = HemoCapabilityAccess.getBloodVolume(be).orElse(null);
		IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (tileVolume == null || playerVolume == null || !playerVolume.isActive()) {
			return 0.0D;
		}

		double playerRoom = Math.max(0.0D, playerVolume.getMaxBloodVolume() - playerVolume.getBloodVolume());
		double transfer = Math.min(maxAmount, Math.min(tileVolume.getBloodVolume(), playerRoom));
		if (transfer <= 0.0D) {
			return 0.0D;
		}

		tileVolume.subtractBloodVolume(transfer);
		playerVolume.fill(transfer);
		if (be instanceof IBloodTile bt) {
			bt.sendUpdates();
		}
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(playerVolume));
		return transfer;
	}

	private static BlockEntity resolveBloodTile(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof FillerBlockEntity filler) {
			BlockPos mainPos = filler.getMainBlockPos();
			be = mainPos == null ? null : level.getBlockEntity(mainPos);
		}
		return be != null && HemoCapabilityAccess.getBloodVolume(be).isPresent() ? be : null;
	}

	private record BlockTarget(BlockPos pos, BlockState state, BlockBloodEndpoint endpoint) {
	}
}
