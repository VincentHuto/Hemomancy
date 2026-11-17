package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.common.block.harbinger.plant.BloodwoodGrowthHandler;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hemomancy.common.tile.HeldFormations;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import net.minecraft.network.chat.Component;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.world.vein.EarthenVeinTravelManager;
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

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.DoubleConsumer;

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
		IBloodVolume playerBlood = HemoCapabilityAccess.getBloodVolume(player);
		double before = playerBlood == null ? 0.0D : playerBlood.getBloodVolume();
		BlockPos lookedAt = findLookedAtBlockPos(player, reach);
		BlockBloodVolume tank = lookedAt == null ? null : tankAt(serverLevel, lookedAt);
		BlockTarget target = findLookedAtEndpoint(serverLevel, player, reach);
		double handled;
		if (target != null) {
			handled = target.endpoint().absorbBloodFromBlock(serverLevel, target.pos(), target.state(), player, maxAmount);
		} else {
			BlockPos pos = findLookedAtBlockPos(player, reach);
			handled = pos == null ? 0.0D : tryAbsorbFromReservoir(serverLevel, pos, player, maxAmount);
		}
		if (playerBlood != null && handled > 0.0D) {
			// PT-1b: the player's own poured blood, drawn back out, does not count toward the proof.
			double own = tank == null ? 0.0D : tank.takePoured(player.getUUID(), handled);
			com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment.recordAbsorption(player,
					Math.max(0.0D, playerBlood.getBloodVolume() - before - own));
		}
		return handled;
	}

	/** The tank a block reads, when it holds blood; null otherwise. */
	@Nullable
	public static BlockBloodVolume tankAt(ServerLevel level, BlockPos pos) {
		BlockEntity be = resolveBloodReservoir(level, pos);
		return be instanceof IBloodReservoir reservoir && reservoir.holdsBlood() ? ReservoirBlood.settled(be) : null;
	}

	/**
	 * PT-1b: runs a Blood Projection and records whatever it added to the tank the player looks at as that player's
	 * poured blood. Returns the projection's own result.
	 */
	public static double trackPoured(Level level, LivingEntity user, double reach, java.util.function.DoubleSupplier projection) {
		if (!(level instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player)) return projection.getAsDouble();
		var trace = SanguineProjectionTargeting.pick(serverLevel, player, reach, true);
		BlockBloodVolume tank = trace instanceof BlockHitResult hit && trace.getType() == HitResult.Type.BLOCK
				? tankAt(serverLevel, hit.getBlockPos()) : null;
		double before = tank == null ? 0.0D : tank.getBloodVolume() + tank.clotted();
		double result = projection.getAsDouble();
		if (tank != null) {
			double added = tank.getBloodVolume() + tank.clotted() - before;
			if (added > 0.0D) tank.recordPoured(player.getUUID(), added);
		}
		return result;
	}

	public static double tryProjectIntoLookedAtBlock(Level level, LivingEntity user, double maxAmount) {
		return tryProjectIntoLookedAtBlock(level, user, maxAmount, null);
	}

	/** {@code tankTransfer} hears the blood that went into a block that holds it, as a tank does. */
	public static double tryProjectIntoLookedAtBlock(Level level, LivingEntity user, double maxAmount,
			@Nullable DoubleConsumer tankTransfer) {
		if (!(level instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player) || maxAmount <= 0.0D) {
			return 0.0D;
		}
		HitResult trace = SanguineProjectionTargeting.pick(serverLevel, player, VerbReach.of(player, Verb.PROJECT), true);
		BlockPos pos = trace.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) trace).getBlockPos() : null;
		if (pos == null) {
			return 0.0D;
		}
		double veinHandled = EarthenVeinTravelManager.project(player, serverLevel, pos, maxAmount);
		if (veinHandled > 0.0D) return veinHandled;
		double bloodwoodHandled = BloodwoodGrowthHandler.tryGrowFromProjection(serverLevel, pos, player, maxAmount);
		if (bloodwoodHandled > 0.0D) {
			return bloodwoodHandled;
		}
		BlockTarget target = findEndpoint(serverLevel, pos);
		if (target == null) return 0.0D;
		double moved = target.endpoint().projectBloodIntoBlock(serverLevel, target.pos(), target.state(), player, maxAmount);
		if (moved > 0.0D && tankTransfer != null && target.endpoint().holdsProjectedBlood()) tankTransfer.accept(moved);
		return moved;
	}

	public static Optional<Vec3> findLookedAtBloodBlockSource(Level level, LivingEntity user) {
		HitResult trace = user.pick(VerbReach.of(user, Verb.ABSORB), 0, true);
		if (!(trace instanceof BlockHitResult blockHit)) {
			return Optional.empty();
		}
		return isBloodInteractionBlock(level, blockHit.getBlockPos())
				? Optional.of(blockHit.getLocation())
				: Optional.empty();
	}

	public static boolean isBloodInteractionBlock(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof BlockBloodEndpoint endpoint) {
			return endpoint.canDrawAbsorptionParticles(level, pos, state);
		}
		return resolveBloodReservoir(level, pos) != null;
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

	private static double tryAbsorbFromReservoir(ServerLevel level, BlockPos pos, ServerPlayer player,
			double maxAmount) {
		BlockEntity be = resolveBloodReservoir(level, pos);
		// A station mid-rite keeps its blood: it is part of the upgrade snapshot.
		if (be == null || be instanceof IBloodReservoir closed && !closed.canProvideBlood()) {
			return 0.0D;
		}
		IBloodVolume reservoirVolume = HemoCapabilityAccess.getBloodVolume(be).orElse(null);
		IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player);
		if (reservoirVolume == null || playerVolume == null || !playerVolume.isActive()) {
			return 0.0D;
		}
		if (!BloodDrawRights.mayDraw(player, be)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.living_blood.not_yours"), true);
			return 0.0D;
		}

		double playerRoom = Math.max(0.0D, playerVolume.getMaxBloodVolume() - playerVolume.getBloodVolume());
		double transfer = Math.min(maxAmount, Math.min(reservoirVolume.getBloodVolume(), playerRoom));
		if (transfer <= 0.0D) {
			// Clot is not fuel: once the fresh blood is gone, the draw scrapes it out as Formations.
			return reservoirVolume.getBloodVolume() <= 0.0D && reservoirVolume instanceof BlockBloodVolume block
					? scrapeClot(be, block, player) : 0.0D;
		}

		reservoirVolume.subtractBloodVolume(transfer);
		playerVolume.fill(transfer);
		if (be instanceof IBloodReservoir reservoir) {
			reservoir.sendUpdates();
		}
		BloodVolumeEvents.syncVolume(player, playerVolume);
		return transfer;
	}

	/**
	 * Scrapes whole Formations' worth of clot out of a reservoir into the player's pack, one Formation a call; a smear
	 * too small for a Formation is wiped away. Returns the clot taken, so the channel reads as working.
	 */
	private static double scrapeClot(BlockEntity be, BlockBloodVolume block, ServerPlayer player) {
		double clot = block.clotted();
		if (clot <= CoagulationRules.CLOT_EPSILON) return 0.0D;
		if (CoagulationRules.neglectFormations(clot) <= 0) {
			block.scrapeClot(clot);
		} else if (HeldFormations.give(player, 1) > 0) {
			clot = block.scrapeClot(CoagulationRules.NEGLECT_ML_PER_FORMATION);
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_SET_THROUGH);
		} else {
			return 0.0D;
		}
		if (be instanceof IBloodReservoir reservoir) reservoir.sendUpdates();
		return clot;
	}

	private static BlockEntity resolveBloodReservoir(Level level, BlockPos pos) {
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
