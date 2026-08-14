package com.vincenthuto.hemomancy.common.world.fold;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.NonEuclideanHallwayBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FoldedHallwayManager {
	private static final int SEARCH_RADIUS_XZ = 4;
	private static final int SEARCH_RADIUS_Y = 3;
	private static final int EXIT_COOLDOWN_TICKS = 4;
	private static final Map<TravelerKey, FoldTravelerState> TRAVELERS = new ConcurrentHashMap<>();
	private static final Set<TravelerKey> PENDING_EXIT_AFTER_MOVE = ConcurrentHashMap.newKeySet();

	private FoldedHallwayManager() {
	}

	public static Optional<FoldTravelerState> getState(Player player) {
		FoldTravelerState state = TRAVELERS.get(key(player));
		return state != null && state.isActive() ? Optional.of(state) : Optional.empty();
	}

	public static boolean isActive(Player player) {
		return getState(player).isPresent();
	}

	public static Vec3 compressMovement(Player player, Vec3 movement) {
		TravelerKey key = key(player);
		FoldTravelerState state = TRAVELERS.get(key);
		if (state == null || !state.isActive() || state.activeFold() == null) {
			if (state != null && state.cooldownTicks() > 0) {
				return movement;
			}
			return compressEntryMovement(player, movement);
		}
		if (!sameDimension(player.level(), state.activeFold())) {
			clear(player);
			return movement;
		}
		if (state.entrySide() == null
				|| !FoldActivationBounds.containsTraversalPoint(state.activeFold(), state.entrySide(),
						FoldMinecraftAdapter.fromVec3(player.position()))) {
			TRAVELERS.put(key, FoldTravelerState.inactive(EXIT_COOLDOWN_TICKS));
			return movement;
		}
		return compressActiveMovement(player, state, movement);
	}

	private static Vec3 compressEntryMovement(Player player, Vec3 movement) {
		FoldVector start = FoldMinecraftAdapter.fromVec3(player.position());
		FoldVector requestedMovement = FoldMinecraftAdapter.fromVec3(movement);
		if (Math.abs(requestedMovement.x) < 1.0E-7D
				&& Math.abs(requestedMovement.y) < 1.0E-7D
				&& Math.abs(requestedMovement.z) < 1.0E-7D) {
			return movement;
		}

		Optional<MovementEntryCandidate> entry = findEnteringFold(player.level(), start, start.add(requestedMovement));
		if (entry.isEmpty()) {
			return movement;
		}

		FoldedHallwaySpec spec = entry.get().spec();
		FoldActivationBounds.EntryCrossing crossing = entry.get().crossing();
		FoldVector compressedMovement = FoldTransform.of(spec)
				.compressMovementAfterEntry(requestedMovement, crossing.fraction());
		FoldVector expectedPosition = start.add(compressedMovement);
		double progress = FoldActivationBounds.realDepthProgress(spec, crossing.entrySide(), expectedPosition);
		TRAVELERS.put(key(player), FoldTravelerState.active(spec, crossing.entrySide(), Math.max(0.0D, progress)));
		return FoldMinecraftAdapter.toVec3(compressedMovement);
	}

	private static Vec3 compressActiveMovement(Player player, FoldTravelerState state, Vec3 movement) {
		FoldedHallwaySpec spec = state.activeFold();
		FoldTransform transform = FoldTransform.of(spec);
		FoldVector requestedMovement = FoldMinecraftAdapter.fromVec3(movement);
		FoldVector compressedMovement = transform.compressMovement(requestedMovement);
		Optional<FoldActivationBounds.ExitCrossing> exit = FoldActivationBounds.exitCrossing(spec, state.entrySide(),
				FoldMinecraftAdapter.fromVec3(player.position()), compressedMovement);
		if (exit.isEmpty()) {
			return FoldMinecraftAdapter.toVec3(compressedMovement);
		}

		PENDING_EXIT_AFTER_MOVE.add(key(player));
		return FoldMinecraftAdapter.toVec3(transform.compressMovementUntilExit(requestedMovement, exit.get().fraction()));
	}

	public static void finishMove(Player player) {
		TravelerKey key = key(player);
		if (PENDING_EXIT_AFTER_MOVE.remove(key)) {
			TRAVELERS.put(key, FoldTravelerState.inactive(EXIT_COOLDOWN_TICKS));
		}
	}

	public static void tick(Player player) {
		TravelerKey key = key(player);
		FoldTravelerState state = TRAVELERS.get(key);
		if (state != null && state.isActive() && state.activeFold() != null) {
			tickActive(player, state);
			return;
		}

		if (state != null && state.cooldownTicks() > 0) {
			FoldTravelerState ticked = state.withCooldownTicked();
			if (ticked.cooldownTicks() > 0) {
				TRAVELERS.put(key, ticked);
			} else {
				TRAVELERS.remove(key);
			}
			return;
		}

		if (state != null) {
			TRAVELERS.remove(key);
		}
	}

	public static boolean isSameActiveFold(Player player, FoldedHallwaySpec spec) {
		return getState(player).map(FoldTravelerState::activeFold).map(spec::equals).orElse(false);
	}

	private static void tickActive(Player player, FoldTravelerState state) {
		FoldedHallwaySpec spec = state.activeFold();
		if (!sameDimension(player.level(), spec) || !controllerStillValid(player.level(), spec)) {
			clear(player);
			return;
		}

		FoldVector position = FoldMinecraftAdapter.fromVec3(player.position());
		FoldViewerSide entrySide = state.entrySide();
		if (entrySide == null || !FoldActivationBounds.containsTraversalPoint(spec, entrySide, position)) {
			TRAVELERS.put(key(player), FoldTravelerState.inactive(EXIT_COOLDOWN_TICKS));
			return;
		}

		double progress = FoldActivationBounds.realDepthProgress(spec, entrySide,
				position);
		TRAVELERS.put(key(player), FoldTravelerState.active(spec, entrySide, progress));
	}

	private static Optional<MovementEntryCandidate> findEnteringFold(Level level, FoldVector start, FoldVector end) {
		BlockPos center = BlockPos.containing(end.x, end.y, end.z);
		for (BlockPos pos : BlockPos.betweenClosed(
				center.offset(-SEARCH_RADIUS_XZ, -SEARCH_RADIUS_Y, -SEARCH_RADIUS_XZ),
				center.offset(SEARCH_RADIUS_XZ, SEARCH_RADIUS_Y, SEARCH_RADIUS_XZ))) {
			BlockState state = level.getBlockState(pos);
			if (!state.is(BlockInit.non_euclidean_hallway.get())) {
				continue;
			}
			FoldedHallwaySpec spec = FoldMinecraftAdapter.fromController(level.dimension(), pos, state);
			Optional<FoldActivationBounds.EntryCrossing> crossing = FoldActivationBounds.entryCrossing(spec, start, end);
			if (crossing.isPresent()) {
				return Optional.of(new MovementEntryCandidate(spec, crossing.get()));
			}
		}
		return Optional.empty();
	}

	private static boolean controllerStillValid(Level level, FoldedHallwaySpec spec) {
		BlockPos anchor = new BlockPos(spec.anchorX(), spec.anchorY(), spec.anchorZ());
		BlockState state = level.getBlockState(anchor);
		if (!state.is(BlockInit.non_euclidean_hallway.get())) {
			return false;
		}
		return FoldMinecraftAdapter.fromDirection(state.getValue(NonEuclideanHallwayBlock.FACING)) == spec.facing();
	}

	private static boolean sameDimension(Level level, FoldedHallwaySpec spec) {
		return level.dimension().location().toString().equals(spec.dimensionId());
	}

	private static void clear(Player player) {
		TravelerKey key = key(player);
		TRAVELERS.remove(key);
		PENDING_EXIT_AFTER_MOVE.remove(key);
	}

	private static TravelerKey key(Player player) {
		return new TravelerKey(player.getUUID(), player.level().isClientSide);
	}

	private record MovementEntryCandidate(FoldedHallwaySpec spec, FoldActivationBounds.EntryCrossing crossing) {
	}

	private record TravelerKey(UUID playerId, boolean clientSide) {
	}
}
