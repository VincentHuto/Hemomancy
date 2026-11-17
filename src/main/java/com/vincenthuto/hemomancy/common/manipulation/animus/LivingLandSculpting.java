package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.manipulation.LivingLandStrikeEntity;
import com.vincenthuto.hemomancy.common.event.MausoleumEvents;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.network.mausoleum.WardStrikePacket;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import com.vincenthuto.hemomancy.common.world.livingland.LivingLandLedger;
import com.vincenthuto.hemomancy.config.HemoConfigValues;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Living Land as a held Projection Sculpting (core-loop Track 9, CL-17: Animus, Ferric secondary). Hold Projection on
 * ground, a wall or a ceiling: the first paid pulse wakes that patch, and after a short charge every pulse sends a
 * column of the patch's own ground at the nearest enemy in front of it ({@link LivingLandStrikeEntity}). With no enemy
 * near, the patch rises instead: a step, a ledge or a hanging spur of lifted ground, up to three blocks. Letting go
 * calls the strikes home and settles the raise. The channel is the timer.
 *
 * <p>Every block it moves goes through {@link PlayerBlockEdits#lift} and {@link PlayerBlockEdits#settle} and is
 * recorded in the level's {@link LivingLandLedger}, so nothing is gained or lost whatever happens to the strike or
 * the player. Wards, rite spaces, Saint arenas and blocks with contents refuse it before any blood is paid.
 */
public final class LivingLandSculpting implements VerbSculpting {
	/** One woken patch per player; session state, cleared on logout and with the server. */
	private static final Map<UUID, Patch> PATCHES = new ConcurrentHashMap<>();

	/** The patch a player woke: its face, mode and mastery, the strikes in flight and the raise standing. */
	static final class Patch {
		final BlockPos pos;
		final Direction face;
		final LivingLandTerrainRules.Mode mode;
		final long wokeAt;
		final int level;
		final List<LivingLandStrikeEntity> strikes = new ArrayList<>();
		final Set<BlockPos> raiseTaken = new HashSet<>();
		@Nullable UUID raise;
		/** Stands at a raise so the blood holding it is drawn; it lives as long as the patch. */
		@Nullable LivingLandStrikeEntity marker;
		int raised;

		Patch(BlockPos pos, Direction face, long wokeAt, int level) {
			this.pos = pos.immutable();
			this.face = face;
			this.mode = LivingLandTerrainRules.Mode.forFace(face);
			this.wokeAt = wokeAt;
			this.level = level;
		}
	}

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block
				&& LivingLandTerrain.wakeable(player.level(), block.pos(), block.state())
				&& !StrandSculpting.takesBlood(player.level(), block);
	}

	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (PATCHES.containsKey(player.getUUID())) return true;
		if (!(target instanceof VerbTarget.Block block)) return false;
		Optional<LivingLandTerrain.Refusal> refusal = LivingLandTerrain.refusal(player, block.pos());
		if (refusal.isEmpty()) return true;
		player.displayClientMessage(Component.translatable(refusal.get().langKey()).withStyle(ChatFormatting.GRAY), true);
		if (refusal.get() == LivingLandTerrain.Refusal.WARD) {
			// The ward's own mark on the face the player tried to wake.
			MausoleumEvents.strike(player.serverLevel(), player, block.pos(), block.hit().getDirection(),
					WardStrikePacket.Kind.BREAK);
		}
		return false;
	}

	@Override
	public int cadence() {
		return LivingLandTerrainRules.CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return LivingLandTerrainRules.BLOOD_PER_PULSE;
	}

	@Override
	public double reachScale() {
		return 1.5D;
	}

	@Override
	public boolean holdsChannel() {
		return true;
	}

	@Override
	public boolean presentsCast() {
		// The waking patch and the strikes are the presentation; an outpouring round the caster would bury them.
		return false;
	}

	@Override
	public void begin(ServerPlayer player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return;
		ManipLevel mastery = HemoCapabilityAccess.getKnownManipulations(player).getManipLevel(ManipulationInit.living_land.get());
		Patch patch = new Patch(block.pos(), block.hit().getDirection(), player.level().getGameTime(),
				mastery == null ? 1 : mastery.getCurrentLevel());
		PATCHES.put(player.getUUID(), patch);
		ServerLevel level = player.serverLevel();
		Vec3 face = LivingLandTerrainRules.root(patch.pos, patch.face);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.ANIMUS_CONJURE, face,
				face.add(Vec3.atLowerCornerOf(patch.face.getNormal())), 1.2D, LivingLandTerrainRules.CHARGE_TICKS);
		level.playSound(null, face.x, face.y, face.z, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.PLAYERS, 1.0F, 0.5F);
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		Patch patch = PATCHES.get(player.getUUID());
		if (patch == null) return;
		ServerLevel level = player.serverLevel();
		if (level.getGameTime() - patch.wokeAt < LivingLandTerrainRules.CHARGE_TICKS) return;
		patch.strikes.removeIf(strike -> strike.isRemoved() || strike.isReturning());
		List<LivingEntity> enemies = enemies(player, patch);
		int room = LivingLandTerrainRules.strikesInFlight(patch.level) - patch.strikes.size();
		for (LivingEntity enemy : enemies) {
			if (room <= 0) break;
			if (strike(player, patch, enemy, pulse.workScale())) room--;
		}
		if (enemies.isEmpty() && patch.strikes.isEmpty()) raise(player, patch, pulse.workScale());
	}

	@Override
	public void hold(ServerPlayer player, VerbTarget aimed) {
		Patch patch = PATCHES.get(player.getUUID());
		if (patch != null && patch.raise != null) LivingLandLedger.get(player.serverLevel()).touch(patch.raise, player.level().getGameTime());
	}

	@Override
	public void end(ServerPlayer player, boolean released) {
		Patch patch = PATCHES.remove(player.getUUID());
		if (patch == null) return;
		for (LivingLandStrikeEntity strike : patch.strikes) {
			if (!strike.isRemoved()) strike.recall();
		}
		if (patch.marker != null) patch.marker.discard();
		if (patch.raise != null) LivingLandLedger.get(player.serverLevel()).settle(player.serverLevel(), patch.raise, player);
	}

	/** Harmable creatures in front of the patch and near it, nearest the patch first. */
	private static List<LivingEntity> enemies(ServerPlayer player, Patch patch) {
		Vec3 centre = Vec3.atCenterOf(patch.pos);
		double reach = LivingLandTerrainRules.PATCH_RADIUS + 3.0D;
		List<LivingEntity> found = player.level().getEntitiesOfClass(LivingEntity.class,
				new AABB(patch.pos).inflate(reach + LivingLandTerrainRules.MAX_FRONT),
				enemy -> enemy != player && enemy.isAlive() && ManipulationCombatHelper.canHarm(player, enemy)
						&& LivingLandTerrainRules.inFront(patch.pos, patch.face, enemy.getBoundingBox().getCenter())
						&& LivingLandTerrainRules.shadow(patch.pos, patch.face, enemy.getBoundingBox().getCenter())
								.distSqr(patch.pos) <= reach * reach);
		found.sort(Comparator.comparingDouble(enemy -> enemy.distanceToSqr(centre)));
		return found;
	}

	/** Lifts a column under {@code enemy}'s shadow and sends it out; false when no column could be taken. */
	private static boolean strike(ServerPlayer player, Patch patch, LivingEntity enemy, double workScale) {
		ServerLevel level = player.serverLevel();
		int length = LivingLandTerrainRules.pillarLength(patch.level, workScale);
		Optional<List<BlockPos>> column = LivingLandTerrainRules.sourceColumn(patch.pos, patch.face,
				enemy.getBoundingBox().getCenter(), length,
				pos -> !patch.raiseTaken.contains(pos) && LivingLandTerrain.liftable(player, pos),
				pos -> LivingLandTerrain.open(level, pos));
		if (column.isEmpty()) return false;
		boolean lifts = HemoConfigValues.get(HemoServerConfig.LIVING_LAND_LIFTS_BLOCKS, true);
		List<BlockState> carried = new ArrayList<>();
		UUID record = null;
		if (lifts) {
			List<BlockPos> taken = new ArrayList<>();
			for (BlockPos cell : column.get()) {
				BlockState state = PlayerBlockEdits.lift(player, cell);
				if (state == null) break;
				taken.add(cell);
				carried.add(state);
			}
			if (carried.size() < LivingLandTerrainRules.MIN_LENGTH) {
				for (int i = carried.size() - 1; i >= 0; i--) LivingLandLedger.restore(level, taken.get(i), carried.get(i), player);
				return false;
			}
			LivingLandLedger ledger = LivingLandLedger.get(level);
			record = ledger.open(player.getUUID(), level.getGameTime());
			for (int i = 0; i < carried.size(); i++) ledger.add(record, new LivingLandLedger.Lifted(taken.get(i), carried.get(i), null));
		} else {
			// Lifting is off in the server config: the strike is conjured ground, copies of the column, and the world stays.
			for (BlockPos cell : column.get()) carried.add(level.getBlockState(cell));
		}
		LivingLandStrikeEntity strike = new LivingLandStrikeEntity(EntityInit.living_land_strike.get(), level);
		Vec3 root = LivingLandTerrainRules.root(column.get().getFirst(), patch.face);
		strike.launch(player, record, carried, root, patch.face, enemy, LivingLandTerrainRules.damage(carried.size()));
		if (!level.addFreshEntity(strike)) {
			if (record != null) LivingLandLedger.get(level).settle(level, record, player);
			return false;
		}
		patch.strikes.add(strike);
		level.playSound(null, root.x, root.y, root.z, carried.getFirst().getSoundType().getBreakSound(),
				SoundSource.PLAYERS, 1.2F, 0.55F);
		return true;
	}

	/** Raises the patch one block, up to the dry-aware height. */
	private static void raise(ServerPlayer player, Patch patch, double workScale) {
		int height = LivingLandTerrainRules.maxRaise(workScale);
		if (patch.raised >= height || !HemoConfigValues.get(HemoServerConfig.LIVING_LAND_LIFTS_BLOCKS, true)) return;
		ServerLevel level = player.serverLevel();
		List<LivingLandTerrainRules.RaiseStep> steps = LivingLandTerrainRules.raiseSteps(patch.pos, patch.face,
				patch.raised, patch.raised + 1,
				pos -> LivingLandTerrain.liftable(player, pos) && !(level.getBlockState(pos).getBlock() instanceof FallingBlock),
				pos -> LivingLandTerrain.open(level, pos)
						&& level.getEntitiesOfClass(LivingEntity.class, new AABB(pos)).isEmpty(),
				patch.raiseTaken::contains);
		for (LivingLandTerrainRules.RaiseStep step : steps) {
			BlockState state = PlayerBlockEdits.lift(player, step.from());
			if (state == null) return;
			if (!PlayerBlockEdits.settle(player, step.into(), state)) {
				LivingLandLedger.restore(level, step.from(), state, player);
				return;
			}
			LivingLandLedger ledger = LivingLandLedger.get(level);
			if (patch.raise == null || !ledger.holds(patch.raise)) patch.raise = ledger.open(player.getUUID(), level.getGameTime());
			ledger.add(patch.raise, new LivingLandLedger.Lifted(step.from(), state, step.into()));
			patch.raiseTaken.add(step.from());
			patch.raised++;
			Vec3 at = Vec3.atCenterOf(step.into());
			ManipulationVisuals.burst(level, ManipulationVisuals.Form.ANIMUS_IMPACT, at,
					at.add(Vec3.atLowerCornerOf(patch.face.getNormal())), 0.7D, 10);
			level.playSound(null, at.x, at.y, at.z, state.getSoundType().getPlaceSound(), SoundSource.PLAYERS, 1.0F, 0.6F);
		}
		if (patch.raised > 0 && (patch.marker == null || patch.marker.isRemoved())) {
			LivingLandStrikeEntity marker = new LivingLandStrikeEntity(EntityInit.living_land_strike.get(), level);
			marker.markRaise(player, LivingLandTerrainRules.root(patch.pos, patch.face), patch.face);
			if (level.addFreshEntity(marker)) patch.marker = marker;
		}
		if (patch.marker != null) patch.marker.setRaised(patch.raised);
	}

	/** The strikes a player has in flight; for tests and the review client. */
	public static List<LivingLandStrikeEntity> strikes(Player player) {
		Patch patch = PATCHES.get(player.getUUID());
		return patch == null ? List.of() : List.copyOf(patch.strikes);
	}

	/** How high the player's raise stands; 0 with no patch. */
	public static int raised(Player player) {
		Patch patch = PATCHES.get(player.getUUID());
		return patch == null ? 0 : patch.raised;
	}

	public static boolean hasPatch(Player player) {
		return PATCHES.containsKey(player.getUUID());
	}

	public static void clearSessionState() {
		PATCHES.clear();
	}

	public static void logout(Player player) {
		// The raise is left to the ledger's sweep, which settles it once nothing touches it.
		PATCHES.remove(player.getUUID());
	}
}
